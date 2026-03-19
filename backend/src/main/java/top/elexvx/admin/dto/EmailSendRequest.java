package top.elexvx.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailSendRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid (no spaces)")
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = sanitizeNoSpace(email);
  }

  private String sanitizeNoSpace(String value) {
    return value == null ? null : value.replaceAll("\\s+", "");
  }
}
