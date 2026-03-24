package elexvx.admin.mapper;

import elexvx.admin.entity.UiThemeSetting;

public interface UiThemeSettingMapper {
  UiThemeSetting selectTop();
  int insert(UiThemeSetting setting);
  int update(UiThemeSetting setting);
}
