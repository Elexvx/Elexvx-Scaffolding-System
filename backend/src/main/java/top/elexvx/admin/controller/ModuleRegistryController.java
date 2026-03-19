package top.elexvx.admin.controller;

import top.elexvx.admin.service.ModuleRegistryService;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.ModuleRegistryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/modules")
public class ModuleRegistryController {
  private static final String PERM_QUERY = "system:SystemModule:query";
  private static final String PERM_UPDATE = "system:SystemModule:update";

  private final ModuleRegistryService moduleRegistryService;

  public ModuleRegistryController(ModuleRegistryService moduleRegistryService) {
    this.moduleRegistryService = moduleRegistryService;
  }

  @GetMapping
  public ApiResponse<List<ModuleRegistryResponse>> list() {
    PermissionUtil.check(PERM_QUERY);
    return ApiResponse.success(moduleRegistryService.listRegistries());
  }

  @PostMapping("/{moduleKey}/enable")
  public ApiResponse<ModuleRegistryResponse> enable(@PathVariable String moduleKey) {
    PermissionUtil.check(PERM_UPDATE);
    return ApiResponse.success(moduleRegistryService.enableModule(moduleKey, true));
  }

  @PostMapping("/{moduleKey}/disable")
  public ApiResponse<ModuleRegistryResponse> disable(@PathVariable String moduleKey) {
    PermissionUtil.check(PERM_UPDATE);
    return ApiResponse.success(moduleRegistryService.enableModule(moduleKey, false));
  }

  @PostMapping("/{moduleKey}/install")
  public ApiResponse<ModuleRegistryResponse> install(@PathVariable String moduleKey) {
    PermissionUtil.check(PERM_UPDATE);
    return ApiResponse.success(moduleRegistryService.installModule(moduleKey));
  }

  @PostMapping("/{moduleKey}/uninstall")
  public ApiResponse<ModuleRegistryResponse> uninstall(@PathVariable String moduleKey) {
    PermissionUtil.check(PERM_UPDATE);
    return ApiResponse.success(moduleRegistryService.uninstallModule(moduleKey));
  }
}
