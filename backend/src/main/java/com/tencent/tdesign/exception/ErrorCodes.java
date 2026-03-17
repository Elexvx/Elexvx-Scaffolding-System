package com.tencent.tdesign.exception;

/**
 * 统一错误码定义。
 *
 * <p>当前保持与 HTTP 状态码语义一致，避免影响既有前端逻辑。
 */
public final class ErrorCodes {
  public static final int BAD_REQUEST = 400;
  public static final int UNAUTHORIZED = 401;
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;
  public static final int CONFLICT = 409;
  public static final int UNPROCESSABLE_ENTITY = 422;
  public static final int TOO_MANY_REQUESTS = 429;
  public static final int PAYLOAD_TOO_LARGE = 413;
  public static final int INTERNAL_SERVER_ERROR = 500;

  private ErrorCodes() {}
}
