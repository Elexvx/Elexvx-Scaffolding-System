package top.elexvx.admin.security;

import top.elexvx.admin.service.PermissionFacade;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class PermissionCache {
  private final AuthContext authContext;
  private final PermissionFacade permissionFacade;
  private Long cachedUserId;
  private Set<String> roleSet;
  private Set<String> permissionSet;
  private Boolean admin;

  public PermissionCache(AuthContext authContext, PermissionFacade permissionFacade) {
    this.authContext = authContext;
    this.permissionFacade = permissionFacade;
  }

  public boolean isAdmin() {
    ensureLoaded();
    return Boolean.TRUE.equals(admin);
  }

  public boolean hasRole(String role) {
    if (role == null || role.isBlank()) return false;
    ensureLoaded();
    return roleSet.stream().anyMatch(r -> r.equalsIgnoreCase(role));
  }

  public boolean hasPermission(String permission) {
    if (permission == null || permission.isBlank()) return false;
    ensureLoaded();
    return permissionSet.contains(permission);
  }

  public Set<String> getPermissions() {
    ensureLoaded();
    return permissionSet;
  }

  private void ensureLoaded() {
    long userId = authContext.requireUserId();
    if (cachedUserId != null && cachedUserId == userId) return;
    cachedUserId = userId;
    List<String> roles = permissionFacade.getEffectiveRoles(userId);
    List<String> permissions = permissionFacade.getEffectivePermissions(userId);
    roleSet = new HashSet<>(roles);
    permissionSet = new HashSet<>(permissions);
    admin = permissionFacade.isAdminAccount(userId);
  }
}
