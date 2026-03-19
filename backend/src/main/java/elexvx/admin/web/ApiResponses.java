package elexvx.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.vo.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public final class ApiResponses {
  private ApiResponses() {}

  public static <T> ApiResponse<T> success(T data) {
    return build(true, 0, null, null, data);
  }

  public static ApiResponse<Void> failure(int code, String message, String userTip) {
    return build(false, code, message, userTip, null);
  }

  public static ApiResponse<Void> unauthorized(String message, String userTip) {
    return failure(ErrorCodes.UNAUTHORIZED, message, userTip);
  }

  public static ApiResponse<Void> forbidden(String message, String userTip) {
    return failure(ErrorCodes.FORBIDDEN, message, userTip);
  }

  public static ApiResponse<Void> validationError(String message, String userTip) {
    return failure(ErrorCodes.UNPROCESSABLE_ENTITY, message, userTip);
  }

  public static ApiResponse<Void> notFound(String message, String userTip) {
    return failure(ErrorCodes.NOT_FOUND, message, userTip);
  }

  public static ApiResponse<Void> conflict(String message, String userTip) {
    return failure(ErrorCodes.CONFLICT, message, userTip);
  }

  public static ApiResponse<Void> rateLimit(String message, String userTip) {
    return failure(ErrorCodes.TOO_MANY_REQUESTS, message, userTip);
  }

  public static ApiResponse<Void> payloadTooLarge(String message, String userTip) {
    return failure(ErrorCodes.PAYLOAD_TOO_LARGE, message, userTip);
  }

  public static ApiResponse<Void> internalError(String message, String userTip) {
    return failure(ErrorCodes.INTERNAL_SERVER_ERROR, message, userTip);
  }

  public static void writeJson(
    HttpServletResponse response,
    HttpStatus status,
    ApiResponse<?> body,
    ObjectMapper objectMapper
  ) throws IOException {
    if (response.isCommitted()) {
      return;
    }
    response.setStatus(status.value());
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }

  private static <T> ApiResponse<T> build(boolean success, int code, String message, String userTip, T data) {
    long timestamp = Instant.now().toEpochMilli();
    String requestId = RequestTraceContext.currentRequestId();
    String path = RequestTraceContext.currentPath();
    return new ApiResponse<>(success, code, data, message, userTip, requestId, timestamp, path);
  }
}
