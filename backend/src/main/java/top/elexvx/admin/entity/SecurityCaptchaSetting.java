package top.elexvx.admin.entity;

public class SecurityCaptchaSetting {
  private Long id;
  private String captchaType;
  private Integer dragCaptchaWidth;
  private Integer dragCaptchaHeight;
  private Integer dragCaptchaThreshold;
  private Integer imageCaptchaLength;
  private Integer imageCaptchaNoiseLines;
  private Boolean captchaEnabled;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public void setImageCaptchaNoiseLines(Integer imageCaptchaNoiseLines) {
    this.imageCaptchaNoiseLines = imageCaptchaNoiseLines;
  }

  public Integer getImageCaptchaNoiseLines() {
    return imageCaptchaNoiseLines;
  }

  public Boolean getCaptchaEnabled() {
    return captchaEnabled;
  }

  public void setCaptchaEnabled(Boolean captchaEnabled) {
    this.captchaEnabled = captchaEnabled;
  }
}
