package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UiSystemSetting;

public interface UiSystemSettingMapper {
  UiSystemSetting selectTop();
  int insert(UiSystemSetting setting);
  int update(UiSystemSetting setting);
}
