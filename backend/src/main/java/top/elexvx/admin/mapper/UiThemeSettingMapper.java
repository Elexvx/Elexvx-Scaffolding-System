package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiThemeSetting;

public interface UiThemeSettingMapper {
  UiThemeSetting selectTop();
  int insert(UiThemeSetting setting);
  int update(UiThemeSetting setting);
}
