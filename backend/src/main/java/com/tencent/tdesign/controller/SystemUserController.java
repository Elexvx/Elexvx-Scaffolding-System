package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.annotation.PagePerm;
import com.tencent.tdesign.dto.UserCreateRequest;
import com.tencent.tdesign.dto.UserUpdateRequest;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.service.UserAdminService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.PageResult;
import com.tencent.tdesign.vo.UserListItem;
import jakarta.validation.Valid;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/user")
public class SystemUserController {
  private static final Logger log = LoggerFactory.getLogger(SystemUserController.class);
  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
  private final UserAdminService userAdminService;
  private final AuthContext authContext;
  private final boolean allowPasswordInQuery;

  public SystemUserController(
    UserAdminService userAdminService,
    AuthContext authContext,
    @Value("${tdesign.security.allow-password-in-query:false}") boolean allowPasswordInQuery
  ) {
    this.userAdminService = userAdminService;
    this.authContext = authContext;
    this.allowPasswordInQuery = allowPasswordInQuery;
  }

  @GetMapping("/page")
  @PagePerm(routeName = "SystemUser", action = "query")
  public ApiResponse<PageResult<UserListItem>> page(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String mobile,
    @RequestParam(required = false) Long orgUnitId,
    @RequestParam(required = false) Long departmentId,
    @RequestParam(required = false) Integer status,
    @RequestParam(required = false) String startTime,
    @RequestParam(required = false) String endTime,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    java.time.LocalDateTime start = parseDateTime(startTime);
    java.time.LocalDateTime end = parseDateTime(endTime);
    return ApiResponse.success(userAdminService.page(keyword, mobile, orgUnitId, departmentId, status, start, end, page, size));
  }

  @GetMapping("/{id}")
  @PagePerm(routeName = "SystemUser", action = "query")
  public ApiResponse<UserListItem> get(@PathVariable long id) {
    return ApiResponse.success(userAdminService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  @PagePerm(routeName = "SystemUser", action = "create")
  public ApiResponse<UserListItem> create(@RequestBody @Valid UserCreateRequest req) {
    return ApiResponse.success(userAdminService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemUser", action = "update")
  public ApiResponse<UserListItem> update(@PathVariable long id, @RequestBody @Valid UserUpdateRequest req) {
    return ApiResponse.success(userAdminService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  @PagePerm(routeName = "SystemUser", action = "delete")
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    long self = authContext.requireUserId();
    if (self == id) throw badRequest("不允许删除当前登录用户");
    return ApiResponse.success(userAdminService.delete(id));
  }

  @PostMapping("/{id}/reset-password")
  @RepeatSubmit
  @PagePerm(routeName = "SystemUser", action = "update")
  public ApiResponse<Boolean> resetPassword(
    @PathVariable long id,
    @RequestBody(required = false) Map<String, Object> body,
    @Deprecated(since = "1.0.1", forRemoval = false)
    @RequestParam(required = false) String password
  ) {
    if (password != null && !allowPasswordInQuery) {
      throw badRequest("不允许通过 URL 参数传递密码，请改为 JSON body: {\"password\": \"...\"}");
    }
    String newPassword = null;
    if (body != null) {
      Object value = body.get("password");
      if (value != null) newPassword = String.valueOf(value);
    }
    if (newPassword == null && allowPasswordInQuery) {
      newPassword = password;
    }
    return ApiResponse.success(userAdminService.resetPassword(id, newPassword));
  }

  private java.time.LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return java.time.LocalDateTime.parse(value);
    } catch (java.time.format.DateTimeParseException firstParseException) {
      try {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return java.time.LocalDateTime.parse(value, formatter);
      } catch (java.time.format.DateTimeParseException secondParseException) {
        log.debug("用户时间参数解析失败，value={}", value, secondParseException);
        return null;
      }
    }
  }
}
