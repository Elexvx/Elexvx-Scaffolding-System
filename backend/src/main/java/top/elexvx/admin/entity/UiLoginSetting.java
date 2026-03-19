package top.elexvx.admin.entity;

public class UiLoginSetting {
  private Long id;
  private String loginBgUrl;
  private Boolean allowMultiDeviceLogin;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getLoginBgUrl() { return loginBgUrl; }
  public void setLoginBgUrl(String loginBgUrl) { this.loginBgUrl = loginBgUrl; }
  public Boolean getAllowMultiDeviceLogin() { return allowMultiDeviceLogin; }
  public void setAllowMultiDeviceLogin(Boolean allowMultiDeviceLogin) { this.allowMultiDeviceLogin = allowMultiDeviceLogin; }
}
