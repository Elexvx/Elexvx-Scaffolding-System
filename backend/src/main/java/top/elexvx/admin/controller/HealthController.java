package top.elexvx.admin.controller;

import top.elexvx.admin.service.HealthService;
import top.elexvx.admin.vo.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private final HealthService healthService;

  public HealthController(HealthService healthService) {
    this.healthService = healthService;
  }

  @GetMapping("/health")
  public ApiResponse<Map<String, Object>> health() {
    Map<String, Object> healthStatus = new HashMap<>();
    healthStatus.put("redisEnabled", healthService.isRedisEnabled());
    healthStatus.put("redisAvailable", healthService.isRedisAvailable());
    healthStatus.put("redisMessage", healthService.getRedisMessage());
    return ApiResponse.success(healthStatus);
  }
}
