package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementUpsertRequest {
  @NotBlank(message = "标题不能为空")
  private String title;

  @NotBlank(message = "内容不能为空")
  private String content;

  @NotBlank(message = "类型不能为空")
  private String type;

  @NotBlank(message = "优先级不能为空")
  private String priority;

  private String summary;
  private String coverUrl;
  private String attachmentUrl;
  private String attachmentName;
  private String status;

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

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
