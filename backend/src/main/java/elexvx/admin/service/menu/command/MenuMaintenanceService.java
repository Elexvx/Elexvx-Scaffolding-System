package elexvx.admin.service.menu.command;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.mapper.MenuItemMapper;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.menu.support.MenuSeedCatalog;
import elexvx.admin.service.menu.support.MenuSeedNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuMaintenanceService {
  private final MenuItemMapper menuItemMapper;
  private final MenuMutationService menuMutationService;
  private final OperationLogService operationLogService;

  public MenuMaintenanceService(
    MenuItemMapper menuItemMapper,
    MenuMutationService menuMutationService,
    OperationLogService operationLogService
  ) {
    this.menuItemMapper = menuItemMapper;
    this.menuMutationService = menuMutationService;
    this.operationLogService = operationLogService;
  }

  @Transactional
  public int seedDefaultSidebarMenus(boolean overwriteExisting) {
    List<MenuSeedNode> seeds = MenuSeedCatalog.defaults();
    Map<String, MenuItemEntity> created = new HashMap<>();
    for (MenuSeedNode seed : seeds) {
      if (seed.parentRouteName() != null) {
        continue;
      }
      MenuItemEntity e = menuMutationService.upsertSeed(seed, overwriteExisting);
      created.put(seed.routeName(), e);
    }
    for (MenuSeedNode seed : seeds) {
      if (seed.parentRouteName() == null) {
        continue;
      }
      MenuItemEntity parent = created.get(seed.parentRouteName());
      if (parent == null) {
        Optional<MenuItemEntity> parentFromDb = Optional.ofNullable(menuItemMapper.selectByRouteName(seed.parentRouteName()));
        if (parentFromDb.isEmpty()) {
          continue;
        }
        parent = parentFromDb.get();
        created.put(seed.parentRouteName(), parent);
      }
      MenuItemEntity e = menuMutationService.upsertSeed(seed.withParentId(parent.getId()), overwriteExisting);
      created.put(seed.routeName(), e);
    }
    int total = created.size();
    menuMutationService.ensureAdminOwnsAllMenus();
    operationLogService.log("UPDATE", "菜单管理", "初始化默认菜单: " + total + "项");
    return total;
  }

  @Transactional
  public boolean removeObsoleteWatermarkRoute() {
    MenuItemEntity obsolete = menuItemMapper.selectByRouteName("SystemWatermark");
    if (obsolete == null) {
      return false;
    }
    return menuMutationService.delete(obsolete.getId(), true);
  }

  @Transactional
  public boolean removeObsoleteNotificationRoute() {
    MenuItemEntity group = menuItemMapper.selectByRouteName("notification");
    if (group != null) {
      return menuMutationService.delete(group.getId(), true);
    }
    MenuItemEntity page = menuItemMapper.selectByRouteName("NotificationTable");
    if (page != null) {
      return menuMutationService.delete(page.getId(), true);
    }
    return false;
  }

  @Transactional
  public boolean removeObsoleteTeamRoute() {
    MenuItemEntity teamPage = menuItemMapper.selectByRouteName("SystemTeam");
    if (teamPage != null) {
      return menuMutationService.delete(teamPage.getId(), true);
    }
    MenuItemEntity teamRoute = menuItemMapper.selectByRouteName("team");
    if (teamRoute != null) {
      return menuMutationService.delete(teamRoute.getId(), true);
    }
    return false;
  }

  @Transactional
  public boolean removeObsoleteModuleAccessRoutes() {
    boolean changed = false;
    MenuItemEntity aiPage = menuItemMapper.selectByRouteName("SystemAi");
    if (aiPage != null) {
      menuMutationService.delete(aiPage.getId(), true);
      changed = true;
    }
    MenuItemEntity modulePage = menuItemMapper.selectByRouteName("SystemModule");
    if (modulePage != null) {
      menuMutationService.delete(modulePage.getId(), true);
      changed = true;
    }
    return changed;
  }

  @Transactional
  public boolean ensureOrgManagementMenuSeeded() {
    if (menuItemMapper.count() == 0) {
      return false;
    }
    if (menuItemMapper.selectByRouteName("SystemOrg") != null) {
      return false;
    }
    seedDefaultSidebarMenus(false);
    return true;
  }
}
