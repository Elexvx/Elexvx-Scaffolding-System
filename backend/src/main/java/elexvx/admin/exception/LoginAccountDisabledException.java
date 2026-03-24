package elexvx.admin.exception;

/**
 * 账号被禁用或不可用。
 */
public class LoginAccountDisabledException extends LoginFlowException {
  private LoginAccountDisabledException(String message, String userTip, String logCode) {
    super(ErrorCodes.FORBIDDEN, message, userTip, logCode, false);
  }

  public static LoginAccountDisabledException disabled(String account) {
    return new LoginAccountDisabledException(
      "账号已禁用",
      "账号已禁用，请联系管理员",
      "LOGIN_ACCOUNT_DISABLED:account=" + safeAccount(account)
    );
  }

  private static String safeAccount(String account) {
    return account == null || account.isBlank() ? "unknown" : account.trim();
  }
}
