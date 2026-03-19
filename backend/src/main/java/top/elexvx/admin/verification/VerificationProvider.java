package top.elexvx.admin.verification;

import top.elexvx.admin.entity.VerificationSetting;

public interface VerificationProvider {
  String getType();

  int getExpiresInSeconds();

  void sendCode(VerificationSetting setting, String target, String sourceIp, String providerHint);

  boolean verify(String target, String code);

  void invalidate(String target);
}
