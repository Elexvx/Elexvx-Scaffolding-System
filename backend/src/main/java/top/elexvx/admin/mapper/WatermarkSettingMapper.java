package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.WatermarkSetting;

public interface WatermarkSettingMapper {
  WatermarkSetting selectTop();
  int insert(WatermarkSetting setting);
  int update(WatermarkSetting setting);
}
