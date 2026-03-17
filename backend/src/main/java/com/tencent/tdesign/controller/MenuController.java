package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.MenuItemService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.MenuListResult;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController {
  private static final Logger log = LoggerFactory.getLogger(MenuController.class);
  private final MenuItemService menuItemService;

  public MenuController(MenuItemService menuItemService) {
    this.menuItemService = menuItemService;
  }

  @GetMapping("/get-menu-list-i18n")
  public ApiResponse<MenuListResult> getMenuList() {
    try {
      if (menuItemService.isConfigured()) {
        return ApiResponse.success(new MenuListResult(menuItemService.getMenuRoutesForCurrentUser()));
      }
    } catch (Exception loadException) {
      log.warn("加载菜单失败，返回空菜单", loadException);
    }
    return ApiResponse.success(new MenuListResult(new ArrayList<>()));
  }
}
