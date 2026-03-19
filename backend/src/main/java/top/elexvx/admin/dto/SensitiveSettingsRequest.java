package top.elexvx.admin.dto;

import java.util.List;

public class SensitiveSettingsRequest {
  private Boolean enabled;
  private List<SensitivePageSettingRequest> pages;

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public List<SensitivePageSettingRequest> getPages() {
    return pages;
  }

  public void setPages(List<SensitivePageSettingRequest> pages) {
    this.pages = pages;
  }
}
