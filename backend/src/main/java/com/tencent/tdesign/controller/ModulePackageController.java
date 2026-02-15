package com.tencent.tdesign.controller;

import com.tencent.tdesign.module.ModuleDefinition;
import com.tencent.tdesign.module.ModuleDefinitionRegistry;
import com.tencent.tdesign.service.ModuleBackendProcessManager;
import com.tencent.tdesign.service.ModulePackageService;
import com.tencent.tdesign.service.ModuleRegistryService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.ModuleRegistryResponse;
import java.nio.charset.StandardCharsets;
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
  private static final String PERM_QUERY = "system:SystemModule:query";
  private static final String PERM_UPDATE = "system:SystemModule:update";

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
    PermissionUtil.check(PERM_QUERY);
    ModuleDefinition definition = definitionRegistry.getDefinition(moduleKey);
    if (definition == null) {
      throw new IllegalArgumentException("模块不存在: " + moduleKey);
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
      } catch (Exception ignored) {}
      throw ex;
    }
  }
}
