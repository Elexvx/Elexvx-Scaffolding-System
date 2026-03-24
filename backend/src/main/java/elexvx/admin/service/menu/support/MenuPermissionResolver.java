package elexvx.admin.service.menu.support;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.security.AuthContext;
import elexvx.admin.service.PermissionFacade;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MenuPermissionResolver {
  private final PermissionFacade permissionFacade;
  private final AuthContext authContext;

  public MenuPermissionResolver(PermissionFacade permissionFacade, AuthContext authContext) {
    this.permissionFacade = permissionFacade;
    this.authContext = authContext;
  }

  public List<MenuItemEntity> filterAccessible(List<MenuItemEntity> items) {
    long userId = authContext.requireUserId();
    Set<Long> accessibleMenuIds = new HashSet<>(permissionFacade.getAccessibleMenuIds(userId));
    Map<Long, MenuItemEntity> idMap = new HashMap<>();
    for (MenuItemEntity e : items) {
      idMap.put(e.getId(), e);
    }
    Set<Long> visibleIds = new HashSet<>();
    for (MenuItemEntity e : items) {
      if (!Boolean.TRUE.equals(e.getEnabled())) {
        continue;
      }
      if (accessibleMenuIds.contains(e.getId())) {
        visibleIds.add(e.getId());
        Long pid = e.getParentId();
        while (pid != null) {
          if (visibleIds.contains(pid)) {
            break;
          }
          MenuItemEntity parent = idMap.get(pid);
          if (parent == null || !Boolean.TRUE.equals(parent.getEnabled())) {
            break;
          }
          visibleIds.add(pid);
          pid = parent.getParentId();
        }
      }
    }
    List<MenuItemEntity> list = new ArrayList<>();
    for (MenuItemEntity e : items) {
      if (visibleIds.contains(e.getId())) {
        list.add(e);
      }
    }
    return list;
  }
}
