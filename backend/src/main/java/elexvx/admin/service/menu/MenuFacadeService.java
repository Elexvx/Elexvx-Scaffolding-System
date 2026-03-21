package elexvx.admin.service.menu;

import elexvx.admin.dto.MenuItemCreateRequest;
import elexvx.admin.dto.MenuItemReorderRequest;
import elexvx.admin.dto.MenuItemUpdateRequest;
import elexvx.admin.service.menu.command.MenuMaintenanceService;
import elexvx.admin.service.menu.command.MenuMutationService;
import elexvx.admin.service.menu.query.MenuQueryService;
import elexvx.admin.service.menu.query.MenuRouteQueryService;
import elexvx.admin.vo.MenuItemTreeNode;
import elexvx.admin.vo.RouteItem;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuFacadeService {
  private final MenuQueryService menuQueryService;
  private final MenuRouteQueryService menuRouteQueryService;
  private final MenuMutationService menuMutationService;
  private final MenuMaintenanceService menuMaintenanceService;

  public MenuFacadeService(
    MenuQueryService menuQueryService,
    MenuRouteQueryService menuRouteQueryService,
    MenuMutationService menuMutationService,
    MenuMaintenanceService menuMaintenanceService
  ) {
    this.menuQueryService = menuQueryService;
    this.menuRouteQueryService = menuRouteQueryService;
    this.menuMutationService = menuMutationService;
    this.menuMaintenanceService = menuMaintenanceService;
  }

  public boolean isConfigured() {
    return menuQueryService.isConfigured();
  }

  public List<RouteItem> getMenuRoutesForCurrentUser() {
    return menuRouteQueryService.getMenuRoutesForCurrentUser();
  }

  public List<MenuItemTreeNode> getAdminTree() {
    return menuQueryService.getAdminTree();
  }

  public MenuItemTreeNode create(MenuItemCreateRequest req) {
    return menuMutationService.create(req);
  }

  public MenuItemTreeNode update(long id, MenuItemUpdateRequest req) {
    return menuMutationService.update(id, req);
  }

  public boolean delete(long id, boolean cascade) {
    return menuMutationService.delete(id, cascade);
  }

  public boolean reorder(MenuItemReorderRequest req) {
    return menuMutationService.reorder(req);
  }

  public int seedDefaultSidebarMenus(boolean overwriteExisting) {
    return menuMaintenanceService.seedDefaultSidebarMenus(overwriteExisting);
  }

  public boolean removeObsoleteWatermarkRoute() {
    return menuMaintenanceService.removeObsoleteWatermarkRoute();
  }

  public boolean removeObsoleteNotificationRoute() {
    return menuMaintenanceService.removeObsoleteNotificationRoute();
  }

  public boolean removeObsoleteTeamRoute() {
    return menuMaintenanceService.removeObsoleteTeamRoute();
  }

  public boolean removeObsoleteModuleAccessRoutes() {
    return menuMaintenanceService.removeObsoleteModuleAccessRoutes();
  }

  public boolean ensureOrgManagementMenuSeeded() {
    return menuMaintenanceService.ensureOrgManagementMenuSeeded();
  }
}
