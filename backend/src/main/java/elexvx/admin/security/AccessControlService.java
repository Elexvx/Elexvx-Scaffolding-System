package elexvx.admin.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
  private final PermissionCache permissionCache;

  public AccessControlService(PermissionCache permissionCache) {
    this.permissionCache = permissionCache;
  }

  public boolean hasRole(String role) {
    if (role == null || role.isBlank()) return false;
    if (permissionCache.isAdmin()) return true;
    return permissionCache.hasRole(role);
  }

  public boolean hasPermission(String permission) {
    if (permission == null || permission.isBlank()) return false;
    if (permissionCache.isAdmin()) return true;
    return permissionCache.hasPermission(permission);
  }

  public void checkRole(String role) {
    if (!hasRole(role)) {
      throw new AccessDeniedException("权限不足，请联系管理员开通");
    }
  }

  public void checkPermission(String permission) {
    if (!hasPermission(permission)) {
      throw new AccessDeniedException("权限不足，请联系管理员开通");
    }
  }
}
