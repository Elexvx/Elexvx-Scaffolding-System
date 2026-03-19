package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class SensitivePageSettingRequest {
  @NotBlank(message = "页面标识不能为空")
  private String pageKey;

  private String pageName;
  private Boolean enabled;

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

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}
