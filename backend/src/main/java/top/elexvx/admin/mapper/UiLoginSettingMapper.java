package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiLoginSetting;

public interface UiLoginSettingMapper {
  UiLoginSetting selectTop();
  int insert(UiLoginSetting setting);
  int update(UiLoginSetting setting);
}
