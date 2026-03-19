package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.SensitiveSetting;

public interface SensitiveSettingMapper {
  SensitiveSetting selectTop();
  int insert(SensitiveSetting setting);
  int update(SensitiveSetting setting);
}
