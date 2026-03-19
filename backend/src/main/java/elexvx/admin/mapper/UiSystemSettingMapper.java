package elexvx.admin.mapper;

import elexvx.admin.entity.UiSystemSetting;

public interface UiSystemSettingMapper {
  UiSystemSetting selectTop();
  int insert(UiSystemSetting setting);
  int update(UiSystemSetting setting);
}
