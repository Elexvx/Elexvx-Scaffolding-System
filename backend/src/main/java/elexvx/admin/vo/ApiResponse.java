package elexvx.admin.vo;

import elexvx.admin.web.ApiResponses;

public class ApiResponse<T> {
  private boolean success;
  private int code;
  private T data;
  private String message;
  private String userTip;
  private String requestId;
  private long timestamp;
  private String path;

  public ApiResponse() {}

  public ApiResponse(
    boolean success,
    int code,
    T data,
    String message,
    String userTip,
    String requestId,
    long timestamp,
    String path
  ) {
    this.success = success;
    this.code = code;
    this.data = data;
    this.message = message;
    this.userTip = userTip;
    this.requestId = requestId;
    this.timestamp = timestamp;
    this.path = path;
  }

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponses.success(data);
  }

  public static <T> ApiResponse<T> failure(int code, String message) {
    return failure(code, message, message);
  }

  public static <T> ApiResponse<T> failure(int code, String message, String userTip) {
    @SuppressWarnings("unchecked")
    ApiResponse<T> response = (ApiResponse<T>) ApiResponses.failure(code, message, userTip);
    return response;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getUserTip() {
    return userTip;
  }

  public void setUserTip(String userTip) {
    this.userTip = userTip;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
