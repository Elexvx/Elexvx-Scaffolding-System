package elexvx.admin.controller;

import elexvx.admin.service.menu.MenuFacadeService;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.MenuListResult;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController {
  private static final Logger log = LoggerFactory.getLogger(MenuController.class);
  private final MenuFacadeService menuFacadeService;

  public MenuController(MenuFacadeService menuFacadeService) {
    this.menuFacadeService = menuFacadeService;
  }

  @GetMapping("/get-menu-list-i18n")
  public ApiResponse<MenuListResult> getMenuList() {
    try {
      if (menuFacadeService.isConfigured()) {
        return ApiResponse.success(new MenuListResult(menuFacadeService.getMenuRoutesForCurrentUser()));
      }
    } catch (Exception loadException) {
      log.warn("加载菜单失败，返回空菜单", loadException);
    }
    return ApiResponse.success(new MenuListResult(new ArrayList<>()));
  }
}
