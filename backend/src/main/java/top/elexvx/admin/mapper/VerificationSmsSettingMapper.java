package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.VerificationSmsSetting;

public interface VerificationSmsSettingMapper {
  VerificationSmsSetting selectTop();
  int insert(VerificationSmsSetting setting);
  int update(VerificationSmsSetting setting);
}
