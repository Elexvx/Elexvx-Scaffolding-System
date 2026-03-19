package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementRequest {
  @NotBlank(message = "内容不能为空")
  private String content;

  @NotBlank(message = "类型不能为空")
  private String type; // e.g. "notification", "announcement"

  @NotBlank(message = "级别不能为空")
  private String quality; // e.g. "low", "middle", "high"

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getQuality() { return quality; }
  public void setQuality(String quality) { this.quality = quality; }
}
