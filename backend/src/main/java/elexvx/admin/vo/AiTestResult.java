package elexvx.admin.vo;

public class AiTestResult {
  private boolean success;
  private String message;
  private long durationMs;

  public static AiTestResult ok(String message, long durationMs) {
    AiTestResult r = new AiTestResult();
    r.success = true;
    r.message = message;
    r.durationMs = durationMs;
    return r;
  }

  public static AiTestResult failed(String message) {
    AiTestResult r = new AiTestResult();
    r.success = false;
    r.message = message;
    r.durationMs = 0;
    return r;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getDurationMs() {
    return durationMs;
  }

  public void setDurationMs(long durationMs) {
    this.durationMs = durationMs;
  }
}
