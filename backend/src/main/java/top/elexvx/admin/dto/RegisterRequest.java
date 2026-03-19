package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
  @NotBlank(message = "Account is required")
  @Size(max = 64, message = "Account length must be <= 64")
  @Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Account only supports letters, numbers, and _@.- (no spaces)")
  private String account;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 64, message = "Password length must be 6-64")
  private String password;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;

  private String captchaId;

  private String captchaCode;

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = sanitizeTrim(account);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = sanitizeTrim(password);
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = sanitizeTrim(confirmPassword);
  }

  public String getCaptchaId() {
    return captchaId;
  }

  public void setCaptchaId(String captchaId) {
    this.captchaId = sanitizeTrim(captchaId);
  }

  public String getCaptchaCode() {
    return captchaCode;
  }

  public void setCaptchaCode(String captchaCode) {
    this.captchaCode = sanitizeNoSpace(captchaCode);
  }

  private String sanitizeTrim(String value) {
    return value == null ? null : value.trim();
  }

  private String sanitizeNoSpace(String value) {
    return value == null ? null : value.replaceAll("\\s+", "");
  }
}
