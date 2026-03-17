package com.tencent.tdesign.config;

import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ConcurrentLoginException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.exception.ModuleUnavailableException;
import com.tencent.tdesign.exception.RepeatSubmitException;
import com.tencent.tdesign.mapper.MenuItemMapper;
import com.tencent.tdesign.entity.MenuItemEntity;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
/**
 * 全局异常处理器。
 *
 * <p>将常见异常统一转换为 {@link ApiResponse}，并对 SSE 请求做兼容处理：
 * SSE 场景下返回 {@code text/event-stream} 格式的事件，避免前端连接被异常响应体破坏。
 */
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final @NonNull MediaType SSE_MEDIA_TYPE = MediaType.parseMediaType(MediaType.TEXT_EVENT_STREAM_VALUE);
  private final MenuItemMapper menuItemMapper;

  public GlobalExceptionHandler(MenuItemMapper menuItemMapper) {
    this.menuItemMapper = menuItemMapper;
  }

  /**
   * 处理未登录异常
   */
  @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
  public Object handleAuthenticationCredentialsNotFoundException(
    AuthenticationCredentialsNotFoundException e,
    HttpServletRequest request
  ) {
    log.debug("用户未登录或token已过期: {}", e.getMessage());
    String raw = e.getMessage();
    String msg = "未登录或登录已失效，请重新登录";
    if (raw != null && !raw.isBlank() && !raw.matches(".*[A-Za-z].*")) {
      msg = raw;
    }
    if (isSseRequest(request)) {
      String payload = "event: unauthorized\ndata: " + escapeSseData(msg) + "\n\n";
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(SSE_MEDIA_TYPE).body(payload);
    }
    return ApiResponse.failure(ErrorCodes.UNAUTHORIZED, msg);
  }

  /**
   * 处理权限不足异常
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
    log.warn("权限不足: {}", e.getMessage());
    String message = buildPermissionMessage(e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.failure(ErrorCodes.FORBIDDEN, message));
  }

  @ExceptionHandler(ModuleUnavailableException.class)
  public ResponseEntity<ApiResponse<Void>> handleModuleUnavailableException(ModuleUnavailableException e) {
    String msg = e.getMessage() == null || e.getMessage().isBlank() ? "模块不可用" : e.getMessage();
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.failure(ErrorCodes.FORBIDDEN, msg));
  }

  /**
   * 处理参数校验异常 (Bean Validation)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
      .map(FieldError::getDefaultMessage)
      .collect(Collectors.joining("; "));
    log.debug("参数校验失败: {}", message);
    return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "参数校验失败: " + message);
  }

  /**
   * 处理参数绑定异常
   */
  @ExceptionHandler(BindException.class)
  public ApiResponse<Void> handleBindException(BindException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
      .map(FieldError::getDefaultMessage)
      .collect(Collectors.joining("; "));
    log.debug("参数绑定失败: {}", message);
    return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "参数绑定失败: " + message);
  }

  /**
   * 处理多设备登录异常
   */
  @ExceptionHandler(ConcurrentLoginException.class)
  public ApiResponse<Void> handleConcurrentLoginException(ConcurrentLoginException e) {
    log.debug("检测到多设备登录: {}", e.getMessage());
    return ApiResponse.failure(ErrorCodes.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(RepeatSubmitException.class)
  public ResponseEntity<ApiResponse<Void>> handleRepeatSubmitException(RepeatSubmitException e) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
      .body(ApiResponse.failure(ErrorCodes.TOO_MANY_REQUESTS, e.getMessage()));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    int code = e.getCode();
    String message = e.getMessage() == null || e.getMessage().isBlank() ? "请求处理失败" : e.getMessage();
    HttpStatus status = HttpStatus.resolve(code);
    if (status == null || status.is2xxSuccessful()) {
      status = HttpStatus.BAD_REQUEST;
    }
    if (status.is5xxServerError()) {
      log.error("业务异常(code={}): {}", code, message, e);
    } else {
      log.warn("业务异常(code={}): {}", code, message);
    }
    return ResponseEntity.status(status).body(ApiResponse.failure(code, message));
  }

  /**
   * 处理非法参数异常
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
    log.debug("非法参数: {}", e.getMessage());
    return ApiResponse.failure(ErrorCodes.BAD_REQUEST, e.getMessage());
  }

  /**
   * 处理请求体解析异常（如日期格式、JSON 格式错误等）
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ApiResponse<Void> handleHttpMessageNotReadableException(@NonNull HttpMessageNotReadableException e) {
    Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
    if (root instanceof DateTimeParseException) {
      return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "日期格式错误，应为 yyyy-MM-dd");
    }
    log.debug("请求体解析失败: {}", root != null ? root.getMessage() : e.getMessage());
    return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "请求体格式错误");
  }

  /**
   * 处理所有未捕获的异常
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ApiResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    log.debug("上传文件超限: {}", e.getMessage());
    return ApiResponse.failure(ErrorCodes.PAYLOAD_TOO_LARGE, "上传文件过大，请压缩后重试");
  }

  @ExceptionHandler(AsyncRequestTimeoutException.class)
  public Object handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e, HttpServletRequest request) {
    if (isSseRequest(request)) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(SSE_MEDIA_TYPE).body("");
    }
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @ExceptionHandler(AsyncRequestNotUsableException.class)
  public Object handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e, HttpServletRequest request) {
    log.debug("Async response is not usable, likely client disconnected: {}", e.getMessage());
    if (isSseRequest(request)) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(SSE_MEDIA_TYPE).body("");
    }
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @ExceptionHandler(IOException.class)
  public Object handleIOException(IOException e, HttpServletRequest request) {
    if (isClientDisconnected(e)) {
      log.debug("Client disconnected during async response write: {}", e.getMessage());
      if (isSseRequest(request)) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(SSE_MEDIA_TYPE).body("");
      }
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    log.error("IO exception", e);
    if (isSseRequest(request)) {
      String payload = "event: error\ndata: server io error\n\n";
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(SSE_MEDIA_TYPE)
        .body(payload);
    }
    return ApiResponse.failure(ErrorCodes.INTERNAL_SERVER_ERROR, "服务器内部错误，请联系管理员");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException e) {
    log.debug("静态资源不存在: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @ExceptionHandler(Exception.class)
  public Object handleException(Exception e, HttpServletRequest request) {
    log.error("系统异常", e);
    if (isSseRequest(request)) {
      String payload = "event: error\ndata: server error\n\n";
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(SSE_MEDIA_TYPE)
        .body(payload);
    }
    return ApiResponse.failure(ErrorCodes.INTERNAL_SERVER_ERROR, "服务器内部错误，请联系管理员");
  }

  private String buildPermissionMessage(String permission) {
    if (permission == null || permission.isBlank()) {
      return "权限不足，请联系管理员开通权限后再试";
    }
    String text = permission.trim();
    // Keep plain access-denied messages unchanged to avoid duplicated "missing permission" wrappers.
    if (!text.contains(":")) {
      if ("Access is denied".equalsIgnoreCase(text) || "Forbidden".equalsIgnoreCase(text)) {
        return "权限不足，请联系管理员开通权限后再试";
      }
      return text;
    }
    String[] parts = text.split(":");
    if (parts.length != 3 || !"system".equalsIgnoreCase(parts[0])) {
      return "权限不足，请联系管理员开通权限后再试";
    }
    String resource = parts[1];
    String action = parts[2];
    String pageName = resolvePageTitle(resource);
    String actionLabel = resolveActionLabel(action);
    String suffix = (actionLabel == null || actionLabel.isBlank()) ? "" : "的" + actionLabel + "操作";
    return "权限不足：页面[" + pageName + "] 权限[" + text + "]未开通" + suffix + "，请联系管理员开通权限后再试";
  }

  private String resolvePageTitle(String resource) {
    if (resource == null || resource.isBlank()) return "Unknown";
    MenuItemEntity item = menuItemMapper.selectByRouteName(resource);
    if (item == null) return resource;
    String title = item.getTitleZhCn();
    return (title == null || title.isBlank()) ? resource : title;
  }

  private String resolveActionLabel(String action) {
    if (action == null || action.isBlank()) return null;
    return switch (action) {
      case "create" -> "新增";
      case "update" -> "修改";
      case "delete" -> "删除";
      case "query" -> "查询";
      case "import" -> "导入";
      case "export" -> "导出";
      default -> action;
    };
  }

  private boolean isSseRequest(HttpServletRequest request) {
    if (request == null) return false;
    String accept = request.getHeader("Accept");
    if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
      return true;
    }
    String uri = request.getRequestURI();
    if (uri == null || uri.isBlank()) {
      Object errorUri = request.getAttribute("jakarta.servlet.error.request_uri");
      if (errorUri instanceof String) uri = (String) errorUri;
    }
    if (uri == null) return false;
    return uri.endsWith("/auth/concurrent/stream")
      || uri.endsWith("/auth/login/pending/stream");
  }

  private boolean isClientDisconnected(Throwable throwable) {
    Throwable cursor = throwable;
    while (cursor != null) {
      if (cursor instanceof AsyncRequestNotUsableException || cursor instanceof EOFException || cursor instanceof SocketException) {
        return true;
      }
      String message = cursor.getMessage();
      if (message != null) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (
          lower.contains("broken pipe")
            || lower.contains("connection reset")
            || lower.contains("connection aborted")
            || lower.contains("response not usable")
            || lower.contains("你的主机中的软件中止了一个已建立的连接")
        ) {
          return true;
        }
      }
      cursor = cursor.getCause();
    }
    return false;
  }

  private String escapeSseData(String data) {
    if (data == null) return "";
    return data.replace("\n", "\\n").replace("\r", "");
  }
}
