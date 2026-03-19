package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.SecurityTokenSetting;

public interface SecurityTokenSettingMapper {
  SecurityTokenSetting selectTop();
  int insert(SecurityTokenSetting setting);
  int update(SecurityTokenSetting setting);
}
