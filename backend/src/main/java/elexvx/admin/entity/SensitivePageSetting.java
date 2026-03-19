package elexvx.admin.entity;

import java.time.LocalDateTime;

public class SensitivePageSetting {
  private Long id;

  private String pageKey;

  private String pageName;

  private boolean enabled = false;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPageKey() {
    return pageKey;
  }

  public void setPageKey(String pageKey) {
    this.pageKey = pageKey;
  }

  public String getPageName() {
    return pageName;
  }

  public void setPageName(String pageName) {
    this.pageName = pageName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
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
}
