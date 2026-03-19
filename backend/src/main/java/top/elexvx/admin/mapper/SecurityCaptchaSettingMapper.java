package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.SecurityCaptchaSetting;

public interface SecurityCaptchaSettingMapper {
  SecurityCaptchaSetting selectTop();
  int insert(SecurityCaptchaSetting setting);
  int update(SecurityCaptchaSetting setting);
}
