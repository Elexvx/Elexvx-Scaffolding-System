package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
  @NotBlank(message = "账号不能为空")
  private String account;

  @NotBlank(message = "手机号不能为空")
  private String phone;

  @NotBlank(message = "验证码不能为空")
  private String code;

  @NotBlank(message = "新密码不能为空")
  private String newPassword;

  @NotBlank(message = "确认密码不能为空")
  private String confirmPassword;

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = sanitizeTrim(account);
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = sanitizeNoSpace(phone);
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = sanitizeNoSpace(code);
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = sanitizeTrim(newPassword);
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = sanitizeTrim(confirmPassword);
  }

  private String sanitizeTrim(String value) {
    return value == null ? null : value.trim();
  }

  private String sanitizeNoSpace(String value) {
    return value == null ? null : value.replaceAll("\\s+", "");
  }
}
