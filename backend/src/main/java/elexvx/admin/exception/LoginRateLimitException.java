package elexvx.admin.exception;

/**
 * 登录尝试过多或已触发临时限制。
 */
public class LoginRateLimitException extends LoginFlowException {
  private LoginRateLimitException(String message, String userTip, String logCode) {
    super(ErrorCodes.TOO_MANY_REQUESTS, message, userTip, logCode, false);
  }

  public static LoginRateLimitException tooManyAttempts(String account, long failCount, int threshold) {
    return new LoginRateLimitException(
      "登录失败次数过多，已临时限制",
      "尝试次数过多，请稍后再试",
      "LOGIN_RATE_LIMITED:account=" + safeAccount(account) + ":failCount=" + failCount + ":threshold=" + threshold
    );
  }

  public static LoginRateLimitException tooManyRequests(String account) {
    return new LoginRateLimitException(
      "登录请求过于频繁，已临时限制",
      "尝试次数过多，请稍后再试",
      "LOGIN_RATE_LIMITED:account=" + safeAccount(account)
    );
  }

  private static String safeAccount(String account) {
    return account == null || account.isBlank() ? "unknown" : account.trim();
  }
}
