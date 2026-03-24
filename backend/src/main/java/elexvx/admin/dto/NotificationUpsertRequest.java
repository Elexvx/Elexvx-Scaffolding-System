package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationUpsertRequest {
  @NotBlank(message = "\u6807\u9898\u4e0d\u80fd\u4e3a\u7a7a")
  private String title;

  @NotBlank(message = "\u5185\u5bb9\u4e0d\u80fd\u4e3a\u7a7a")
  private String content;

  @NotBlank(message = "\u4f18\u5148\u7ea7\u4e0d\u80fd\u4e3a\u7a7a")
  private String priority;

  private String summary;
  private String status;
  private String type;
  private String coverUrl;
  private String attachmentUrl;
  private String attachmentName;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
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
}
