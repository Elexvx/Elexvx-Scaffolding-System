package top.elexvx.admin.vo;

import top.elexvx.admin.entity.Notification;
import java.time.LocalDateTime;

public class NotificationSummary {
  private Long id;
  private String title;
  private String summary;
  private String priority;
  private String status;
  private LocalDateTime publishAt;
  private LocalDateTime updatedAt;

  public static NotificationSummary from(Notification n) {
    NotificationSummary r = new NotificationSummary();
    r.id = n.getId();
    r.title = n.getTitle();
    r.summary = n.getSummary();
    r.priority = n.getPriority();
    r.status = n.getStatus();
    r.publishAt = n.getPublishAt();
    r.updatedAt = n.getUpdatedAt();
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

  public LocalDateTime getPublishAt() {
    return publishAt;
  }

  public void setPublishAt(LocalDateTime publishAt) {
    this.publishAt = publishAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
