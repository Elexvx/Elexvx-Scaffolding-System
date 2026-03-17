package com.tencent.tdesign.service;

import com.tencent.tdesign.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserAdminServiceTest {

  @Test
  void rootAdminCanBeManagedBySelfWhenAllowed() {
    assertDoesNotThrow(() -> UserAdminService.validateManageableTarget(1L, 1L, "admin", true, true, true));
  }

  @Test
  void rootAdminCannotBeManagedByOtherAdminWhenNotAllowed() {
    assertThrows(
      BusinessException.class,
      () -> UserAdminService.validateManageableTarget(2L, 1L, "ADMIN", true, true, false)
    );
  }

  @Test
  void adminRoleUserCanBeManagedByAdmin() {
    assertDoesNotThrow(() -> UserAdminService.validateManageableTarget(2L, 1L, "alice", true, true, false));
  }

  @Test
  void adminRoleUserCannotBeManagedByNonAdmin() {
    assertThrows(
      BusinessException.class,
      () -> UserAdminService.validateManageableTarget(2L, 1L, "alice", true, false, false)
    );
  }

  @Test
  void selfManageAllowedWhenNotRootAdmin() {
    assertDoesNotThrow(() -> UserAdminService.validateManageableTarget(1L, 1L, "alice", true, false, false));
  }
}
