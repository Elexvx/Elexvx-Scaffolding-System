package elexvx.admin.entity;

public class SecurityTokenSetting {
  private Long id;
  private Integer sessionTimeoutMinutes;
  private Integer tokenTimeoutMinutes;
  private Integer tokenRefreshGraceMinutes;
  private Boolean allowUrlTokenParam;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
  public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }
  public Integer getTokenTimeoutMinutes() { return tokenTimeoutMinutes; }
  public void setTokenTimeoutMinutes(Integer tokenTimeoutMinutes) { this.tokenTimeoutMinutes = tokenTimeoutMinutes; }
  public Integer getTokenRefreshGraceMinutes() { return tokenRefreshGraceMinutes; }
  public void setTokenRefreshGraceMinutes(Integer tokenRefreshGraceMinutes) { this.tokenRefreshGraceMinutes = tokenRefreshGraceMinutes; }
  public Boolean getAllowUrlTokenParam() { return allowUrlTokenParam; }
  public void setAllowUrlTokenParam(Boolean allowUrlTokenParam) { this.allowUrlTokenParam = allowUrlTokenParam; }
}
