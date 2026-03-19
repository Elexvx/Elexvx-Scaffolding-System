package top.elexvx.admin.controller;

import top.elexvx.admin.dto.RoleUpsertRequest;
import top.elexvx.admin.annotation.PagePerm;
import top.elexvx.admin.annotation.RepeatSubmit;
import top.elexvx.admin.service.RoleAdminService;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.RoleResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/role")
public class SystemRoleController {
  private final RoleAdminService roleAdminService;

  public SystemRoleController(RoleAdminService roleAdminService) {
    this.roleAdminService = roleAdminService;
  }

  @GetMapping("/list")
  @PagePerm(routeName = "SystemRole", action = "query")
  public ApiResponse<List<RoleResponse>> list() {
    return ApiResponse.success(roleAdminService.list());
  }

  @GetMapping("/{id}")
  @PagePerm(routeName = "SystemRole", action = "query")
  public ApiResponse<RoleResponse> get(@PathVariable long id) {
    return ApiResponse.success(roleAdminService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  @PagePerm(routeName = "SystemRole", action = "create")
  public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleUpsertRequest req) {
    return ApiResponse.success(roleAdminService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemRole", action = "update")
  public ApiResponse<RoleResponse> update(@PathVariable long id, @RequestBody @Valid RoleUpsertRequest req) {
    return ApiResponse.success(roleAdminService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemRole", action = "delete")
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    return ApiResponse.success(roleAdminService.delete(id));
  }
}
