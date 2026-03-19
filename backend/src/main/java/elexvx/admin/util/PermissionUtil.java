package elexvx.admin.util;

import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.security.AccessControlService;
import elexvx.admin.security.AuthContext;
import elexvx.admin.security.PermissionCache;
import elexvx.admin.service.PermissionFacade;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public final class PermissionUtil {
  private static AccessControlService accessControlService;
  private static PermissionFacade permissionFacade;
  private static AuthContext authContext;
  private static PermissionCache permissionCache;

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  @Autowired
  public PermissionUtil(
    AccessControlService accessControlService,
    PermissionFacade permissionFacade,
    AuthContext authContext,
    PermissionCache permissionCache
  ) {
    PermissionUtil.accessControlService = accessControlService;
    PermissionUtil.permissionFacade = permissionFacade;
    PermissionUtil.authContext = authContext;
    PermissionUtil.permissionCache = permissionCache;
  }

  public static void check(String permission) {
    ensureInitialized();
    if (permission == null || permission.isBlank()) {
      throw badRequest("permission 不能为空");
    }
    if (permissionCache.isAdmin()) return;
    accessControlService.checkPermission(permission);
  }

  public static void checkAny(String... permissions) {
    ensureInitialized();
    if (permissions == null || permissions.length == 0) {
      throw badRequest("permissions 不能为空");
    }
    if (permissionCache.isAdmin()) return;

    List<String> effective = List.copyOf(permissionCache.getPermissions());
    for (String p : permissions) {
      if (p == null || p.isBlank()) continue;
      if (effective.contains(p)) return;
    }
    throw new AccessDeniedException("权限不足，请联系管理员开通");
  }

  public static void checkAdmin() {
    ensureInitialized();
    if (!permissionCache.isAdmin()) {
      throw new AccessDeniedException("权限不足，请联系管理员开通");
    }
  }

  private static void ensureInitialized() {
    if (accessControlService == null || permissionFacade == null || authContext == null || permissionCache == null) {
      throw new IllegalStateException("PermissionUtil not initialized");
    }
  }
}
