package top.elexvx.admin.controller.plugin;

import top.elexvx.admin.plugin.PluginLifecycleService;
import top.elexvx.admin.plugin.PluginPackageService;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@ConditionalOnProperty(prefix = "elexvx.plugins", name = "enabled", havingValue = "true")
@RequestMapping("/plugins")
public class PluginController {
  private final PluginPackageService packageService;
  private final PluginLifecycleService lifecycleService;

  public PluginController(PluginPackageService packageService, PluginLifecycleService lifecycleService) {
    this.packageService = packageService;
    this.lifecycleService = lifecycleService;
  }

  @PostMapping("/install")
  public ApiResponse<PluginLifecycleService.PluginStatusResponse> install(@RequestParam("file") MultipartFile file) {
    PermissionUtil.checkAdmin();
    PluginPackageService.PluginInstallArtifact artifact = packageService.stage(file);
    return ApiResponse.success(lifecycleService.installAndEnable(artifact));
  }

  @PostMapping("/{pluginId}/disable")
  public ApiResponse<PluginLifecycleService.PluginStatusResponse> disable(@PathVariable String pluginId) {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(lifecycleService.disable(pluginId));
  }
}
