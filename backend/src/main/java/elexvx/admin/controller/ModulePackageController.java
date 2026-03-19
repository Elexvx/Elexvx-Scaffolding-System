package elexvx.admin.controller;

import elexvx.admin.module.ModuleDefinition;
import elexvx.admin.module.ModuleDefinitionRegistry;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.service.ModuleBackendProcessManager;
import elexvx.admin.service.ModulePackageService;
import elexvx.admin.service.ModuleRegistryService;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.ModuleRegistryResponse;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/system/modules")
public class ModulePackageController {
  private static final Logger log = LoggerFactory.getLogger(ModulePackageController.class);
  private static final String PERM_QUERY = "system:SystemModule:query";

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  private final ModuleDefinitionRegistry definitionRegistry;
  private final ModulePackageService modulePackageService;
  private final ModuleRegistryService moduleRegistryService;
  private final ModuleBackendProcessManager moduleBackendProcessManager;

  public ModulePackageController(
    ModuleDefinitionRegistry definitionRegistry,
    ModulePackageService modulePackageService,
    ModuleRegistryService moduleRegistryService,
    ModuleBackendProcessManager moduleBackendProcessManager
  ) {
    this.definitionRegistry = definitionRegistry;
    this.modulePackageService = modulePackageService;
    this.moduleRegistryService = moduleRegistryService;
    this.moduleBackendProcessManager = moduleBackendProcessManager;
  }

  @GetMapping("/{moduleKey}/package")
  public ResponseEntity<ByteArrayResource> downloadPackage(@PathVariable String moduleKey) {
    // 安装包下载为二进制文件响应，需要保留原生 Content-Disposition 与字节流。
    PermissionUtil.check(PERM_QUERY);
    ModuleDefinition definition = definitionRegistry.getDefinition(moduleKey);
    if (definition == null) {
      throw badRequest("模块不存在: " + moduleKey);
    }
    byte[] zip = modulePackageService.buildPackage(definition);
    String filename = "module-" + definition.getKey().trim().toLowerCase() + ".zip";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/zip"));
    headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
    return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(zip));
  }

  @PostMapping("/package/install")
  public ApiResponse<ModuleRegistryResponse> uploadAndInstall(@RequestParam("file") MultipartFile file) {
    PermissionUtil.checkAdmin();
    ModulePackageService.StagedModulePackage staged = modulePackageService.stagePackage(file);
    ModulePackageService.CommitResult commit = modulePackageService.commitStagedPackage(staged);
    String key = staged.manifest().getKey();
    try {
      ModuleRegistryResponse resp = moduleRegistryService.installModule(key);
      moduleBackendProcessManager.ensureRunning(key);
      return ApiResponse.success(resp);
    } catch (Exception ex) {
      moduleBackendProcessManager.stop(key);
      modulePackageService.rollbackCommit(commit);
      try {
        moduleRegistryService.uninstallModule(key);
      } catch (Exception uninstallException) {
        log.warn("模块安装失败后的卸载回滚失败，moduleKey={}", key, uninstallException);
      }
      throw ex;
    }
  }
}
