package elexvx.admin.controller;

import elexvx.admin.dto.MenuItemCreateRequest;
import elexvx.admin.dto.MenuItemReorderRequest;
import elexvx.admin.dto.MenuItemUpdateRequest;
import elexvx.admin.annotation.PagePerm;
import elexvx.admin.annotation.RepeatSubmit;
import elexvx.admin.service.menu.MenuFacadeService;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.MenuItemTreeNode;
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
  private final MenuFacadeService menuFacadeService;

  public SystemMenuController(MenuFacadeService menuFacadeService) {
    this.menuFacadeService = menuFacadeService;
  }

  @GetMapping("/tree")
  @PagePerm(routeName = "SystemMenu", action = "query")
  public ApiResponse<List<MenuItemTreeNode>> tree() {
    return ApiResponse.success(menuFacadeService.getAdminTree());
  }

  @PostMapping
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "create")
  public ApiResponse<MenuItemTreeNode> create(@RequestBody @Valid MenuItemCreateRequest req) {
    return ApiResponse.success(menuFacadeService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "update")
  public ApiResponse<MenuItemTreeNode> update(@PathVariable long id, @RequestBody @Valid MenuItemUpdateRequest req) {
    return ApiResponse.success(menuFacadeService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "delete")
  public ApiResponse<Boolean> delete(@PathVariable long id, @RequestParam(defaultValue = "false") boolean cascade) {
    return ApiResponse.success(menuFacadeService.delete(id, cascade));
  }

  @PutMapping("/reorder")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "update")
  public ApiResponse<Boolean> reorder(@RequestBody @Valid MenuItemReorderRequest req) {
    return ApiResponse.success(menuFacadeService.reorder(req));
  }

  @PostMapping("/seed-default")
  @RepeatSubmit
  @PagePerm(routeName = "SystemMenu", action = "create")
  public ApiResponse<Integer> seedDefault(@RequestParam(defaultValue = "false") boolean overwrite) {
    return ApiResponse.success(menuFacadeService.seedDefaultSidebarMenus(overwrite));
  }
}
