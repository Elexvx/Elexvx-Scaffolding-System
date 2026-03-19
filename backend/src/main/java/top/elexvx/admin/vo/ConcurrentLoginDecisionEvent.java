package top.elexvx.admin.vo;

public class ConcurrentLoginDecisionEvent {
  private String status;

  public ConcurrentLoginDecisionEvent() {}

  public ConcurrentLoginDecisionEvent(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
