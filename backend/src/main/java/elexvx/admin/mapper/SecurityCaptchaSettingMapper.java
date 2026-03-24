package elexvx.admin.mapper;

import elexvx.admin.entity.SecurityCaptchaSetting;

public interface SecurityCaptchaSettingMapper {
  SecurityCaptchaSetting selectTop();
  int insert(SecurityCaptchaSetting setting);
  int update(SecurityCaptchaSetting setting);
}
