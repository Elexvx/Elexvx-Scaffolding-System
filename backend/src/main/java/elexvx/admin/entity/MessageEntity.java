package elexvx.admin.entity;

import java.time.LocalDateTime;

public class MessageEntity {
  private String id;

  private Long toUserId;

  private String content;

  private String type;

  private boolean status;

  private boolean collected;

  private String quality;

  private LocalDateTime createdAt;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public Long getToUserId() { return toUserId; }
  public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public boolean isStatus() { return status; }
  public void setStatus(boolean status) { this.status = status; }
  public boolean isCollected() { return collected; }
  public void setCollected(boolean collected) { this.collected = collected; }
  public String getQuality() { return quality; }
  public void setQuality(String quality) { this.quality = quality; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
