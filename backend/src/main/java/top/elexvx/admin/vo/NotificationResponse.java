package top.elexvx.admin.vo;

import top.elexvx.admin.entity.Notification;
import java.time.LocalDateTime;

public class NotificationResponse {
  private Long id;
  private String title;
  private String summary;
  private String content;
  private String priority;
  private String status;
  private String type;
  private String coverUrl;
  private String attachmentUrl;
  private String attachmentName;
  private LocalDateTime publishAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdByName;

  public static NotificationResponse from(Notification n) {
    NotificationResponse r = new NotificationResponse();
    r.id = n.getId();
    r.title = n.getTitle();
    r.summary = n.getSummary();
    r.content = n.getContent();
    r.priority = n.getPriority();
    r.status = n.getStatus();
    r.type = n.getType();
    r.coverUrl = n.getCoverUrl();
    r.attachmentUrl = n.getAttachmentUrl();
    r.attachmentName = n.getAttachmentName();
    r.publishAt = n.getPublishAt();
    r.createdAt = n.getCreatedAt();
    r.updatedAt = n.getUpdatedAt();
    r.createdByName = n.getCreatedByName();
    return r;
  }

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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public String getCreatedByName() {
    return createdByName;
  }

  public void setCreatedByName(String createdByName) {
    this.createdByName = createdByName;
  }
}
