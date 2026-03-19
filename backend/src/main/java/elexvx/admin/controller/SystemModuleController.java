package elexvx.admin.controller;

import elexvx.admin.service.ModuleRegistryService;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.ModuleDescriptor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/modules/descriptor")
public class SystemModuleController {
  private final ModuleRegistryService moduleRegistryService;

  public SystemModuleController(ModuleRegistryService moduleRegistryService) {
    this.moduleRegistryService = moduleRegistryService;
  }

  @GetMapping
  public ApiResponse<List<ModuleDescriptor>> list() {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(moduleRegistryService.listModules());
  }
}
