package top.elexvx.admin.controller;

import top.elexvx.admin.service.RedisService;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.RedisInfoVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/monitor")
@ConditionalOnProperty(name = "elexvx.redis.enabled", havingValue = "true")
public class RedisController {

  private final RedisService redisService;

  public RedisController(RedisService redisService) {
    this.redisService = redisService;
  }

  @GetMapping("/redis")
  public ApiResponse<RedisInfoVO> getRedisInfo() {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(redisService.getRedisInfo());
  }
}
