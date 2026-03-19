package elexvx.admin.entity;

import java.time.LocalDateTime;

public class Announcement {
  private Long id;

  private String title;

  private String summary;

  private String content;

  private String type;

  private String priority;

  private String status;

  private String coverUrl;

  private String attachmentUrl;

  private String attachmentName;

  private LocalDateTime publishAt;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private Long createdById;

  private String createdByName;

  private boolean isBroadcasted = false;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public void setCoverUrl(String coverUrl) {
    this.coverUrl = coverUrl;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public void setAttachmentUrl(String attachmentUrl) {
    this.attachmentUrl = attachmentUrl;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public LocalDateTime getPublishAt() {
    return publishAt;
  }

  public void setPublishAt(LocalDateTime publishAt) {
    this.publishAt = publishAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long getCreatedById() {
    return createdById;
  }

  public void setCreatedById(Long createdById) {
    this.createdById = createdById;
  }

  public String getCreatedByName() {
    return createdByName;
  }

  public void setCreatedByName(String createdByName) {
    this.createdByName = createdByName;
  }

  public boolean isBroadcasted() {
    return isBroadcasted;
  }

  public void setBroadcasted(boolean broadcasted) {
    isBroadcasted = broadcasted;
  }
}
