package elexvx.admin.mapper;

import elexvx.admin.entity.VerificationEmailSetting;

public interface VerificationEmailSettingMapper {
  VerificationEmailSetting selectTop();
  int insert(VerificationEmailSetting setting);
  int update(VerificationEmailSetting setting);
}
