package elexvx.admin.exception;

/**
 * 登录验证码失败。
 */
public class LoginCaptchaException extends LoginFlowException {
  private LoginCaptchaException(String message, String userTip, String logCode) {
    super(ErrorCodes.BAD_REQUEST, message, userTip, logCode, false);
  }

  public static LoginCaptchaException missing() {
    return new LoginCaptchaException(
      "验证码不能为空",
      "请输入验证码",
      "LOGIN_CAPTCHA_MISSING"
    );
  }

  public static LoginCaptchaException invalidOrExpired() {
    return new LoginCaptchaException(
      "验证码错误或已过期",
      "验证码错误或已过期",
      "LOGIN_CAPTCHA_INVALID"
    );
  }
}
