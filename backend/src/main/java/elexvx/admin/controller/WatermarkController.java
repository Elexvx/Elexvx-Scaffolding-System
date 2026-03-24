package elexvx.admin.controller;

import elexvx.admin.dto.WatermarkSettingRequest;
import elexvx.admin.entity.WatermarkSetting;
import elexvx.admin.mapper.WatermarkSettingMapper;
import elexvx.admin.annotation.RepeatSubmit;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/watermark")
public class WatermarkController {
  private final WatermarkSettingMapper mapper;
  private final OperationLogService operationLogService;

  public WatermarkController(WatermarkSettingMapper mapper, OperationLogService operationLogService) {
    this.mapper = mapper;
    this.operationLogService = operationLogService;
  }

  @GetMapping
  public ApiResponse<WatermarkSetting> get() {
    // 允许所有登录用户获取水印配置
    WatermarkSetting setting = mapper.selectTop();
    return ApiResponse.success(setting == null ? new WatermarkSetting() : setting);
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<WatermarkSetting> save(@RequestBody @Valid WatermarkSettingRequest req) {
    PermissionUtil.checkAdmin();
    WatermarkSetting s = mapper.selectTop();
    if (s == null) s = new WatermarkSetting();
    s.setType(req.getType());
    s.setContent(req.getContent());
    s.setImageUrl(req.getImageUrl());
    s.setOpacity(req.getOpacity());
    s.setSize(req.getSize());
    s.setGapX(req.getGapX());
    s.setGapY(req.getGapY());
    s.setRotate(req.getRotate());
    s.setEnabled(req.getEnabled());
    if (s.getId() == null) mapper.insert(s);
    else mapper.update(s);
    operationLogService.log("UPDATE", "水印设置", "更新水印配置");
    return ApiResponse.success(s);
  }
}
