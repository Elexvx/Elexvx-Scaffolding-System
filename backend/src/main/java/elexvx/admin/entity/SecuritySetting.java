package elexvx.admin.entity;

public class SecuritySetting {
  private Long id;

  private Integer sessionTimeoutMinutes;

  private Integer tokenTimeoutMinutes;

  private Integer tokenRefreshGraceMinutes;

  private Boolean allowUrlTokenParam;

  private Boolean captchaEnabled;

  private String captchaType;

  private Integer dragCaptchaWidth;

  private Integer dragCaptchaHeight;

  private Integer dragCaptchaThreshold;

  private Integer imageCaptchaLength;

  private Integer imageCaptchaNoiseLines;

  private Integer passwordMinLength;

  private Boolean passwordRequireUppercase;

  private Boolean passwordRequireLowercase;

  private Boolean passwordRequireSpecial;

  private Boolean passwordAllowSequential;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getSessionTimeoutMinutes() {
    return sessionTimeoutMinutes;
  }

  public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
    this.sessionTimeoutMinutes = sessionTimeoutMinutes;
  }

  public Integer getTokenTimeoutMinutes() {
    return tokenTimeoutMinutes;
  }

  public void setTokenTimeoutMinutes(Integer tokenTimeoutMinutes) {
    this.tokenTimeoutMinutes = tokenTimeoutMinutes;
  }

  public Integer getTokenRefreshGraceMinutes() {
    return tokenRefreshGraceMinutes;
  }

  public void setTokenRefreshGraceMinutes(Integer tokenRefreshGraceMinutes) {
    this.tokenRefreshGraceMinutes = tokenRefreshGraceMinutes;
  }

  public Boolean getAllowUrlTokenParam() {
    return allowUrlTokenParam;
  }

  public void setAllowUrlTokenParam(Boolean allowUrlTokenParam) {
    this.allowUrlTokenParam = allowUrlTokenParam;
  }

  public Boolean getCaptchaEnabled() {
    return captchaEnabled;
  }

  public void setCaptchaEnabled(Boolean captchaEnabled) {
    this.captchaEnabled = captchaEnabled;
  }

  public String getCaptchaType() {
    return captchaType;
  }

  public void setCaptchaType(String captchaType) {
    this.captchaType = captchaType;
  }

  public Integer getDragCaptchaWidth() {
    return dragCaptchaWidth;
  }

  public void setDragCaptchaWidth(Integer dragCaptchaWidth) {
    this.dragCaptchaWidth = dragCaptchaWidth;
  }

  public Integer getDragCaptchaHeight() {
    return dragCaptchaHeight;
  }

  public void setDragCaptchaHeight(Integer dragCaptchaHeight) {
    this.dragCaptchaHeight = dragCaptchaHeight;
  }

  public Integer getDragCaptchaThreshold() {
    return dragCaptchaThreshold;
  }

  public void setDragCaptchaThreshold(Integer dragCaptchaThreshold) {
    this.dragCaptchaThreshold = dragCaptchaThreshold;
  }

  public Integer getImageCaptchaLength() {
    return imageCaptchaLength;
  }

  public void setImageCaptchaLength(Integer imageCaptchaLength) {
    this.imageCaptchaLength = imageCaptchaLength;
  }

  public Integer getImageCaptchaNoiseLines() {
    return imageCaptchaNoiseLines;
  }

  public void setImageCaptchaNoiseLines(Integer imageCaptchaNoiseLines) {
    this.imageCaptchaNoiseLines = imageCaptchaNoiseLines;
  }

  public Integer getPasswordMinLength() {
    return passwordMinLength;
  }

  public void setPasswordMinLength(Integer passwordMinLength) {
    this.passwordMinLength = passwordMinLength;
  }

  public Boolean getPasswordRequireUppercase() {
    return passwordRequireUppercase;
  }

  public void setPasswordRequireUppercase(Boolean passwordRequireUppercase) {
    this.passwordRequireUppercase = passwordRequireUppercase;
  }

  public Boolean getPasswordRequireLowercase() {
    return passwordRequireLowercase;
  }

  public void setPasswordRequireLowercase(Boolean passwordRequireLowercase) {
    this.passwordRequireLowercase = passwordRequireLowercase;
  }

  public Boolean getPasswordRequireSpecial() {
    return passwordRequireSpecial;
  }

  public void setPasswordRequireSpecial(Boolean passwordRequireSpecial) {
    this.passwordRequireSpecial = passwordRequireSpecial;
  }

  public Boolean getPasswordAllowSequential() {
    return passwordAllowSequential;
  }

  public void setPasswordAllowSequential(Boolean passwordAllowSequential) {
    this.passwordAllowSequential = passwordAllowSequential;
  }
}
