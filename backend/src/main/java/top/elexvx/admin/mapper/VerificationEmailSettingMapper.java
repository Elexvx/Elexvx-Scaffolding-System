package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.VerificationEmailSetting;

public interface VerificationEmailSettingMapper {
  VerificationEmailSetting selectTop();
  int insert(VerificationEmailSetting setting);
  int update(VerificationEmailSetting setting);
}
