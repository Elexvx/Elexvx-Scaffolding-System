package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class ConcurrentLoginDecisionRequest {
  @NotBlank(message = "requestId不能为空")
  private String requestId;

  @NotBlank(message = "action不能为空")
  private String action;

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
