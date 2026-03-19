package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationBroadcastRequest {
  @NotBlank(message = "\u6807\u9898\u4e0d\u80fd\u4e3a\u7a7a")
  private String title;

  @NotBlank(message = "\u5185\u5bb9\u4e0d\u80fd\u4e3a\u7a7a")
  private String content;

  @NotBlank(message = "\u4f18\u5148\u7ea7\u4e0d\u80fd\u4e3a\u7a7a")
  private String priority;

  private String summary;

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
}
