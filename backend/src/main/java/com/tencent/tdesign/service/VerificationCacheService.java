package com.tencent.tdesign.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class VerificationCacheService {
  private static final int CAPTCHA_TTL_SECONDS = 120;
  private static final int CODE_TTL_SECONDS = 300;
  private static final int LOGIN_PENDING_TTL_SECONDS = 120;
  private static final int CONCURRENT_STREAM_TICKET_TTL_SECONDS = 60;

  private final Cache<String, CaptchaService.CaptchaEntry> captchaCache = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(CAPTCHA_TTL_SECONDS))
    .maximumSize(10_000)
    .build();

  private final Cache<String, String> smsCodeCache = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(CODE_TTL_SECONDS))
    .maximumSize(20_000)
    .build();

  private final Cache<String, String> emailCodeCache = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(CODE_TTL_SECONDS))
    .maximumSize(20_000)
    .build();

  private final Cache<String, ConcurrentLoginService.PendingLogin> pendingLoginCache = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(LOGIN_PENDING_TTL_SECONDS))
    .maximumSize(50_000)
    .build();

  private final Cache<String, String> concurrentStreamTicketCache = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(CONCURRENT_STREAM_TICKET_TTL_SECONDS))
    .maximumSize(20_000)
    .build();

  public Cache<String, CaptchaService.CaptchaEntry> captchaCache() {
    return captchaCache;
  }

  public Cache<String, String> smsCodeCache() {
    return smsCodeCache;
  }

  public Cache<String, String> emailCodeCache() {
    return emailCodeCache;
  }

  public Cache<String, ConcurrentLoginService.PendingLogin> pendingLoginCache() {
    return pendingLoginCache;
  }

  public Cache<String, String> concurrentStreamTicketCache() {
    return concurrentStreamTicketCache;
  }
}
