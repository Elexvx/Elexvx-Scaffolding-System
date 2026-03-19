package top.elexvx.admin.controller;

import top.elexvx.admin.service.ServerMonitorService;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.ServerInfoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/monitor")
public class ServerMonitorController {

  private final ServerMonitorService serverMonitorService;

  public ServerMonitorController(ServerMonitorService serverMonitorService) {
    this.serverMonitorService = serverMonitorService;
  }

  @GetMapping("/server")
  public ApiResponse<ServerInfoVO> getServerInfo() {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(serverMonitorService.getServerInfo());
  }
}
