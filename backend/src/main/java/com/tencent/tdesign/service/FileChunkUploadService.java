package com.tencent.tdesign.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.dto.FileUploadInitRequest;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.vo.FileUploadSessionResponse;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.regex.Pattern;

@Service
public class FileChunkUploadService {
  private static final Logger log = LoggerFactory.getLogger(FileChunkUploadService.class);
  private static final int DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;
  private static final int MIN_CHUNK_SIZE = 1 * 1024;
  private static final int MAX_CHUNK_SIZE = 20 * 1024 * 1024;
  private static final int UPLOAD_ID_SALT_BYTES = 32;
  private static final String DEFAULT_FOLDER = "business";
  private static final String META_FILE = "session.json";
  private static final int MAX_FINGERPRINT_LENGTH = 512;
  private static final long SESSION_TTL_MILLIS = 30 * 60 * 1000L;
  private static final Pattern UPLOAD_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{20,100}$");

  private final ObjectStorageService storageService;
  private final ObjectMapper objectMapper;
  private final Path chunkRoot;
  private final ConcurrentMap<String, Object> sessionLocks = new ConcurrentHashMap<>();
  private final AuthContext authContext;
  private final byte[] uploadIdSalt;
  private final long maxFileSizeBytes;
  private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "chunk-upload-cleaner");
    thread.setDaemon(true);
    return thread;
  });

  public FileChunkUploadService(
    ObjectStorageService storageService,
    ObjectMapper objectMapper,
    AuthContext authContext,
    @Value("${tdesign.file.token-secret:}") String secret,
    @Value("${tdesign.file.upload.max-file-size-mb:100}") long maxFileSizeMb
  ) {
    this.storageService = storageService;
    this.objectMapper = objectMapper;
    this.authContext = authContext;
    this.maxFileSizeBytes = Math.max(1, maxFileSizeMb) * 1024 * 1024;
    String effective = secret == null ? "" : secret.trim();
    if (effective.isEmpty()) {
      byte[] generated = new byte[UPLOAD_ID_SALT_BYTES];
      new SecureRandom().nextBytes(generated);
      this.uploadIdSalt = generated;
      log.warn("tdesign.file.token-secret 未配置，分片 uploadId 使用进程级随机盐。建议在生产环境配置固定高强度密钥。");
    } else {
      this.uploadIdSalt = effective.getBytes(StandardCharsets.UTF_8);
    }
    this.chunkRoot = Paths.get(System.getProperty("user.dir"), "runtime", "upload-chunks").toAbsolutePath().normalize();
    try {
      Files.createDirectories(chunkRoot);
    } catch (IOException e) {
      throw new IllegalStateException("无法创建分片缓存目录", e);
    }
    cleanupExpiredSessions();
    cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 10, 10, TimeUnit.MINUTES);
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  @PreDestroy
  public void shutdownCleanupExecutor() {
    cleanupExecutor.shutdownNow();
  }

  public FileUploadSessionResponse initSession(FileUploadInitRequest request) {
    long userId = authContext.requireUserId();
    UploadSession session = ensureSession(request, userId);
    return FileUploadSessionResponse.from(
      session.getUploadId(),
      session.getFileName(),
      session.getFileSize(),
      session.getChunkSize(),
      session.getTotalChunks(),
      session.getUploadedChunkList()
    );
  }

  public FileUploadSessionResponse getStatus(String uploadId) {
    long userId = authContext.requireUserId();
    UploadSession session = loadSession(uploadId, userId);
    return FileUploadSessionResponse.from(
      session.getUploadId(),
      session.getFileName(),
      session.getFileSize(),
      session.getChunkSize(),
      session.getTotalChunks(),
      session.getUploadedChunkList()
    );
  }

  public void saveChunk(String uploadId, int chunkIndex, MultipartFile chunk) {
    Objects.requireNonNull(chunk, "chunk 不能为空");
    long userId = authContext.requireUserId();
    UploadSession session = loadSession(uploadId, userId);
    validateChunkIndex(session, chunkIndex);
    if (chunk.isEmpty()) {
      throw badRequest("分片数据不能为空");
    }
    long expectedSize = expectedChunkSize(session, chunkIndex);
    if (chunk.getSize() != expectedSize) {
      throw badRequest("分片大小不匹配，期望 " + expectedSize + " 字节，实际 " + chunk.getSize() + " 字节");
    }
    Path dir = sessionDir(uploadId);
    Path chunkFile = dir.resolve(chunkFileName(chunkIndex));
    try (InputStream in = chunk.getInputStream()) {
      Files.copy(in, chunkFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("保存分片失败", e);
    }
    synchronized (lockFor(uploadId)) {
      session.markChunkUploaded(chunkIndex);
      session.touch();
      persistSession(session, dir);
    }
  }

  public String finalizeUpload(String uploadId, String folder, String page) {
    long userId = authContext.requireUserId();
    UploadSession session = loadSession(uploadId, userId);
    if (!session.isComplete()) {
      throw new IllegalStateException("还有未上传的分片");
    }
    Path dir = sessionDir(uploadId);
    folder = StringUtils.hasText(folder) ? folder : DEFAULT_FOLDER;
    page = StringUtils.hasText(page) ? page : "console-download";
    try (InputStream stream = openCombinedStream(dir, session.totalChunks)) {
      String url = storageService.uploadStream(stream, session.fileSize, session.fileName, folder, page);
      deleteSessionDir(dir);
      return url;
    } catch (IOException e) {
      throw new IllegalStateException("组合分片并上传失败", e);
    } finally {
      sessionLocks.remove(uploadId);
    }
  }

  public boolean cancel(String uploadId) {
    long userId = authContext.requireUserId();
    loadSession(uploadId, userId);
    Path dir = sessionDir(uploadId);
    boolean deleted = deleteSessionDir(dir);
    sessionLocks.remove(uploadId);
    return deleted;
  }

  private UploadSession ensureSession(FileUploadInitRequest request, long userId) {
    if (!StringUtils.hasText(request.getFingerprint())) {
      throw badRequest("文件指纹不能为空");
    }
    String fingerprint = request.getFingerprint().trim();
    if (fingerprint.length() > MAX_FINGERPRINT_LENGTH) {
      throw badRequest("文件指纹过长");
    }
    String uploadId = buildUploadId(userId, fingerprint);
    Path dir = sessionDir(uploadId);
    synchronized (lockFor(uploadId)) {
      UploadSession existing = loadSessionIfExists(dir);
      int chunkSize = normalizeChunkSize(request.getChunkSize());
      long fileSize = request.getFileSize();
      if (fileSize <= 0) {
        throw badRequest("文件大小必须大于 0");
      }
      if (fileSize > maxFileSizeBytes) {
        throw badRequest("上传文件过大");
      }
      if (existing != null && existing.getOwnerUserId() != userId) {
        throw badRequest("上传会话无权限访问: " + uploadId);
      }
      if (existing != null && (!existing.matches(request.getFileName(), fileSize, chunkSize))) {
        deleteSessionDir(dir);
        existing = null;
      }
      if (existing == null) {
        try {
          Files.createDirectories(dir);
        } catch (IOException e) {
          throw new IllegalStateException("无法创建分片目录", e);
        }
        existing = new UploadSession(uploadId, userId, request.getFingerprint(), request.getFileName(), fileSize, chunkSize);
        persistSession(existing, dir);
      } else {
        existing.setFileName(request.getFileName());
        existing.setFileSize(fileSize);
        existing.setChunkSize(chunkSize);
        existing.recalculateTotalChunks();
        existing.touch();
        persistSession(existing, dir);
      }
      return existing;
    }
  }

  private UploadSession loadSession(String uploadId, long userId) {
    Path dir = sessionDir(uploadId);
    UploadSession session = loadSessionIfExists(dir);
    if (session == null) {
      throw badRequest("上传会话不存在: " + uploadId);
    }
    if (isExpired(session)) {
      synchronized (lockFor(uploadId)) {
        deleteSessionDir(dir);
        sessionLocks.remove(uploadId);
      }
      throw badRequest("上传会话已过期: " + uploadId);
    }
    if (session.getOwnerUserId() != userId) {
      throw badRequest("上传会话无权限访问: " + uploadId);
    }
    return session;
  }

  private UploadSession loadSessionIfExists(Path dir) {
    Path meta = dir.resolve(META_FILE);
    if (!Files.exists(meta)) {
      return null;
    }
    try {
      UploadSession session = objectMapper.readValue(meta.toFile(), UploadSession.class);
      if (session.uploadedChunks == null) {
        session.uploadedChunks = new TreeSet<>();
      }
      return session;
    } catch (IOException e) {
      throw new IllegalStateException("读取分片会话失败", e);
    }
  }

  private void persistSession(UploadSession session, Path dir) {
    Path meta = dir.resolve(META_FILE);
    try {
      objectMapper.writeValue(meta.toFile(), session);
    } catch (IOException e) {
      throw new IllegalStateException("写入分片会话失败", e);
    }
  }

  private Path sessionDir(String uploadId) {
    validateUploadId(uploadId);
    Path dir = chunkRoot.resolve(uploadId).normalize();
    if (!dir.startsWith(chunkRoot)) {
      throw badRequest("非法上传会话");
    }
    return dir;
  }

  private void validateUploadId(String uploadId) {
    if (!StringUtils.hasText(uploadId) || !UPLOAD_ID_PATTERN.matcher(uploadId).matches()) {
      throw badRequest("非法 uploadId");
    }
  }

  private InputStream openCombinedStream(Path dir, int totalChunks) throws IOException {
    List<Path> chunkFiles = new ArrayList<>();
    for (int i = 0; i < totalChunks; i++) {
      Path chunkFile = dir.resolve(chunkFileName(i));
      if (!Files.exists(chunkFile)) {
        throw new IllegalStateException("缺失的分片: " + i);
      }
      chunkFiles.add(chunkFile);
    }
    return new LazyChunkSequenceInputStream(chunkFiles);
  }

  private static class LazyChunkSequenceInputStream extends InputStream {
    private final List<Path> chunkFiles;
    private int index;
    private InputStream current;

    private LazyChunkSequenceInputStream(List<Path> chunkFiles) {
      this.chunkFiles = chunkFiles;
    }

    @Override
    public int read() throws IOException {
      while (true) {
        InputStream stream = ensureCurrent();
        if (stream == null) return -1;
        int value = stream.read();
        if (value >= 0) return value;
        switchToNext();
      }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (len == 0) return 0;
      while (true) {
        InputStream stream = ensureCurrent();
        if (stream == null) return -1;
        int count = stream.read(b, off, len);
        if (count >= 0) return count;
        switchToNext();
      }
    }

    @Override
    public void close() throws IOException {
      if (current != null) {
        current.close();
        current = null;
      }
    }

    private InputStream ensureCurrent() throws IOException {
      if (current != null) return current;
      if (index >= chunkFiles.size()) return null;
      current = Files.newInputStream(chunkFiles.get(index));
      return current;
    }

    private void switchToNext() throws IOException {
      if (current != null) {
        current.close();
        current = null;
      }
      index++;
    }
  }

  private String chunkFileName(int index) {

    return "chunk-" + index;
  }

  private void validateChunkIndex(UploadSession session, int chunkIndex) {
    if (chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
      throw badRequest("分片索引越界: " + chunkIndex);
    }
  }

  private long expectedChunkSize(UploadSession session, int chunkIndex) {
    long start = (long) chunkIndex * session.getChunkSize();
    long remaining = session.getFileSize() - start;
    if (remaining <= 0) {
      throw badRequest("分片索引越界: " + chunkIndex);
    }
    return Math.min(session.getChunkSize(), remaining);
  }

  private int normalizeChunkSize(Integer requested) {
    if (requested == null) {
      return DEFAULT_CHUNK_SIZE;
    }
    int size = Math.max(MIN_CHUNK_SIZE, Math.min(requested, MAX_CHUNK_SIZE));
    return size;
  }

  private Object lockFor(String uploadId) {
    return sessionLocks.computeIfAbsent(uploadId, (key) -> new Object());
  }

  private boolean deleteSessionDir(Path dir) {
    if (!Files.exists(dir)) {
      return false;
    }
    try (Stream<Path> stream = Files.walk(dir)) {
      stream.sorted(Comparator.reverseOrder())
        .forEach((path) -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException deleteException) {
            log.debug("删除分片文件失败，path={}", path, deleteException);
          }
        });
      return true;
    } catch (IOException e) {
      throw new IllegalStateException("删除分片目录失败", e);
    }
  }

  private String buildUploadId(long userId, String fingerprint) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(uploadIdSalt);
      digest.update((byte) ':');
      digest.update(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
      digest.update((byte) ':');
      digest.update(fingerprint.getBytes(StandardCharsets.UTF_8));
      byte[] hash = digest.digest();
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("初始化上传 ID 失败", e);
    }
  }


  private void cleanupExpiredSessions() {
    try (Stream<Path> stream = Files.list(chunkRoot)) {
      stream.filter(Files::isDirectory).forEach(this::cleanupIfExpired);
    } catch (IOException listException) {
      log.warn("扫描过期上传会话失败，chunkRoot={}", chunkRoot, listException);
    }
  }

  private void cleanupIfExpired(Path dir) {
    String uploadId = dir.getFileName() == null ? "" : dir.getFileName().toString();
    if (uploadId.isBlank() || !UPLOAD_ID_PATTERN.matcher(uploadId).matches()) return;
    synchronized (lockFor(uploadId)) {
      UploadSession session = loadSessionIfExists(dir);
      if (session != null && isExpired(session)) {
        deleteSessionDir(dir);
        sessionLocks.remove(uploadId);
      }
    }
  }

  private boolean isExpired(UploadSession session) {
    return System.currentTimeMillis() - session.getUpdatedAt() > SESSION_TTL_MILLIS;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @SuppressWarnings("unused")
  private static class UploadSession {
    private String uploadId;
    private long ownerUserId;
    private String fingerprint;
    private String fileName;
    private long fileSize;
    private int chunkSize;
    private int totalChunks;
    private Set<Integer> uploadedChunks = new TreeSet<>();
    private long updatedAt;

    public UploadSession() {}

    public UploadSession(String uploadId, long ownerUserId, String fingerprint, String fileName, long fileSize, int chunkSize) {
      this.uploadId = uploadId;
      this.ownerUserId = ownerUserId;
      this.fingerprint = fingerprint;
      this.fileName = fileName;
      this.fileSize = fileSize;
      this.chunkSize = chunkSize;
      recalculateTotalChunks();
      this.updatedAt = Instant.now().toEpochMilli();
    }

    public void recalculateTotalChunks() {
      if (chunkSize <= 0) {
        chunkSize = DEFAULT_CHUNK_SIZE;
      }
      this.totalChunks = (int) Math.max(1, (fileSize + chunkSize - 1) / chunkSize);
    }

    public List<Integer> getUploadedChunkList() {
      return new ArrayList<>(uploadedChunks);
    }

    public String getUploadId() {
      return uploadId;
    }

    public long getOwnerUserId() {
      return ownerUserId;
    }

    public String getFileName() {
      return fileName;
    }

    public long getFileSize() {
      return fileSize;
    }

    public int getChunkSize() {
      return chunkSize;
    }

    public int getTotalChunks() {
      return totalChunks;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
      this.fileSize = fileSize;
    }

    public void setChunkSize(int chunkSize) {
      this.chunkSize = chunkSize;
    }

    public boolean matches(String fileName, long size, int chunkSize) {
      return Objects.equals(this.fileName, fileName)
        && this.fileSize == size
        && this.chunkSize == chunkSize;
    }

    public int getUploadedCount() {
      return uploadedChunks.size();
    }

    public boolean isComplete() {
      return getUploadedCount() >= totalChunks;
    }

    public void markChunkUploaded(int index) {
      uploadedChunks.add(index);
      updatedAt = Instant.now().toEpochMilli();
    }

    public long getUpdatedAt() {
      return updatedAt;
    }

    public void touch() {
      updatedAt = Instant.now().toEpochMilli();
    }
  }
}
