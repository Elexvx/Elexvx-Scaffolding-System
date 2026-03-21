package elexvx.admin.service;

import elexvx.admin.service.menu.MenuFacadeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MenuMaintenanceRunner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(MenuMaintenanceRunner.class);

  private final MenuFacadeService menuFacadeService;
  private final boolean enabled;

  public MenuMaintenanceRunner(
    MenuFacadeService menuFacadeService,
    @Value("${elexvx.menu.maintenance.enabled:true}") boolean enabled
  ) {
    this.menuFacadeService = menuFacadeService;
    this.enabled = enabled;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!enabled) {
      log.info("Menu maintenance is disabled by config: elexvx.menu.maintenance.enabled=false");
      return;
    }

    runTask("ensureOrgManagementMenuSeeded", menuFacadeService::ensureOrgManagementMenuSeeded);
    runTask("removeObsoleteWatermarkRoute", menuFacadeService::removeObsoleteWatermarkRoute);
    runTask("removeObsoleteTeamRoute", menuFacadeService::removeObsoleteTeamRoute);
    runTask("removeObsoleteNotificationRoute", menuFacadeService::removeObsoleteNotificationRoute);
    runTask("removeObsoleteModuleAccessRoutes", menuFacadeService::removeObsoleteModuleAccessRoutes);
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
