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
import java.util.Map;
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

  @PostMapping("/concurrent/ticket")
  @RepeatSubmit
  public ApiResponse<Map<String, String>> issueConcurrentStreamTicket() {
    authContext.requireUserId();
    String token = authContext.requireToken();
    String ticket = concurrentLoginService.issueConcurrentStreamTicket(token);
    return ApiResponse.success(Map.of("ticket", ticket));
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

    concurrentLoginService.decide(userId, req.getRequestId(), approve);
    if (approve) {
      concurrentLoginService.publishForceLogout(userId, "当前登录状态已失效，请重新登录");
      authTokenService.removeUserTokens(userId);
    }
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
