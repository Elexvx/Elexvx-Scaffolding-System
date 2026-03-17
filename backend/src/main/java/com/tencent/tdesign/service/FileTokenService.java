package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.StorageSetting;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
public class FileTokenService {
  private static final Logger log = LoggerFactory.getLogger(FileTokenService.class);
  private static final byte TOKEN_VERSION = 1;
  private static final int IV_SIZE = 12;
  private static final int KEY_SIZE_BYTES = 32;

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  private final SecretKeySpec key;
  private final SecureRandom random = new SecureRandom();
  private final String publicPrefix;
  private final long tokenTtlSeconds;

  public FileTokenService(
    @Value("${tdesign.file.token-secret:}") String secret,
    @Value("${server.servlet.context-path:/api}") String contextPath,
    @Value("${tdesign.file.token-ttl-seconds:600}") long tokenTtlSeconds
  ) {
    String effective = (secret == null) ? "" : secret.trim();
    if (effective.isEmpty()) {
      byte[] generated = new byte[KEY_SIZE_BYTES];
      random.nextBytes(generated);
      this.key = new SecretKeySpec(generated, "AES");
      log.warn("File token secret is empty; using ephemeral random key. "
        + "Set tdesign.file.token-secret to keep links stable across restarts.");
    } else {
      this.key = new SecretKeySpec(sha256(effective), "AES");
    }
    this.tokenTtlSeconds = Math.max(1, tokenTtlSeconds);
    String prefix = (contextPath == null) ? "" : contextPath.trim();
    if (prefix.isEmpty() || "/".equals(prefix)) {
      prefix = "";
    }
    this.publicPrefix = prefix + "/files/";
  }

  public String buildAccessUrl(StorageSetting.Provider provider, String objectKey) {
    long expiresAt = Instant.now().plusSeconds(tokenTtlSeconds).getEpochSecond();
    String token = encrypt(new TokenPayload(provider, objectKey, expiresAt));
    return publicPrefix + token;
  }

  public String extractToken(String url) {
    if (url == null || url.isBlank()) return null;
    String clean = url.trim();
    int q = clean.indexOf('?');
    if (q >= 0) clean = clean.substring(0, q);
    try {
      if (clean.startsWith("http://") || clean.startsWith("https://")) {
        URI uri = new URI(clean);
        if (uri.getPath() != null) clean = uri.getPath();
      }
    } catch (Exception parseUriException) {
      log.debug("解析文件访问 URL 失败，按原始路径继续，url={}", clean, parseUriException);
    }
    int idx = clean.indexOf("/files/");
    if (idx >= 0) {
      return clean.substring(idx + "/files/".length());
    }
    return null;
  }

  public TokenPayload decrypt(String token) {
    if (token == null || token.isBlank()) throw badRequest("Empty file token");
    try {
      byte[] raw = Base64.getUrlDecoder().decode(token);
      ByteBuffer buffer = ByteBuffer.wrap(raw);
      byte version = buffer.get();
      if (version != TOKEN_VERSION) {
        throw badRequest("Unsupported token version");
      }
      byte[] iv = new byte[IV_SIZE];
      buffer.get(iv);
      byte[] encrypted = new byte[buffer.remaining()];
      buffer.get(encrypted);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
      byte[] plain = cipher.doFinal(encrypted);
      String payload = new String(plain, StandardCharsets.UTF_8);
      TokenPayload parsed = TokenPayload.fromToken(payload);
      if (parsed.isExpired()) {
        throw badRequest("File token expired");
      }
      return parsed;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw badRequest("Invalid file token");
    }
  }

  private String encrypt(TokenPayload payload) {
    try {
      byte[] iv = new byte[IV_SIZE];
      random.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
      byte[] encrypted = cipher.doFinal(payload.toToken().getBytes(StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(1 + iv.length + encrypted.length);
      buffer.put(TOKEN_VERSION);
      buffer.put(iv);
      buffer.put(encrypted);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encrypt file token");
    }
  }

  private byte[] sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to initialize file token secret");
    }
  }

  public static class TokenPayload {
    private final StorageSetting.Provider provider;
    private final String objectKey;
    private final long expiresAt;

    public TokenPayload(StorageSetting.Provider provider, String objectKey, long expiresAt) {
      if (provider == null || objectKey == null || objectKey.isBlank()) {
        throw badRequest("Invalid token payload");
      }
      this.provider = provider;
      this.objectKey = objectKey;
      this.expiresAt = expiresAt;
    }

    public StorageSetting.Provider getProvider() {
      return provider;
    }

    public String getObjectKey() {
      return objectKey;
    }

    public long getExpiresAt() {
      return expiresAt;
    }

    public boolean isExpired() {
      return Instant.now().getEpochSecond() >= expiresAt;
    }

    private String toToken() {
      String encodedKey = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(objectKey.getBytes(StandardCharsets.UTF_8));
      return "v1|" + provider.name() + "|" + expiresAt + "|" + encodedKey;
    }

    private static TokenPayload fromToken(String raw) {
      String[] parts = raw.split("\\|", 4);
      if (parts.length != 4 || !"v1".equals(parts[0])) {
        throw badRequest("Invalid token payload");
      }
      StorageSetting.Provider provider = StorageSetting.Provider.valueOf(parts[1]);
      long expiresAt = Long.parseLong(parts[2]);
      byte[] decoded = Base64.getUrlDecoder().decode(parts[3]);
      String key = new String(decoded, StandardCharsets.UTF_8);
      return new TokenPayload(provider, key, expiresAt);
    }
  }
}
