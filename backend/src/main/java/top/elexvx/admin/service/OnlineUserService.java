package top.elexvx.admin.service;

import top.elexvx.admin.security.AuthSession;
import top.elexvx.admin.vo.OnlineUserVO;
import top.elexvx.admin.vo.PageResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OnlineUserService {

  private static final Logger logger = LoggerFactory.getLogger(OnlineUserService.class);
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final AuthTokenService authTokenService;
  private final SecuritySettingService securitySettingService;

  public OnlineUserService(AuthTokenService authTokenService, SecuritySettingService securitySettingService) {
    this.authTokenService = authTokenService;
    this.securitySettingService = securitySettingService;
  }

  public PageResult<OnlineUserVO> getOnlineUsers(String loginAddress, String userName, int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.max(size, 1);
    boolean noFilter = (loginAddress == null || loginAddress.isEmpty()) && (userName == null || userName.isEmpty());
    List<String> tokenList = noFilter
      ? authTokenService.listActiveTokensByPage(safePage * safeSize, safeSize)
      : authTokenService.listAllTokens();
    List<OnlineUserVO> allUsers = new ArrayList<>();
    logger.debug("Found {} tokens in system", tokenList.size());

    long now = System.currentTimeMillis();
    long idleTimeoutMs = resolveSessionTimeoutMs();
    Map<String, AuthSession> sessionMap = authTokenService.getSessions(tokenList);
    for (String token : tokenList) {
      try {
        AuthSession session = sessionMap.get(token);
        if (session == null) {
          logger.debug("No login ID found for token: {}", token);
          authTokenService.removeToken(token);
          continue;
        }
        if (isSessionExpired(session, now, idleTimeoutMs)) {
          logger.debug("Session expired for token: {}", token);
          authTokenService.removeToken(token);
          continue;
        }

        String userNameStr = (String) session.getAttributes().get("userName");
        String ipAddr = session.getIpAddress();
        if (loginAddress != null && !loginAddress.isEmpty()) {
          if (ipAddr == null || !ipAddr.contains(loginAddress)) {
            continue;
          }
        }
        if (userName != null && !userName.isEmpty()) {
          if (userNameStr == null || !userNameStr.contains(userName)) {
            continue;
          }
        }
        allUsers.add(toVo(token, session));
        logger.debug("Added online user: {}", userNameStr);
      } catch (Exception e) {
        logger.warn("Error processing token {}: {}", token, e.getMessage());
      }
    }

    logger.info("Total online users found: {}", allUsers.size());
    PageResult<OnlineUserVO> result = new PageResult<>();
    if (noFilter) {
      result.setList(allUsers);
      result.setTotal(authTokenService.countActiveTokens());
      return result;
    }
    int start = safePage * safeSize;
    int end = Math.min(start + safeSize, allUsers.size());
    result.setList(start < allUsers.size() ? allUsers.subList(start, end) : new ArrayList<>());
    result.setTotal(allUsers.size());
    return result;
  }

  public boolean forceLogout(String sessionId) {
    try {
      authTokenService.removeToken(sessionId);
      return authTokenService.getSession(sessionId) == null;
    } catch (Exception e) {
      return false;
    }
  }

  public OnlineUserVO getOnlineUser(String sessionId) {
    try {
      AuthSession session = authTokenService.getSession(sessionId);
      if (session == null) return null;
      long now = System.currentTimeMillis();
      long idleTimeoutMs = resolveSessionTimeoutMs();
      if (isSessionExpired(session, now, idleTimeoutMs)) {
        authTokenService.removeToken(sessionId);
        return null;
      }

      String userNameStr = (String) session.getAttributes().get("userName");
      String account = (String) session.getAttributes().get("account");
      String ipAddr = session.getIpAddress();
      String location = session.getLoginLocation();
      String browser = session.getBrowser();
      String os = session.getOs();

      OnlineUserVO user = new OnlineUserVO();
      user.setSessionId(sessionId);
      user.setLoginName(account != null ? account : "unknown");
      user.setUserName(userNameStr != null ? userNameStr : "unknown");
      user.setIpAddress(ipAddr != null ? ipAddr : "");
      user.setLoginLocation(location != null ? location : "");
      user.setBrowser(browser != null ? browser : "Unknown");
      user.setOs(os != null ? os : "Unknown");
      Object loginTimeObj = session.getAttributes().get("loginTime");
      Long loginTimeMs = loginTimeObj instanceof Number ? ((Number) loginTimeObj).longValue() : null;
      user.setLoginTime(loginTimeMs != null ? dateFormat.format(new Date(loginTimeMs)) : "");
      return user;
    } catch (Exception e) {
      return null;
    }
  }

  private boolean isSessionExpired(AuthSession session, long now, long idleTimeoutMs) {
    if (session.getExpiresAt() > 0 && session.getExpiresAt() <= now) {
      return true;
    }
    Long lastAccess = readLongAttribute(session, "lastAccessTime");
    if (lastAccess == null) {
      lastAccess = readLongAttribute(session, "loginTime");
    }
    if (idleTimeoutMs > 0 && lastAccess != null && now - lastAccess > idleTimeoutMs) {
      return true;
    }
    return false;
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
    if (value instanceof String) {
      try {
        return Long.parseLong((String) value);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  private OnlineUserVO toVo(String sessionId, AuthSession session) {
    String userNameStr = (String) session.getAttributes().get("userName");
    String account = (String) session.getAttributes().get("account");
    String ipAddr = session.getIpAddress();
    String location = session.getLoginLocation();
    String browser = session.getBrowser();
    String os = session.getOs();
    Object loginTimeObj = session.getAttributes().get("loginTime");
    Long loginTimeMs = loginTimeObj instanceof Number ? ((Number) loginTimeObj).longValue() : null;

    OnlineUserVO user = new OnlineUserVO();
    user.setSessionId(sessionId);
    user.setLoginName(account != null ? account : "unknown");
    user.setUserName(userNameStr != null ? userNameStr : "unknown");
    user.setIpAddress(ipAddr != null ? ipAddr : "");
    user.setLoginLocation(location != null ? location : "");
    user.setBrowser(browser != null ? browser : "Unknown");
    user.setOs(os != null ? os : "Unknown");
    user.setLoginTime(loginTimeMs != null ? dateFormat.format(new Date(loginTimeMs)) : "");
    return user;
  }
}
