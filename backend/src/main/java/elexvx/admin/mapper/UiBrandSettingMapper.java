package elexvx.admin.mapper;

import elexvx.admin.entity.UiBrandSetting;

public interface UiBrandSettingMapper {
  UiBrandSetting selectTop();
  int insert(UiBrandSetting setting);
  int update(UiBrandSetting setting);
}
