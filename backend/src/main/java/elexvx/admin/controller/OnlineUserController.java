package elexvx.admin.controller;

import elexvx.admin.service.OnlineUserService;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.security.AccessControlService;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.OnlineUserVO;
import elexvx.admin.vo.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/monitor")
public class OnlineUserController {
  private final OnlineUserService onlineUserService;
  private final OperationLogService operationLogService;
  private final AccessControlService accessControlService;

  public OnlineUserController(
    OnlineUserService onlineUserService,
    OperationLogService operationLogService,
    AccessControlService accessControlService
  ) {
    this.onlineUserService = onlineUserService;
    this.operationLogService = operationLogService;
    this.accessControlService = accessControlService;
  }

  @GetMapping("/online")
  public ApiResponse<PageResult<OnlineUserVO>> getOnlineUsers(
    @RequestParam(required = false) String loginAddress,
    @RequestParam(required = false) String userName,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    accessControlService.checkRole("admin");
    return ApiResponse.success(onlineUserService.getOnlineUsers(loginAddress, userName, page, size));
  }

  @DeleteMapping("/online/{sessionId}")
  public ApiResponse<Boolean> forceLogout(@PathVariable String sessionId) {
    accessControlService.checkRole("admin");
    OnlineUserVO target = onlineUserService.getOnlineUser(sessionId);
    boolean ok = onlineUserService.forceLogout(sessionId);
    if (ok) {
      String detail = target != null ? "强制下线: " + target.getLoginName() : "强制下线会话: " + sessionId;
      operationLogService.log("DELETE", "在线用户", detail);
    }
    return ApiResponse.success(ok);
  }
}
