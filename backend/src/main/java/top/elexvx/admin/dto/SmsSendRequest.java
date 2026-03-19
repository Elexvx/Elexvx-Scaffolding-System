package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmsSendRequest {
  @NotBlank(message = "Phone is required")
  @Pattern(regexp = "^[+0-9-]{6,20}$", message = "Phone format is invalid (no spaces)")
  private String phone;

  private String provider;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = sanitizeNoSpace(phone);
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = sanitizeTrim(provider);
  }

  private String sanitizeTrim(String value) {
    return value == null ? null : value.trim();
  }

  private String sanitizeNoSpace(String value) {
    return value == null ? null : value.replaceAll("\\s+", "");
  }
}
