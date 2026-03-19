package elexvx.admin.mapper;

import elexvx.admin.entity.UiLegalSetting;

public interface UiLegalSettingMapper {
  UiLegalSetting selectTop();
  int insert(UiLegalSetting setting);
  int update(UiLegalSetting setting);
}
