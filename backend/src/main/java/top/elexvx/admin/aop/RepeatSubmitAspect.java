package top.elexvx.admin.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.elexvx.admin.annotation.RepeatSubmit;
import top.elexvx.admin.exception.RepeatSubmitException;
import top.elexvx.admin.security.AuthContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
public class RepeatSubmitAspect {
  private static final long CLEANUP_EVERY = 500;
  private static final long STALE_TIME_MS = 24 * 60 * 60 * 1000L;

  private final ObjectMapper objectMapper;
  private final AuthContext authContext;
  private final Map<String, SubmitRecord> cache = new ConcurrentHashMap<>();
  private final AtomicLong counter = new AtomicLong();

  public RepeatSubmitAspect(ObjectMapper objectMapper, AuthContext authContext) {
    this.objectMapper = objectMapper;
    this.authContext = authContext;
  }

  @Around("@annotation(repeatSubmit)")
  public Object around(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
    int interval = Math.max(0, repeatSubmit.interval());
    if (interval <= 0) {
      return point.proceed();
    }
    String key = buildKey(point);
    long now = System.currentTimeMillis();
    AtomicBoolean repeat = new AtomicBoolean(false);
    cache.compute(
      key,
      (k, old) -> {
        if (old != null && (now - old.timestamp) < interval) {
          repeat.set(true);
          return old;
        }
        return new SubmitRecord(now);
      }
    );
    if (repeat.get()) {
      throw new RepeatSubmitException(repeatSubmit.message());
    }
    cleanupIfNeeded(now);
    return point.proceed();
  }

  private void cleanupIfNeeded(long now) {
    long count = counter.incrementAndGet();
    if (count % CLEANUP_EVERY != 0) return;
    cache.entrySet().removeIf(entry -> (now - entry.getValue().timestamp) > STALE_TIME_MS);
  }

  private String buildKey(ProceedingJoinPoint point) {
    MethodSignature signature = (MethodSignature) point.getSignature();
    HttpServletRequest request = resolveRequest();
    String method = request != null ? request.getMethod() : "";
    String uri = request != null ? request.getRequestURI() : "";
    String query = request != null && request.getQueryString() != null ? request.getQueryString() : "";
    String userKey = resolveUserKey(request);
    String argsHash = hashArgs(point.getArgs());
    String raw = signature.toShortString() + "|" + method + "|" + uri + "|" + query + "|" + userKey + "|" + argsHash;
    return sha256(raw);
  }

  private HttpServletRequest resolveRequest() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs == null ? null : attrs.getRequest();
  }

  private String resolveUserKey(HttpServletRequest request) {
    if (authContext.isAuthenticated()) {
      return "uid:" + authContext.requireUserId();
    }
    String token = authContext.getToken();
    if (token != null && !token.isBlank()) {
      return "token:" + token;
    }
    if (request != null) {
      String ip = resolveClientIp(request);
      if (ip != null && !ip.isBlank()) {
        return "ip:" + ip;
      }
    }
    return "anonymous";
  }

  private String resolveClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if (ip == null) return null;
    if (ip.contains(",")) ip = ip.split(",")[0].trim();
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }
    return ip;
  }

  private String hashArgs(Object[] args) {
    if (args == null || args.length == 0) return "";
    List<Object> filtered = new ArrayList<>();
    for (Object arg : args) {
      if (arg == null || shouldIgnoreArg(arg)) continue;
      filtered.add(arg);
    }
    if (filtered.isEmpty()) return "";
    try {
      return sha256(objectMapper.writeValueAsString(filtered));
    } catch (Exception e) {
      return sha256(filtered.toString());
    }
  }

  private boolean shouldIgnoreArg(Object arg) {
    if (arg instanceof ServletRequest || arg instanceof ServletResponse) return true;
    if (arg instanceof BindingResult) return true;
    if (arg instanceof InputStream || arg instanceof OutputStream || arg instanceof Reader || arg instanceof Writer) return true;
    if (arg instanceof MultipartFile) return true;
    if (arg.getClass().isArray() && MultipartFile.class.isAssignableFrom(arg.getClass().getComponentType())) return true;
    return false;
  }

  private String sha256(String input) {
    if (input == null) return "";
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      return Integer.toHexString(input.hashCode());
    }
  }

  private static class SubmitRecord {
    private final long timestamp;

    private SubmitRecord(long timestamp) {
      this.timestamp = timestamp;
    }
  }
}
