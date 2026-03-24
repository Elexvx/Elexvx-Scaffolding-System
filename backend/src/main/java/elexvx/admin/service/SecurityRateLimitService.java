package elexvx.admin.service;

import elexvx.admin.config.properties.ElexvxCoreProperties;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.exception.LoginRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SecurityRateLimitService {
  private static final Logger log = LoggerFactory.getLogger(SecurityRateLimitService.class);
  private final RedisTemplate<String, Object> redisTemplate;
  private final HttpServletRequest request;
  private final int loginPerMinute;
  private final int loginFailThreshold;
  private final int smsPerMinute;
  private final int smsPerDay;
  private final int uploadRequestsPerMinute;
  private final int uploadFilesPerDay;
  private final long uploadBytesPerDay;

  public SecurityRateLimitService(
    RedisTemplate<String, Object> redisTemplate,
    HttpServletRequest request,
    ElexvxCoreProperties properties
  ) {
    this.redisTemplate = redisTemplate;
    this.request = request;
    ElexvxCoreProperties.Security.RateLimit rateLimit = properties.getSecurity().getRateLimit();
    this.loginPerMinute = rateLimit.getLoginPerMinute();
    this.loginFailThreshold = rateLimit.getLoginFailThreshold();
    this.smsPerMinute = rateLimit.getSmsEmailPerMinute();
    this.smsPerDay = rateLimit.getSmsEmailPerDay();
    this.uploadRequestsPerMinute = rateLimit.getUploadRequestsPerMinute();
    this.uploadFilesPerDay = rateLimit.getUploadFilesPerDay();
    this.uploadBytesPerDay = Math.max(0L, rateLimit.getUploadBytesPerDayMb()) * 1024L * 1024L;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public void checkLoginAttempt(String account) {
    String ip = clientIp();
    String normalizedAccount = normalize(account);
    if (loginPerMinute > 0) {
      checkRateLimit("sec:login:ip:" + ip, account, ip, "IP");
      checkRateLimit("sec:login:acct:" + normalizedAccount, account, ip, "ACCOUNT");
    }

    long failCount = getCount("sec:login:fail:" + ip + ":" + normalizedAccount);
    if (failCount >= loginFailThreshold) {
      log.warn(
        "LOGIN_RATE_LIMITED account={} ip={} failCount={} threshold={}",
        normalizedAccount,
        ip,
        failCount,
        loginFailThreshold
      );
      throw LoginRateLimitException.tooManyAttempts(account, failCount, loginFailThreshold);
    }
  }

  private void checkRateLimit(String key, String account, String ip, String scope) {
    long count = increment(key, Duration.ofMinutes(1));
    if (count > loginPerMinute) {
      log.warn(
        "LOGIN_RATE_LIMITED scope={} account={} ip={} count={} limit={}",
        scope,
        normalize(account),
        ip,
        count,
        loginPerMinute
      );
      throw LoginRateLimitException.tooManyRequests(account);
    }
  }

  public void recordLoginFailure(String account) {
    String key = "sec:login:fail:" + clientIp() + ":" + normalize(account);
    long v = increment(key, Duration.ofMinutes(15));
    if (v > loginFailThreshold) {
      long lockSeconds = Math.min(900, (long) Math.pow(2, (v - loginFailThreshold)) * 5L);
      redisTemplate.expire(key, Duration.ofSeconds(lockSeconds));
    }
  }

  public void clearLoginFailures(String account) {
    redisTemplate.delete("sec:login:fail:" + clientIp() + ":" + normalize(account));
  }

  public void checkVerificationSendQuota(String principal, String channel) {
    String ip = clientIp();
    String p = normalize(principal);
    String day = LocalDate.now().toString();
    requireQuota("sec:send:" + channel + ":ip:min:" + ip, smsPerMinute, Duration.ofMinutes(1), "请求过于频繁，请稍后重试");
    requireQuota("sec:send:" + channel + ":principal:min:" + p, smsPerMinute, Duration.ofMinutes(1), "请求过于频繁，请稍后重试");
    requireQuota("sec:send:" + channel + ":ip:day:" + ip + ":" + day, smsPerDay, Duration.ofDays(2), "今日请求次数已达上限");
    requireQuota("sec:send:" + channel + ":principal:day:" + p + ":" + day, smsPerDay, Duration.ofDays(2), "今日请求次数已达上限");
  }

  public void checkUploadRequestQuota(long userId) {
    if (uploadRequestsPerMinute <= 0) return;
    requireQuota(
      "sec:upload:user:req:min:" + userId,
      uploadRequestsPerMinute,
      Duration.ofMinutes(1),
      "上传请求过于频繁，请稍后重试"
    );
  }

  public void checkUploadQuotaPreview(long userId, long bytes) {
    validateUploadBytes(bytes);
    String day = LocalDate.now().toString();
    if (uploadFilesPerDay > 0) {
      long currentFiles = getCount(uploadDailyFilesKey(userId, day));
      if (currentFiles + 1 > uploadFilesPerDay) {
        throw badRequest("今日上传文件数已达上限");
      }
    }
    if (uploadBytesPerDay > 0) {
      long currentBytes = getCount(uploadDailyBytesKey(userId, day));
      if (safeAdd(currentBytes, bytes) > uploadBytesPerDay) {
        throw badRequest("今日上传总流量已达上限");
      }
    }
  }

  public UploadQuotaLease acquireUploadQuota(long userId, long bytes) {
    validateUploadBytes(bytes);
    String day = LocalDate.now().toString();
    String filesKey = uploadDailyFilesKey(userId, day);
    String bytesKey = uploadDailyBytesKey(userId, day);

    long files = increment(filesKey, Duration.ofDays(2));
    long totalBytes = increment(bytesKey, bytes, Duration.ofDays(2));
    UploadQuotaLease lease = new UploadQuotaLease(filesKey, bytesKey, bytes);

    boolean exceedsFiles = uploadFilesPerDay > 0 && files > uploadFilesPerDay;
    boolean exceedsBytes = uploadBytesPerDay > 0 && totalBytes > uploadBytesPerDay;
    if (exceedsFiles || exceedsBytes) {
      releaseUploadQuota(lease);
      if (exceedsFiles) {
        throw badRequest("今日上传文件数已达上限");
      }
      throw badRequest("今日上传总流量已达上限");
    }
    return lease;
  }

  public void releaseUploadQuota(UploadQuotaLease lease) {
    if (lease == null || lease.released) return;
    try {
      redisTemplate.opsForValue().increment(lease.filesKey, -1L);
      redisTemplate.opsForValue().increment(lease.bytesKey, -lease.bytes);
    } catch (Exception releaseException) {
      log.warn("释放上传配额失败，filesKey={}, bytesKey={}", lease.filesKey, lease.bytesKey, releaseException);
    } finally {
      lease.released = true;
    }
  }

  private void requireQuota(String key, int limit, Duration window, String message) {
    if (limit <= 0) return;
    long count = increment(key, window);
    if (count > limit) {
      throw badRequest(message);
    }
  }

  private long increment(String key, Duration ttl) {
    Long value = redisTemplate.opsForValue().increment(key);
    if (value != null && value == 1L) {
      redisTemplate.expire(key, ttl);
    }
    return value == null ? 0L : value;
  }

  private long increment(String key, long delta, Duration ttl) {
    Long value = redisTemplate.opsForValue().increment(key, delta);
    if (value != null && value == delta) {
      redisTemplate.expire(key, ttl);
    }
    return value == null ? 0L : value;
  }

  private long getCount(String key) {
    Object v = redisTemplate.opsForValue().get(key);
    if (v == null) return 0L;
    try {
      return Long.parseLong(String.valueOf(v));
    } catch (Exception parseException) {
      log.debug("限流计数解析失败，key={}, rawValue={}", key, v, parseException);
      return 0L;
    }
  }

  private String clientIp() {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) return realIp.trim();
    String remoteAddr = request.getRemoteAddr();
    return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }

  private void validateUploadBytes(long bytes) {
    if (bytes <= 0) {
      throw badRequest("上传文件大小无效");
    }
  }

  private long safeAdd(long a, long b) {
    if (b > 0 && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
    if (b < 0 && a < Long.MIN_VALUE - b) return Long.MIN_VALUE;
    return a + b;
  }

  private String uploadDailyFilesKey(long userId, String day) {
    return "sec:upload:user:files:day:" + userId + ":" + day;
  }

  private String uploadDailyBytesKey(long userId, String day) {
    return "sec:upload:user:bytes:day:" + userId + ":" + day;
  }

  public static class UploadQuotaLease {
    private final String filesKey;
    private final String bytesKey;
    private final long bytes;
    private boolean released;

    private UploadQuotaLease(String filesKey, String bytesKey, long bytes) {
      this.filesKey = filesKey;
      this.bytesKey = bytesKey;
      this.bytes = bytes;
      this.released = false;
    }
  }
}
