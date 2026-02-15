package com.tencent.tdesign.controller;

import com.tencent.tdesign.dto.MenuItemCreateRequest;
import com.tencent.tdesign.dto.MenuItemReorderRequest;
import com.tencent.tdesign.dto.MenuItemUpdateRequest;
import com.tencent.tdesign.annotation.PagePerm;
import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.service.MenuItemService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.MenuItemTreeNode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/menu")
public class SystemMenuController {
  private final MenuItemService menuItemService;

  public SystemMenuController(MenuItemService menuItemService) {
    this.menuItemService = menuItemService;
  }

  @GetMapping("/tree")
  @PagePerm(routeName = "SystemMenu", action = "query")
  public ApiResponse<List<MenuItemTreeNode>> tree() {
    return ApiResponse.success(menuItemService.getAdminTree());
  }

  @PostMapping
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "create")
  public ApiResponse<MenuItemTreeNode> create(@RequestBody @Valid MenuItemCreateRequest req) {
    return ApiResponse.success(menuItemService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "update")
  public ApiResponse<MenuItemTreeNode> update(@PathVariable long id, @RequestBody @Valid MenuItemUpdateRequest req) {
    return ApiResponse.success(menuItemService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "delete")
  public ApiResponse<Boolean> delete(@PathVariable long id, @RequestParam(defaultValue = "false") boolean cascade) {
    return ApiResponse.success(menuItemService.delete(id, cascade));
  }

  @PutMapping("/reorder")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "update")
  public ApiResponse<Boolean> reorder(@RequestBody @Valid MenuItemReorderRequest req) {
    return ApiResponse.success(menuItemService.reorder(req));
  }

  @PostMapping("/seed-default")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "create")
  public ApiResponse<Integer> seedDefault(@RequestParam(defaultValue = "false") boolean overwrite) {
    return ApiResponse.success(menuItemService.seedDefaultSidebarMenus(overwrite));
  }
}
