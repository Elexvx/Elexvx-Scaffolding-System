package top.elexvx.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
  @NotBlank(message = "账号不能为空")
  @Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "账号仅支持字母、数字及_@.-，不支持空格")
  private String account;

  @NotBlank(message = "密码不能为空")
  private String password;

  private String captchaId;
  private String captchaCode;
  private Boolean force;

  public Boolean getForce() {
    return force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }

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
