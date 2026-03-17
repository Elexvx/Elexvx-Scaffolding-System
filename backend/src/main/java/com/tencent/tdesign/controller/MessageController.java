package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.MessageSendRequest;
import com.tencent.tdesign.security.AccessControlService;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.service.MessageService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.Message;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {
  private static final String MESSAGE_BROADCAST_PERMISSION = "system:Message:broadcast";
  private static final String MESSAGE_SEND_PERMISSION = "system:Message:send";
  private final MessageService service;
  private final AuthContext authContext;
  private final AccessControlService accessControlService;

  public MessageController(MessageService service, AuthContext authContext, AccessControlService accessControlService) {
    this.service = service;
    this.authContext = authContext;
    this.accessControlService = accessControlService;
  }

  @GetMapping("/list")
  public ApiResponse<List<Message>> list() {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.list(userId));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<Message> send(@RequestBody @Validated MessageSendRequest req) {
    long currentUserId = authContext.requireUserId();
    checkSendPermission(currentUserId, req.getToUserId());
    Message m = service.send(currentUserId, req.getToUserId(), req.getContent(), req.getType(), req.getQuality());
    return ApiResponse.success(m);
  }

  @PostMapping("/broadcast")
  @RepeatSubmit
  public ApiResponse<Integer> broadcast(@RequestBody @Validated com.tencent.tdesign.dto.AnnouncementRequest req) {
    authContext.requireUserId();
    checkBroadcastPermission();
    int count = service.broadcast(req.getContent(), req.getType(), req.getQuality());
    return ApiResponse.success(count);
  }

  @PostMapping("/read")
  @RepeatSubmit
  public ApiResponse<Boolean> markRead(@RequestParam String id) {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.markRead(userId, id, true));
  }

  @PostMapping("/unread")
  @RepeatSubmit
  public ApiResponse<Boolean> markUnread(@RequestParam String id) {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.markRead(userId, id, false));
  }

  @PostMapping("/read-all")
  @RepeatSubmit
  public ApiResponse<Integer> readAll() {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.markAllRead(userId));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable String id) {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.delete(userId, id));
  }

  private void checkBroadcastPermission() {
    if (accessControlService.hasRole("admin")) {
      return;
    }
    if (accessControlService.hasPermission(MESSAGE_BROADCAST_PERMISSION)) {
      return;
    }
    throw new AccessDeniedException("仅管理员或具备消息广播权限的用户可执行该操作");
  }

  private void checkSendPermission(long currentUserId, Long targetUserId) {
    if (accessControlService.hasRole("admin")) {
      return;
    }
    if (accessControlService.hasPermission(MESSAGE_SEND_PERMISSION)) {
      return;
    }
    if (targetUserId != null && targetUserId == currentUserId) {
      return;
    }
    throw new AccessDeniedException("当前账号仅允许向自己发送消息");
  }
}
