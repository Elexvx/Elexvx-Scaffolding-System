package top.elexvx.admin.controller;

import top.elexvx.admin.annotation.RepeatSubmit;
import top.elexvx.admin.dto.FileUploadCompleteRequest;
import top.elexvx.admin.dto.FileUploadInitRequest;
import top.elexvx.admin.exception.ErrorCodes;
import top.elexvx.admin.security.AuthContext;
import top.elexvx.admin.service.FileChunkUploadService;
import top.elexvx.admin.service.ObjectStorageService;
import top.elexvx.admin.service.SecurityRateLimitService;
import top.elexvx.admin.service.UploadFileValidationService;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.FileUploadSessionResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/system/file")
public class FileUploadController {
  private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
    "pdf",
    "doc",
    "docx",
    "xls",
    "xlsx",
    "csv",
    "ppt",
    "pptx",
    "png",
    "jpg",
    "jpeg",
    "gif",
    "webp",
    "bmp"
  );
  private final ObjectStorageService storageService;
  private final FileChunkUploadService chunkUploadService;
  private final SecurityRateLimitService rateLimitService;
  private final UploadFileValidationService uploadFileValidationService;
  private final AuthContext authContext;
  private final long maxFileSizeBytes;

  public FileUploadController(
    ObjectStorageService storageService,
    FileChunkUploadService chunkUploadService,
    SecurityRateLimitService rateLimitService,
    UploadFileValidationService uploadFileValidationService,
    AuthContext authContext,
    @Value("${elexvx.file.upload.max-file-size-mb:100}") long maxFileSizeMb
  ) {
    this.storageService = storageService;
    this.chunkUploadService = chunkUploadService;
    this.rateLimitService = rateLimitService;
    this.uploadFileValidationService = uploadFileValidationService;
    this.authContext = authContext;
    this.maxFileSizeBytes = Math.max(1, maxFileSizeMb) * 1024 * 1024;
  }

  @PostMapping("/upload")
  @RepeatSubmit
  public ApiResponse<Map<String, String>> upload(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "page", required = false) String page
  ) throws IOException {
    long userId = authContext.requireUserId();
    rateLimitService.checkUploadRequestQuota(userId);
    if (file == null || file.isEmpty()) {
      return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "上传文件不能为空");
    }

    SecurityRateLimitService.UploadQuotaLease lease = null;
    try {
      String original = file.getOriginalFilename();
      if (file.getSize() > maxFileSizeBytes) {
        return ApiResponse.failure(ErrorCodes.PAYLOAD_TOO_LARGE, "上传文件过大");
      }
      String ext = fileExtension(original);
      if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
        return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "文件格式不支持");
      }
      try (java.io.InputStream in = file.getInputStream()) {
        uploadFileValidationService.validate(original, file.getContentType(), in);
      }
      lease = rateLimitService.acquireUploadQuota(userId, file.getSize());
      log.info("开始处理文件上传: {}, size: {} bytes", original, file.getSize());
      String url = storageService.upload(file, "business", page);
      log.info("文件上传成功: {}, url: {}", original, url);
      return ApiResponse.success(Map.of("url", url));
    } catch (IllegalArgumentException e) {
      if (lease != null) {
        rateLimitService.releaseUploadQuota(lease);
      }
      String msg = e.getMessage() == null ? "上传请求被拒绝" : e.getMessage();
      int code = (msg.contains("频繁") || msg.contains("上限")) ? ErrorCodes.TOO_MANY_REQUESTS : ErrorCodes.BAD_REQUEST;
      return ApiResponse.failure(code, msg);
    } catch (Exception e) {
      if (lease != null) {
        rateLimitService.releaseUploadQuota(lease);
      }
      log.error("文件上传过程中发生错误", e);
      return ApiResponse.failure(ErrorCodes.INTERNAL_SERVER_ERROR, "文件保存失败，请稍后重试");
    }
  }

  @PostMapping("/upload/init")
  @RepeatSubmit
  public ApiResponse<FileUploadSessionResponse> initUpload(@RequestBody @Valid FileUploadInitRequest request) {
    long userId = authContext.requireUserId();
    rateLimitService.checkUploadRequestQuota(userId);
    String ext = fileExtension(request.getFileName());
    if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
      return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "文件格式不支持");
    }
    rateLimitService.checkUploadQuotaPreview(userId, request.getFileSize());
    return ApiResponse.success(chunkUploadService.initSession(request));
  }

  @GetMapping("/upload/status")
  public ApiResponse<FileUploadSessionResponse> uploadStatus(@RequestParam String uploadId) {
    return ApiResponse.success(chunkUploadService.getStatus(uploadId));
  }

  @PostMapping("/upload/chunk")
  @RepeatSubmit
  public ApiResponse<Boolean> uploadChunk(
    @RequestParam String uploadId,
    @RequestParam int chunkIndex,
    @RequestParam("chunk") MultipartFile chunk
  ) {
    long userId = authContext.requireUserId();
    rateLimitService.checkUploadRequestQuota(userId);
    chunkUploadService.saveChunk(uploadId, chunkIndex, chunk);
    return ApiResponse.success(true);
  }

  @PostMapping("/upload/complete")
  @RepeatSubmit
  public ApiResponse<Map<String, String>> completeUpload(@RequestBody @Valid FileUploadCompleteRequest request) {
    long userId = authContext.requireUserId();
    rateLimitService.checkUploadRequestQuota(userId);
    FileUploadSessionResponse session = chunkUploadService.getStatus(request.getUploadId());
    long fileSize = session.getFileSize() == null ? 0L : session.getFileSize();
    SecurityRateLimitService.UploadQuotaLease lease = rateLimitService.acquireUploadQuota(userId, fileSize);
    try {
      String url = chunkUploadService.finalizeUpload(request.getUploadId(), request.getFolder(), request.getPage());
      return ApiResponse.success(Map.of("url", url));
    } catch (RuntimeException e) {
      rateLimitService.releaseUploadQuota(lease);
      throw e;
    }
  }

  @PostMapping("/upload/cancel")
  @RepeatSubmit
  public ApiResponse<Boolean> cancelUpload(
    @RequestParam(required = false) String uploadId,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    long userId = authContext.requireUserId();
    rateLimitService.checkUploadRequestQuota(userId);
    if (uploadId == null && body != null) {
      Object raw = body.get("uploadId");
      if (raw != null) uploadId = String.valueOf(raw);
    }
    if (uploadId == null || uploadId.isBlank()) {
      return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "uploadId 不能为空");
    }
    return ApiResponse.success(chunkUploadService.cancel(uploadId));
  }

  private String fileExtension(String name) {
    if (name == null) return null;
    String n = name.trim();
    int dot = n.lastIndexOf('.');
    if (dot < 0 || dot == n.length() - 1) return null;
    String ext = n.substring(dot + 1).toLowerCase();
    return ext.isEmpty() ? null : ext;
  }
}
