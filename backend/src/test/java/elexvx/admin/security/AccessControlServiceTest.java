package elexvx.admin.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessControlServiceTest {
  @Test
  void adminShouldPassRoleAndPermissionChecksWithoutSetLookup() {
    FakePermissionCache cache = new FakePermissionCache(true, false, false);

    AccessControlService service = new AccessControlService(cache);

    assertTrue(service.hasRole("any"));
    assertTrue(service.hasPermission("perm:x"));
    assertTrue(cache.adminCalls == 2);
    assertTrue(cache.roleCalls == 0);
    assertTrue(cache.permissionCalls == 0);
  }

  @Test
  void nonAdminShouldUseCacheContains() {
    FakePermissionCache cache = new FakePermissionCache(false, true, false);

    AccessControlService service = new AccessControlService(cache);

    assertTrue(service.hasRole("manager"));
    assertFalse(service.hasPermission("perm:a"));
    assertTrue(cache.adminCalls == 2);
    assertTrue(cache.roleCalls == 1);
    assertTrue(cache.permissionCalls == 1);
    assertTrue("manager".equals(cache.lastRole));
    assertTrue("perm:a".equals(cache.lastPermission));
  }

  private static final class FakePermissionCache extends PermissionCache {
    private int adminCalls;
    private int roleCalls;
    private int permissionCalls;
    private String lastRole;
    private String lastPermission;
    private final boolean adminResult;
    private final boolean roleResult;
    private final boolean permissionResult;

    private FakePermissionCache(boolean adminResult, boolean roleResult, boolean permissionResult) {
      super(new AuthContext(), new elexvx.admin.service.PermissionFacade(null, null, new AuthContext()));
      this.adminResult = adminResult;
      this.roleResult = roleResult;
      this.permissionResult = permissionResult;
    }

    @Override
    public boolean isAdmin() {
      adminCalls++;
      return adminResult;
    }

    @Override
    public boolean hasRole(String role) {
      roleCalls++;
      lastRole = role;
      return roleResult;
    }

    @Override
    public boolean hasPermission(String permission) {
      permissionCalls++;
      lastPermission = permission;
      return permissionResult;
    }
  }
}
