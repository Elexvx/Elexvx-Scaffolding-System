package elexvx.admin.vo;

public class OnlineUserVO {
  private String sessionId;
  private String loginName;
  private String userName;
  private String ipAddress;
  private String loginLocation;
  private String browser;
  private String os;
  private String loginTime;

  public OnlineUserVO() {}

  public OnlineUserVO(
    String sessionId,
    String loginName,
    String userName,
    String ipAddress,
    String loginLocation,
    String browser,
    String os,
    String loginTime
  ) {
    this.sessionId = sessionId;
    this.loginName = loginName;
    this.userName = userName;
    this.ipAddress = ipAddress;
    this.loginLocation = loginLocation;
    this.browser = browser;
    this.os = os;
    this.loginTime = loginTime;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getLoginLocation() {
    return loginLocation;
  }

  public void setLoginLocation(String loginLocation) {
    this.loginLocation = loginLocation;
  }

  public String getBrowser() {
    return browser;
  }

  public void setBrowser(String browser) {
    this.browser = browser;
  }

  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

  public String getLoginTime() {
    return loginTime;
  }

  public void setLoginTime(String loginTime) {
    this.loginTime = loginTime;
  }
}
