package elexvx.admin.vo;

import elexvx.admin.entity.SensitivePageSetting;
import java.util.List;

public class SensitiveSettingsResponse {
  private boolean enabled;
  private List<SensitivePageSetting> pages;

  public SensitiveSettingsResponse() {}

  public SensitiveSettingsResponse(boolean enabled, List<SensitivePageSetting> pages) {
    this.enabled = enabled;
    this.pages = pages;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<SensitivePageSetting> getPages() {
    return pages;
  }

  public void setPages(List<SensitivePageSetting> pages) {
    this.pages = pages;
  }
}
