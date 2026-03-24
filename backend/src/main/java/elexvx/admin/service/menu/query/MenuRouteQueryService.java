package elexvx.admin.service.menu.query;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.mapper.MenuItemMapper;
import elexvx.admin.service.menu.support.MenuPermissionResolver;
import elexvx.admin.service.menu.support.MenuRouteAssembler;
import elexvx.admin.vo.RouteItem;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuRouteQueryService {
  private final MenuItemMapper menuItemMapper;
  private final MenuPermissionResolver menuPermissionResolver;
  private final MenuRouteAssembler menuRouteAssembler;

  public MenuRouteQueryService(
    MenuItemMapper menuItemMapper,
    MenuPermissionResolver menuPermissionResolver,
    MenuRouteAssembler menuRouteAssembler
  ) {
    this.menuItemMapper = menuItemMapper;
    this.menuPermissionResolver = menuPermissionResolver;
    this.menuRouteAssembler = menuRouteAssembler;
  }

  public List<RouteItem> getMenuRoutesForCurrentUser() {
    List<MenuItemEntity> items = menuItemMapper.selectAllEnabled();
    List<MenuItemEntity> accessible = menuPermissionResolver.filterAccessible(items);
    return menuRouteAssembler.pruneEmptyLayoutRoutes(menuRouteAssembler.toRouteTree(accessible));
  }
}
