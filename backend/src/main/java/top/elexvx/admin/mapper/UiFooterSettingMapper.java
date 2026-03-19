package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiFooterSetting;

public interface UiFooterSettingMapper {
  UiFooterSetting selectTop();
  int insert(UiFooterSetting setting);
  int update(UiFooterSetting setting);
}
