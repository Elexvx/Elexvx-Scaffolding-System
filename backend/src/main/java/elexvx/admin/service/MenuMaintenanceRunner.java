package elexvx.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MenuMaintenanceRunner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(MenuMaintenanceRunner.class);

  private final MenuItemService menuItemService;
  private final boolean enabled;

  public MenuMaintenanceRunner(
    MenuItemService menuItemService,
    @Value("${elexvx.menu.maintenance.enabled:true}") boolean enabled
  ) {
    this.menuItemService = menuItemService;
    this.enabled = enabled;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!enabled) {
      log.info("Menu maintenance is disabled by config: elexvx.menu.maintenance.enabled=false");
      return;
    }

    runTask("ensureOrgManagementMenuSeeded", menuItemService::ensureOrgManagementMenuSeeded);
    runTask("removeObsoleteWatermarkRoute", menuItemService::removeObsoleteWatermarkRoute);
    runTask("removeObsoleteTeamRoute", menuItemService::removeObsoleteTeamRoute);
    runTask("removeObsoleteNotificationRoute", menuItemService::removeObsoleteNotificationRoute);
    runTask("removeObsoleteModuleAccessRoutes", menuItemService::removeObsoleteModuleAccessRoutes);
  }

  private void runTask(String name, MaintenanceTask task) {
    try {
      boolean changed = task.execute();
      log.info("Menu maintenance task '{}' done. changed={}", name, changed);
    } catch (Exception ex) {
      log.warn("Menu maintenance task '{}' failed: {}", name, ex.getMessage(), ex);
    }
  }

  @FunctionalInterface
  private interface MaintenanceTask {
    boolean execute();
  }
}
