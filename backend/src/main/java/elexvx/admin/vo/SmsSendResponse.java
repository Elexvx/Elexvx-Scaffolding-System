package elexvx.admin.vo;

public class SmsSendResponse {
  private int expiresIn;

  public SmsSendResponse() {}

  public SmsSendResponse(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }
}
