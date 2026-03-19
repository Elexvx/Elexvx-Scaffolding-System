package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiLayoutSetting;

public interface UiLayoutSettingMapper {
  UiLayoutSetting selectTop();
  int insert(UiLayoutSetting setting);
  int update(UiLayoutSetting setting);
}
