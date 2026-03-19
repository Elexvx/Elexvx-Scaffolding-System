package top.elexvx.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailLoginRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid (no spaces)")
  private String email;

  @NotBlank(message = "Verification code is required")
  private String code;

  private Boolean force;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = sanitizeNoSpace(email);
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = sanitizeNoSpace(code);
  }

  public Boolean getForce() {
    return force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }

  private String sanitizeNoSpace(String value) {
    return value == null ? null : value.replaceAll("\\s+", "");
  }
}
