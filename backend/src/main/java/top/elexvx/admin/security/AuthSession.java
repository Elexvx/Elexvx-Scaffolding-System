package top.elexvx.admin.security;

import java.util.HashMap;
import java.util.Map;

public class AuthSession {
  private String token;
  private long userId;
  private long issuedAt;
  private long expiresAt;
  private String deviceId;
  private String deviceModel;
  private String os;
  private String browser;
  private String ipAddress;
  private String loginLocation;
  private Map<String, Object> attributes = new HashMap<>();

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public long getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(long issuedAt) {
    this.issuedAt = issuedAt;
  }

  public long getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(long expiresAt) {
    this.expiresAt = expiresAt;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
  }

  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

  public String getBrowser() {
    return browser;
  }

  public void setBrowser(String browser) {
    this.browser = browser;
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

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes == null ? new HashMap<>() : attributes;
  }
}
