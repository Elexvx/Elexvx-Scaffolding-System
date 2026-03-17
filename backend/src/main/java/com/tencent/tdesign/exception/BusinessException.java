package com.tencent.tdesign.exception;

/**
 * 带业务错误码的异常。
 *
 * <p>用于将服务层错误统一交给全局异常处理器输出稳定响应。
 */
public class BusinessException extends RuntimeException {
  private final int code;

  public BusinessException(int code, String message) {
    super(message);
    this.code = code;
  }

  public BusinessException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
