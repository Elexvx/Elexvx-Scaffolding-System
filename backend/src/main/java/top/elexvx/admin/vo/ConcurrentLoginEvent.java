package top.elexvx.admin.vo;

public class ConcurrentLoginEvent {
  private String requestId;
  private String deviceInfo;
  private String ipAddress;
  private String loginLocation;
  private String loginTime;

  public ConcurrentLoginEvent() {}

  public ConcurrentLoginEvent(
    String requestId,
    String deviceInfo,
    String ipAddress,
    String loginLocation,
    String loginTime
  ) {
    this.requestId = requestId;
    this.deviceInfo = deviceInfo;
    this.ipAddress = ipAddress;
    this.loginLocation = loginLocation;
    this.loginTime = loginTime;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getDeviceInfo() {
    return deviceInfo;
  }

  public void setDeviceInfo(String deviceInfo) {
    this.deviceInfo = deviceInfo;
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

  public String getLoginTime() {
    return loginTime;
  }

  public void setLoginTime(String loginTime) {
    this.loginTime = loginTime;
  }
}
