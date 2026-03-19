package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageSendRequest {
  @NotNull
  private Long toUserId;

  @NotBlank
  private String content;

  @NotBlank
  private String type;

  @NotBlank
  private String quality;

  public Long getToUserId() { return toUserId; }
  public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getQuality() { return quality; }
  public void setQuality(String quality) { this.quality = quality; }
}
