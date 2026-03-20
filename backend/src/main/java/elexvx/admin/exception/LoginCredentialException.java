package elexvx.admin.exception;

/**
 * 账号或密码校验失败。
 *
 * <p>前端统一展示为“账号或密码错误”，后端日志会在抛出前区分账号不存在和密码不匹配。
 */
public class LoginCredentialException extends LoginFlowException {
  private LoginCredentialException(String message, String userTip, String logCode) {
    super(ErrorCodes.BAD_REQUEST, message, userTip, logCode, true);
  }

  public static LoginCredentialException accountNotFound(String account) {
    return new LoginCredentialException(
      "账号或密码错误",
      "账号或密码错误",
      "LOGIN_CREDENTIAL_INVALID:ACCOUNT_NOT_FOUND:" + safeAccount(account)
    );
  }

  public static LoginCredentialException passwordMismatch(String account) {
    return new LoginCredentialException(
      "账号或密码错误",
      "账号或密码错误",
      "LOGIN_CREDENTIAL_INVALID:PASSWORD_MISMATCH:" + safeAccount(account)
    );
  }

  private static String safeAccount(String account) {
    return account == null || account.isBlank() ? "unknown" : account.trim();
  }
}
