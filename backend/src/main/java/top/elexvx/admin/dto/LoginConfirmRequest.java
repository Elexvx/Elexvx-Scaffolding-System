package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginConfirmRequest {
  @NotBlank(message = "requestId不能为空")
  private String requestId;

  @NotBlank(message = "requestKey不能为空")
  private String requestKey;

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getRequestKey() {
    return requestKey;
  }

  public void setRequestKey(String requestKey) {
    this.requestKey = requestKey;
  }
}
