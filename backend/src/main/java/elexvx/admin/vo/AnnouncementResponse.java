package elexvx.admin.vo;

import elexvx.admin.entity.Announcement;
import java.time.LocalDateTime;

public class AnnouncementResponse {
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
  private String createdByName;

  public static AnnouncementResponse from(Announcement a) {
    AnnouncementResponse r = new AnnouncementResponse();
    r.id = a.getId();
    r.title = a.getTitle();
    r.summary = a.getSummary();
    r.content = a.getContent();
    r.type = a.getType();
    r.priority = a.getPriority();
    r.status = a.getStatus();
    r.coverUrl = a.getCoverUrl();
    r.attachmentUrl = a.getAttachmentUrl();
    r.attachmentName = a.getAttachmentName();
    r.publishAt = a.getPublishAt();
    r.createdAt = a.getCreatedAt();
    r.updatedAt = a.getUpdatedAt();
    r.createdByName = a.getCreatedByName();
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

  public String getCreatedByName() {
    return createdByName;
  }

  public void setCreatedByName(String createdByName) {
    this.createdByName = createdByName;
  }
}
