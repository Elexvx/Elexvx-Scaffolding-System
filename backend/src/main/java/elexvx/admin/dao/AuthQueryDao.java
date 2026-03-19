package elexvx.admin.dao;

import elexvx.admin.dto.UserIdStringValue;
import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.mapper.AuthQueryMapper;
import elexvx.admin.mapper.MenuItemMapper;
import elexvx.admin.service.PermissionCodeService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuthQueryDao {
  private final AuthQueryMapper mapper;
  private final MenuItemMapper menuItemMapper;
  private final PermissionCodeService permissionCodeService;

  public AuthQueryDao(AuthQueryMapper mapper, MenuItemMapper menuItemMapper, PermissionCodeService permissionCodeService) {
    this.mapper = mapper;
    this.menuItemMapper = menuItemMapper;
    this.permissionCodeService = permissionCodeService;
  }

  @Transactional
  public void deleteRoleMenusByMenuIds(Collection<Long> menuIds) {
    if (menuIds == null || menuIds.isEmpty()) return;
    mapper.deleteRoleMenusByMenuIds(menuIds);
  }

  public List<String> findRoleNamesByUserId(long userId) {
    return mapper.findRoleNamesByUserId(userId);
  }

  public List<UserIdStringValue> findRoleNamesByUserIds(Collection<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) return Collections.emptyList();
    return mapper.findRoleNamesByUserIds(userIds);
  }

  public List<String> findPermissionsByUserId(long userId) {
    return mapper.findPermissionsByUserId(userId);
  }

  public List<String> findPermissionsByRoleNames(Collection<String> roleNames) {
    if (roleNames == null || roleNames.isEmpty()) return Collections.emptyList();
    return mapper.findPermissionsByRoleNames(roleNames);
  }

  public List<String> findPermissionsByRoleId(long roleId) {
    return mapper.findPermissionsByRoleId(roleId);
  }

  public List<Long> findMenuIdsByRoleId(long roleId) {
    return mapper.findMenuIdsByRoleId(roleId);
  }

  public List<Long> findMenuIdsByUserId(long userId) {
    return mapper.findMenuIdsByUserId(userId);
  }

  public List<Long> findMenuIdsByRoleNames(Collection<String> roleNames) {
    if (roleNames == null || roleNames.isEmpty()) return Collections.emptyList();
    return mapper.findMenuIdsByRoleNames(roleNames);
  }

  public List<String> findExistingRoleNames(Collection<String> roleNames) {
    if (roleNames == null || roleNames.isEmpty()) return Collections.emptyList();
    return mapper.findExistingRoleNames(roleNames);
  }

  @Transactional
  public void replaceUserRoles(long userId, List<String> roleNames) {
    mapper.deleteUserRoles(userId);
    List<String> normalized = filterStrings(roleNames);
    if (!normalized.isEmpty()) {
      mapper.insertUserRoles(userId, normalized);
    }
  }

  @Transactional
  public void replaceRolePermissions(long roleId, List<String> permissions) {
    mapper.deleteRolePermissions(roleId);
    List<String> normalized = filterStrings(permissions);
    if (!normalized.isEmpty()) {
      mapper.insertRolePermissions(roleId, normalized);
    }
  }

  @Transactional
  public void replaceRoleMenus(long roleId, List<Long> menuIds) {
    mapper.deleteRoleMenus(roleId);
    List<Long> normalized = filterLongs(menuIds);
    if (!normalized.isEmpty()) {
      mapper.insertRoleMenus(roleId, normalized);
    }
  }

  @Transactional
  public void ensureAdminHasAllMenus() {
    // 1. ensure admin has all menus
    mapper.insertMissingAdminMenus();

    // 2. ensure admin has all permissions derived from menus
    Long roleId = mapper.selectRoleIdByName("admin");
    if (roleId == null) return;

    List<MenuItemEntity> menus = menuItemMapper.selectAllEnabled();
    Set<String> permissions = new HashSet<>();
    for (MenuItemEntity menu : menus) {
      String resource = resolveResource(menu);
      if (resource == null || resource.isBlank()) continue;
      List<String> actions = permissionCodeService.parseActions(menu.getActions());
      if (actions.isEmpty()) actions = List.of("create", "update", "delete", "query");
      for (String action : actions) {
        String permissionCode = permissionCodeService.buildPermissionCode(resource, action);
        if (permissionCode != null) {
          permissions.add(permissionCode);
        }
      }
    }

    for (String perm : permissions) {
      if (mapper.countRolePermission(roleId, perm) == 0) {
        mapper.insertRolePermission(roleId, perm);
      }
    }
  }

  private List<String> filterStrings(List<String> values) {
    if (values == null) return Collections.emptyList();
    List<String> list = new ArrayList<>();
    for (String v : values) {
      if (v == null) continue;
      String t = v.trim();
      if (!t.isEmpty()) list.add(t);
    }
    return list;
  }

  private List<Long> filterLongs(List<Long> values) {
    if (values == null) return Collections.emptyList();
    List<Long> list = new ArrayList<>();
    for (Long v : values) {
      if (v != null) list.add(v);
    }
    return list;
  }

  private String resolveResource(MenuItemEntity menu) {
    if (menu == null) return null;
    String routeName = menu.getRouteName();
    if (routeName != null && !routeName.isBlank()) return routeName;
    String path = menu.getPath();
    if (path == null || path.isBlank()) return null;
    String[] segs = path.split("/");
    for (int i = segs.length - 1; i >= 0; i--) {
      if (segs[i] != null && !segs[i].isBlank()) return segs[i];
    }
    return null;
  }
}
