package com.tencent.tdesign.controller;

import com.tencent.tdesign.dto.StorageSettingRequest;
import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.service.ObjectStorageService;
import com.tencent.tdesign.service.OperationLogService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.StorageSettingResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/storage")
public class StorageController {
  private static final String PERM_QUERY = "system:SystemStorage:query";
  private final ObjectStorageService storageService;
  private final OperationLogService operationLogService;

  public StorageController(ObjectStorageService storageService, OperationLogService operationLogService) {
    this.storageService = storageService;
    this.operationLogService = operationLogService;
  }

  @GetMapping
  public ApiResponse<StorageSettingResponse> get() {
    PermissionUtil.check(PERM_QUERY);
    return ApiResponse.success(storageService.currentSetting());
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<StorageSettingResponse> save(@RequestBody @Valid StorageSettingRequest req) {
    PermissionUtil.checkAdmin();
    var saved = storageService.save(req);
    operationLogService.log("UPDATE", "系统设置", "更新对象存储配置: " + saved.getProvider());
    return ApiResponse.success(StorageSettingResponse.from(saved));
  }

  @PostMapping("/test")
  @RepeatSubmit
  public ApiResponse<Void> test(@RequestBody @Valid StorageSettingRequest req) {
    PermissionUtil.checkAdmin();
    storageService.test(req);
    return ApiResponse.success(null);
  }
}
