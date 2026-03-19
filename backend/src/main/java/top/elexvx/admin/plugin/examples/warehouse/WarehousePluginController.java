package top.elexvx.admin.plugin.examples.warehouse;

import top.elexvx.admin.vo.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plugins/warehouse/inbound")
public class WarehousePluginController {
  @GetMapping("/demo")
  public ApiResponse<Map<String, Object>> demoInbound() {
    return ApiResponse.success(Map.of("inboundNo", "IN-2026-0001", "status", "CREATED"));
  }
}
