package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WatermarkSettingRequest {
  @NotBlank
  private String type;
  private String content;
  private String imageUrl;
  @NotNull
  private Double opacity;
  @NotNull
  private Integer size;
  @NotNull
  private Integer gapX;
  @NotNull
  private Integer gapY;
  @NotNull
  private Integer rotate;
  @NotNull
  private Boolean enabled;

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
