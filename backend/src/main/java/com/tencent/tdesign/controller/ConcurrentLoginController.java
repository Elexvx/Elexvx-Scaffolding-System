package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.ConcurrentLoginDecisionRequest;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.service.ConcurrentLoginService;
import com.tencent.tdesign.service.AuthTokenService;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/auth")
public class ConcurrentLoginController {
  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
  private final ConcurrentLoginService concurrentLoginService;
  private final AuthContext authContext;
  private final AuthTokenService authTokenService;

  public ConcurrentLoginController(
    ConcurrentLoginService concurrentLoginService,
    AuthContext authContext,
    AuthTokenService authTokenService
  ) {
    this.concurrentLoginService = concurrentLoginService;
    this.authContext = authContext;
    this.authTokenService = authTokenService;
  }

  @GetMapping(value = "/concurrent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribeConcurrentLogin() {
    long userId = authContext.requireUserId();
    return concurrentLoginService.subscribeLoginNotice(userId);
  }

  @PostMapping("/concurrent/decision")
  @RepeatSubmit
  public ApiResponse<Boolean> decide(@RequestBody @Valid ConcurrentLoginDecisionRequest req) {
    long userId = authContext.requireUserId();
    String action = req.getAction();
    boolean approve;
    if ("approve".equalsIgnoreCase(action)) approve = true;
    else if ("reject".equalsIgnoreCase(action)) approve = false;
    else throw badRequest("action仅支持 approve 或 reject");

    if (approve) {
      concurrentLoginService.publishForceLogout(userId, "\u5f53\u524d\u767b\u5f55\u72b6\u6001\u5df2\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55");
      authTokenService.removeUserTokens(userId);
    }
    concurrentLoginService.decide(userId, req.getRequestId(), approve);
    return ApiResponse.success(true);
  }

  @GetMapping(value = "/login/pending/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribePendingDecision(
    @RequestParam String requestId,
    @RequestParam String requestKey
  ) {
    return concurrentLoginService.subscribeDecision(requestId, requestKey);
  }
}
