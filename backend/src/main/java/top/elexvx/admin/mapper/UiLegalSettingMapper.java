package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiLegalSetting;

public interface UiLegalSettingMapper {
  UiLegalSetting selectTop();
  int insert(UiLegalSetting setting);
  int update(UiLegalSetting setting);
}
