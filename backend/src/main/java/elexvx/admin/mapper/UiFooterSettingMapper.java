package elexvx.admin.mapper;

import elexvx.admin.entity.UiFooterSetting;

public interface UiFooterSettingMapper {
  UiFooterSetting selectTop();
  int insert(UiFooterSetting setting);
  int update(UiFooterSetting setting);
}
