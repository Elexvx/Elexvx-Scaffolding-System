package elexvx.admin.service.auth;

import elexvx.admin.entity.UserEntity;
import elexvx.admin.security.AuthSession;
import elexvx.admin.service.AuthTokenService;
import elexvx.admin.service.ConcurrentLoginService;
import elexvx.admin.service.SecuritySettingService;
import elexvx.admin.service.UiSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConcurrentLoginOrchestrator {
  private final UiSettingService uiSettingService;
  private final ConcurrentLoginService concurrentLoginService;
  private final AuthTokenService authTokenService;
  private final SecuritySettingService securitySettingService;

  public ConcurrentLoginOrchestrator(
    UiSettingService uiSettingService,
    ConcurrentLoginService concurrentLoginService,
    AuthTokenService authTokenService,
    SecuritySettingService securitySettingService
  ) {
    this.uiSettingService = uiSettingService;
    this.concurrentLoginService = concurrentLoginService;
    this.authTokenService = authTokenService;
    this.securitySettingService = securitySettingService;
  }

  public DeviceSnapshot buildDeviceSnapshot(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    String browser = parseBrowser(userAgent);
    String os = parseOS(userAgent);
    String deviceModel = parseDeviceModel(userAgent);
    String ipAddress = getClientIp(request);
    String loginLocation = getLocationByIp(ipAddress);
    return new DeviceSnapshot(deviceModel, os, browser, ipAddress, loginLocation);
  }

  public LoginDecision resolveLoginDecision(UserEntity user, Boolean force, DeviceSnapshot snapshot) {
    if (!uiSettingService.isMultiDeviceLoginAllowed() && hasActiveSession(user.getId())) {
      if (Boolean.TRUE.equals(force)) {
        concurrentLoginService.publishForceLogout(user.getId(), "当前登录状态已失效，请重新登录");
        authTokenService.removeUserTokens(user.getId());
      } else {
        String deviceInfo = buildDeviceInfo(snapshot.deviceModel(), snapshot.os(), snapshot.browser());
        ConcurrentLoginService.PendingLogin pending = concurrentLoginService.createPending(
          user.getId(),
          snapshot.deviceModel(),
          snapshot.os(),
          snapshot.browser(),
          deviceInfo,
          snapshot.ipAddress(),
          snapshot.loginLocation()
        );
        return LoginDecision.pending(pending.getRequestId(), pending.getRequestKey());
      }
    }
    return LoginDecision.pass();
  }

  public long resolveTokenTimeoutSeconds() {
    Integer minutes = securitySettingService.getOrCreate().getTokenTimeoutMinutes();
    if (minutes != null && minutes > 0) {
      return minutes * 60L;
    }
    return 2592000L;
  }

  public AuthSession buildSession(UserEntity user, DeviceSnapshot snapshot) {
    AuthSession session = new AuthSession();
    session.setDeviceModel(snapshot.deviceModel());
    session.setOs(snapshot.os());
    session.setBrowser(snapshot.browser());
    session.setIpAddress(snapshot.ipAddress());
    session.setLoginLocation(snapshot.loginLocation());
    session.getAttributes().put("loginId", user.getId());
    session.getAttributes().put("userName", user.getName());
    session.getAttributes().put("account", user.getAccount());
    session.getAttributes().put("userGuid", user.getGuid());
    session.getAttributes().put("loginTime", System.currentTimeMillis());
    session.setDeviceId(buildDeviceInfo(snapshot.deviceModel(), snapshot.os(), snapshot.browser()));
    return session;
  }

  public DeviceSnapshot toSnapshot(ConcurrentLoginService.PendingLogin pending) {
    return new DeviceSnapshot(
      pending.getDeviceModel(),
      pending.getOs(),
      pending.getBrowser(),
      pending.getIpAddress(),
      pending.getLoginLocation()
    );
  }

  private boolean hasActiveSession(long userId) {
    List<String> tokens = authTokenService.listUserTokens(userId);
    long now = System.currentTimeMillis();
    long idleTimeoutMs = resolveSessionTimeoutMs();
    for (String token : tokens) {
      AuthSession session = authTokenService.getSession(token);
      if (session == null) {
        authTokenService.removeUserToken(userId, token);
        continue;
      }
      if (isSessionExpired(session, now, idleTimeoutMs)) {
        authTokenService.removeUserToken(userId, token);
        continue;
      }
      return true;
    }
    return false;
  }

  private boolean isSessionExpired(AuthSession session, long now, long idleTimeoutMs) {
    if (session.getExpiresAt() > 0 && session.getExpiresAt() <= now) {
      return true;
    }
    Long lastAccess = readLongAttribute(session, "lastAccessTime");
    if (lastAccess == null) {
      lastAccess = readLongAttribute(session, "loginTime");
    }
    return idleTimeoutMs > 0 && lastAccess != null && now - lastAccess > idleTimeoutMs;
  }

  private long resolveSessionTimeoutMs() {
    Integer minutes = securitySettingService.getOrCreate().getSessionTimeoutMinutes();
    if (minutes != null && minutes > 0) {
      return minutes * 60_000L;
    }
    return 0L;
  }

  private Long readLongAttribute(AuthSession session, String key) {
    Object value = session.getAttributes().get(key);
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    if (value instanceof String text) {
      try {
        return Long.parseLong(text);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }
    return ip;
  }

  private String getLocationByIp(String ip) {
    if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.equals("localhost") || ip.equals("127.0.0.1")) {
      return "Local network IP";
    }
    return "Unknown";
  }

  private String parseBrowser(String userAgent) {
    if (userAgent == null) {
      return "Unknown";
    }
    if (userAgent.contains("Edg")) {
      return "Edge";
    }
    if (userAgent.contains("Chrome")) {
      return "Chrome";
    }
    if (userAgent.contains("Firefox")) {
      return "Firefox";
    }
    if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
      return "Safari";
    }
    return "Unknown";
  }

  private String parseOS(String userAgent) {
    if (userAgent == null) {
      return "Unknown";
    }
    if (userAgent.contains("Windows NT 10")) {
      return "Windows 10";
    }
    if (userAgent.contains("Windows NT 6.3")) {
      return "Windows 8.1";
    }
    if (userAgent.contains("Windows NT 6.2")) {
      return "Windows 8";
    }
    if (userAgent.contains("Windows NT 6.1")) {
      return "Windows 7";
    }
    if (userAgent.contains("Windows")) {
      return "Windows";
    }
    if (userAgent.contains("Mac OS X")) {
      return "Mac OS";
    }
    if (userAgent.contains("Linux")) {
      return "Linux";
    }
    if (userAgent.contains("Android")) {
      return "Android";
    }
    if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
      return "iOS";
    }
    return "Unknown";
  }

  private String parseDeviceModel(String userAgent) {
    if (userAgent == null || userAgent.isBlank()) {
      return "Unknown Device";
    }
    if (userAgent.contains("Android")) {
      String model = extractAndroidModel(userAgent);
      return model != null ? model : "Android Device";
    }
    if (userAgent.contains("iPhone")) {
      return "iPhone";
    }
    if (userAgent.contains("iPad")) {
      return "iPad";
    }
    if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS X")) {
      return "Mac";
    }
    if (userAgent.contains("Windows")) {
      return "Windows Device";
    }
    if (userAgent.contains("Linux")) {
      return "Linux Device";
    }
    return "Unknown Device";
  }

  private String extractAndroidModel(String userAgent) {
    int androidIndex = userAgent.indexOf("Android");
    if (androidIndex < 0) {
      return null;
    }
    int firstSemi = userAgent.indexOf(';', androidIndex);
    if (firstSemi < 0) {
      return null;
    }
    int secondSemi = userAgent.indexOf(';', firstSemi + 1);
    int endParen = userAgent.indexOf(')', firstSemi + 1);
    int endIndex = secondSemi;
    if (endIndex == -1 || (endParen != -1 && endParen < endIndex)) {
      endIndex = endParen;
    }
    if (endIndex == -1) {
      return null;
    }
    String model = userAgent.substring(firstSemi + 1, endIndex).trim();
    if (model.isEmpty()) {
      return null;
    }
    int buildIndex = model.indexOf("Build/");
    if (buildIndex > 0) {
      model = model.substring(0, buildIndex).trim();
    }
    return model.isEmpty() ? null : model;
  }

  private String buildDeviceInfo(String deviceModel, String os, String browser) {
    String safeDevice = (deviceModel == null || deviceModel.isBlank()) ? "Unknown Device" : deviceModel;
    String safeOs = (os == null || os.isBlank()) ? "Unknown OS" : os;
    String safeBrowser = (browser == null || browser.isBlank()) ? "Unknown Browser" : browser;
    return safeDevice + " / " + safeOs + " / " + safeBrowser;
  }

  public record DeviceSnapshot(String deviceModel, String os, String browser, String ipAddress, String loginLocation) {}

  public static final class LoginDecision {
    private final boolean pending;
    private final String requestId;
    private final String requestKey;

    private LoginDecision(boolean pending, String requestId, String requestKey) {
      this.pending = pending;
      this.requestId = requestId;
      this.requestKey = requestKey;
    }

    public static LoginDecision pass() {
      return new LoginDecision(false, null, null);
    }

    public static LoginDecision pending(String requestId, String requestKey) {
      return new LoginDecision(true, requestId, requestKey);
    }

    public boolean isPending() {
      return pending;
    }

    public String getRequestId() {
      return requestId;
    }

    public String getRequestKey() {
      return requestKey;
    }
  }
}
