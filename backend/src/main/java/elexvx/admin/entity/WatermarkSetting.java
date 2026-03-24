package elexvx.admin.entity;


public class WatermarkSetting {
  private Long id;

  private String type;

  private String content;

  private String imageUrl;

  private Double opacity = 0.12;

  private Integer size = 120;

  private Integer gapX = 200;

  private Integer gapY = 200;

  private Integer rotate = 20;

  private Boolean enabled = false;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public Double getOpacity() { return opacity; }
  public void setOpacity(Double opacity) { this.opacity = opacity; }
  public Integer getSize() { return size; }
  public void setSize(Integer size) { this.size = size; }
  public Integer getGapX() { return gapX; }
  public void setGapX(Integer gapX) { this.gapX = gapX; }
  public Integer getGapY() { return gapY; }
  public void setGapY(Integer gapY) { this.gapY = gapY; }
  public Integer getRotate() { return rotate; }
  public void setRotate(Integer rotate) { this.rotate = rotate; }
  public Boolean getEnabled() { return enabled; }
  public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
