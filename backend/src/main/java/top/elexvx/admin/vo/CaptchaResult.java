package top.elexvx.admin.vo;

public class CaptchaResult {
  private String id;
  private String image;
  private Integer expiresIn;
  private String type;
  private String dragToken;
  private Integer width;
  private Integer height;
  private Integer threshold;
  private Boolean enabled;

  public CaptchaResult() {
  }

  public CaptchaResult(String id, String image) {
    this.id = id;
    this.image = image;
  }

  public CaptchaResult(String id, String image, Integer expiresIn) {
    this.id = id;
    this.image = image;
    this.expiresIn = expiresIn;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDragToken() {
    return dragToken;
  }

  public void setDragToken(String dragToken) {
    this.dragToken = dragToken;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public Integer getThreshold() {
    return threshold;
  }

  public void setThreshold(Integer threshold) {
    this.threshold = threshold;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}
