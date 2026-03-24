package elexvx.admin.mapper;

import elexvx.admin.entity.UiLayoutSetting;

public interface UiLayoutSettingMapper {
  UiLayoutSetting selectTop();
  int insert(UiLayoutSetting setting);
  int update(UiLayoutSetting setting);
}
