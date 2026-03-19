package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiBrandSetting;

public interface UiBrandSettingMapper {
  UiBrandSetting selectTop();
  int insert(UiBrandSetting setting);
  int update(UiBrandSetting setting);
}
