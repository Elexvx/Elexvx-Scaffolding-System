package com.tencent.tdesign.service;

import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SensitiveFieldCryptoService {
  private static final String PREFIX = "ENC:";
  private static final byte VERSION = 1;
  private static final int IV_LENGTH = 12;
  private static final int TAG_LENGTH = 128;

  private final SecretKeySpec key;
  private final SecureRandom secureRandom = new SecureRandom();

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public SensitiveFieldCryptoService(@Value("${tdesign.security.field-secret:}") String secret) {
    String effective = secret == null ? "" : secret.trim();
    if (!StringUtils.hasText(effective)) {
      throw new IllegalStateException("tdesign.security.field-secret 未配置，服务拒绝启动");
    }
    if (effective.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("tdesign.security.field-secret 强度不足，要求至少 32 bytes");
    }
    this.key = new SecretKeySpec(sha256(effective), "AES");
  }

  public String encryptIfNeeded(String value) {
    if (!StringUtils.hasText(value)) return value;
    if (value.startsWith(PREFIX)) return value;
    return PREFIX + encrypt(value);
  }

  public String decryptIfNeeded(String value) {
    if (!StringUtils.hasText(value)) return value;
    if (!value.startsWith(PREFIX)) return value;
    String payload = value.substring(PREFIX.length());
    try {
      return decrypt(payload);
    } catch (Exception e) {
      return value;
    }
  }

  private String encrypt(String plain) {
    byte[] iv = new byte[IV_LENGTH];
    secureRandom.nextBytes(iv);
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
      byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(1 + iv.length + encrypted.length);
      buffer.put(VERSION);
      buffer.put(iv);
      buffer.put(encrypted);
      return Base64.getEncoder().encodeToString(buffer.array());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encrypt sensitive value", e);
    }
  }

  private String decrypt(String token) throws Exception {
    byte[] raw = Base64.getDecoder().decode(token);
    if (raw.length < 1 + IV_LENGTH) throw badRequest("Invalid encrypted payload");
    ByteBuffer buffer = ByteBuffer.wrap(raw);
    byte version = buffer.get();
    if (version != VERSION) throw badRequest("Unsupported encrypted payload version");
    byte[] iv = new byte[IV_LENGTH];
    buffer.get(iv);
    byte[] encrypted = new byte[buffer.remaining()];
    buffer.get(encrypted);
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
    byte[] plain = cipher.doFinal(encrypted);
    return new String(plain, StandardCharsets.UTF_8);
  }

  private byte[] sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to initialize field secret", e);
    }
  }
}
