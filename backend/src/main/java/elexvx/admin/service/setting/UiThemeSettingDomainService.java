package elexvx.admin.service.setting;

import elexvx.admin.entity.UiSetting;
import elexvx.admin.entity.UiThemeSetting;
import elexvx.admin.mapper.UiThemeSettingMapper;
import elexvx.admin.model.req.setting.UiThemeSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiThemeSettingDomainService {
  private final UiThemeSettingMapper mapper;

  public UiThemeSettingDomainService(UiThemeSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiThemeSetting theme = mapper.selectTop();
    if (theme == null) {
      return;
    }
    out.setAutoTheme(theme.getAutoTheme());
    out.setLightStartTime(theme.getLightStartTime());
    out.setDarkStartTime(theme.getDarkStartTime());
    out.setMode(theme.getMode());
    out.setBrandTheme(theme.getBrandTheme());
  }

  public void apply(UiThemeSettingRequest req) {
    UiThemeSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiThemeSetting();
    }
    boolean changed = false;
    if (req.autoTheme() != null) { s.setAutoTheme(req.autoTheme()); changed = true; }
    if (req.lightStartTime() != null) { s.setLightStartTime(req.lightStartTime()); changed = true; }
    if (req.darkStartTime() != null) { s.setDarkStartTime(req.darkStartTime()); changed = true; }
    if (req.mode() != null) { s.setMode(req.mode()); changed = true; }
    if (req.brandTheme() != null) { s.setBrandTheme(req.brandTheme()); changed = true; }
    if (changed) {
      upsert(s);
    }
  }

  private void upsert(UiThemeSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
