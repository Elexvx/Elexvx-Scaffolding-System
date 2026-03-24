package elexvx.admin.exception;

/**
 * 登录链路中的可预期异常基类。
 *
 * <p>用于区分登录失败类型，同时保留给前端的安全提示和给后端排障的日志标识。
 */
public abstract class LoginFlowException extends BusinessException {
  private final String userTip;
  private final String logCode;
  private final boolean credentialFailure;

  protected LoginFlowException(int code, String message, String userTip, String logCode, boolean credentialFailure) {
    super(code, message);
    this.userTip = userTip;
    this.logCode = logCode;
    this.credentialFailure = credentialFailure;
  }

  public String getUserTip() {
    return userTip;
  }

  public String getLogCode() {
    return logCode;
  }

  public boolean isCredentialFailure() {
    return credentialFailure;
  }
}
