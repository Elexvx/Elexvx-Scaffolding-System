package elexvx.admin.mapper;

import elexvx.admin.entity.SecurityTokenSetting;

public interface SecurityTokenSettingMapper {
  SecurityTokenSetting selectTop();
  int insert(SecurityTokenSetting setting);
  int update(SecurityTokenSetting setting);
}
