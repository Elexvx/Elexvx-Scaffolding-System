package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.PagePerm;
import com.tencent.tdesign.service.PermissionCatalogService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.PermissionCatalogItem;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/permission")
public class SystemPermissionController {
  private final PermissionCatalogService permissionCatalogService;

  public SystemPermissionController(PermissionCatalogService permissionCatalogService) {
    this.permissionCatalogService = permissionCatalogService;
  }

  /**
   * 获取权限目录（页面 + actions + permission code）。
   */
  @GetMapping("/catalog")
  @PagePerm(routeName = "SystemRole", action = "query")
  public ApiResponse<List<PermissionCatalogItem>> catalog() {
    return ApiResponse.success(permissionCatalogService.listCatalog());
  }
}
