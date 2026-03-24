package elexvx.admin.mapper;

import elexvx.admin.entity.WatermarkSetting;

public interface WatermarkSettingMapper {
  WatermarkSetting selectTop();
  int insert(WatermarkSetting setting);
  int update(WatermarkSetting setting);
}
