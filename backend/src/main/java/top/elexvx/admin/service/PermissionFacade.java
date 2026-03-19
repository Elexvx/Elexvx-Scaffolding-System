package top.elexvx.admin.service;

import top.elexvx.admin.dao.AuthQueryDao;
import top.elexvx.admin.exception.BusinessException;
import top.elexvx.admin.exception.ErrorCodes;
import top.elexvx.admin.security.AuthContext;
import top.elexvx.admin.security.AuthSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PermissionFacade {
  private static final String KEY_ASSUMED_ROLES = "assumedRoles";
  private static final String KEY_ASSUMED_PERMISSIONS = "assumedPermissions";

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  private final AuthQueryDao authDao;
  private final AuthTokenService authTokenService;
  private final AuthContext authContext;

  public PermissionFacade(AuthQueryDao authDao, AuthTokenService authTokenService, AuthContext authContext) {
    this.authDao = authDao;
    this.authTokenService = authTokenService;
    this.authContext = authContext;
  }

  public List<String> getAssignedRoles(long userId) {
    return authDao.findRoleNamesByUserId(userId);
  }

  public List<String> getEffectiveRoles(long userId) {
    List<String> override = getAssumedRoles(userId);
    return override.isEmpty() ? authDao.findRoleNamesByUserId(userId) : override;
  }

  public List<String> getEffectivePermissions(long userId) {
    List<String> assumedRoles = getAssumedRoles(userId);
    if (!assumedRoles.isEmpty()) {
      List<String> assumedPermissions = getAssumedPermissions(userId);
      if (!assumedPermissions.isEmpty()) return assumedPermissions;
      return authDao.findPermissionsByRoleNames(assumedRoles);
    }
    return authDao.findPermissionsByUserId(userId);
  }

  public List<Long> getAccessibleMenuIds(long userId) {
    List<String> override = getAssumedRoles(userId);
    if (!override.isEmpty()) {
      return authDao.findMenuIdsByRoleNames(override);
    }
    return authDao.findMenuIdsByUserId(userId);
  }

  public boolean isAdminAccount(long userId) {
    return getAssignedRoles(userId).stream().anyMatch(r -> "admin".equalsIgnoreCase(r));
  }

  public boolean isAssumed(long userId) {
    return !getAssumedRoles(userId).isEmpty();
  }

  public List<String> assumeRoles(long userId, List<String> roleNames) {
    List<String> normalized = normalizeRoles(roleNames);
    if (normalized.isEmpty()) throw badRequest("请选择要切换的角色");

    List<String> existing = authDao.findExistingRoleNames(normalized);
    if (existing.size() != normalized.size()) {
      Set<String> missing = new HashSet<>(normalized);
      missing.removeAll(existing);
      throw badRequest("以下角色不存在: " + String.join(", ", missing));
    }
    List<String> permissions = authDao.findPermissionsByRoleNames(normalized);
    AuthSession session = getCurrentSession(userId);
    session.getAttributes().put(KEY_ASSUMED_ROLES, normalized);
    session.getAttributes().put(KEY_ASSUMED_PERMISSIONS, permissions);
    authTokenService.updateSession(authContext.requireToken(), session);
    return normalized;
  }

  public void clearAssumedRoles(long userId) {
    AuthSession session = getCurrentSessionIfExists(userId);
    if (session == null) return;
    session.getAttributes().remove(KEY_ASSUMED_ROLES);
    session.getAttributes().remove(KEY_ASSUMED_PERMISSIONS);
    authTokenService.updateSession(authContext.requireToken(), session);
  }

  private List<String> getAssumedRoles(long userId) {
    AuthSession session = getCurrentSessionIfExists(userId);
    if (session == null) return Collections.emptyList();
    Object value = session.getAttributes().get(KEY_ASSUMED_ROLES);
    if (value instanceof List<?>) {
      List<?> raw = (List<?>) value;
      List<String> list = new ArrayList<>();
      raw.forEach(it -> {
        if (it != null) list.add(String.valueOf(it));
      });
      return normalizeRoles(list);
    }
    return Collections.emptyList();
  }

  private List<String> getAssumedPermissions(long userId) {
    AuthSession session = getCurrentSessionIfExists(userId);
    if (session == null) return Collections.emptyList();
    Object value = session.getAttributes().get(KEY_ASSUMED_PERMISSIONS);
    if (value instanceof List<?>) {
      List<String> list = new ArrayList<>();
      for (Object it : (List<?>) value) {
        if (it != null) list.add(String.valueOf(it));
      }
      return list;
    }
    return Collections.emptyList();
  }

  private List<String> normalizeRoles(List<String> roleNames) {
    if (roleNames == null) return Collections.emptyList();
    Set<String> set = new HashSet<>();
    for (String r : roleNames) {
      if (r == null) continue;
      String v = r.trim();
      if (v.isEmpty()) continue;
      set.add(v);
    }
    List<String> list = new ArrayList<>();
    for (String r : set) list.add(r);
    list.sort(String.CASE_INSENSITIVE_ORDER);
    return list;
  }

  private AuthSession getCurrentSession(long userId) {
    String token = authContext.requireToken();
    AuthSession session = authTokenService.getSession(token);
    if (session == null || session.getUserId() != userId) {
      throw badRequest("登录已失效，请重新登录");
    }
    return session;
  }

  private AuthSession getCurrentSessionIfExists(long userId) {
    String token = authContext.getToken();
    if (token == null) return null;
    AuthSession session = authTokenService.getSession(token);
    if (session == null || session.getUserId() != userId) return null;
    return session;
  }
}
