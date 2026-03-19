package top.elexvx.admin.verification;

import top.elexvx.admin.entity.VerificationSetting;
import top.elexvx.admin.service.SmsCodeService;
import top.elexvx.admin.service.SmsSenderService;
import org.springframework.stereotype.Service;

@Service
public class SmsVerificationProvider implements VerificationProvider {
  private final SmsCodeService smsCodeService;
  private final SmsSenderService smsSenderService;

  public SmsVerificationProvider(SmsCodeService smsCodeService, SmsSenderService smsSenderService) {
    this.smsCodeService = smsCodeService;
    this.smsSenderService = smsSenderService;
  }

  @Override
  public String getType() {
    return "sms";
  }

  @Override
  public int getExpiresInSeconds() {
    return smsCodeService.getExpiresInSeconds();
  }

  @Override
  public void sendCode(VerificationSetting setting, String target, String sourceIp, String providerHint) {
    String code = smsCodeService.generateCode(target);
    try {
      smsSenderService.sendCode(setting, target, code, sourceIp, providerHint);
    } catch (Exception e) {
      smsCodeService.invalidate(target);
      throw e;
    }
  }

  @Override
  public boolean verify(String target, String code) {
    return smsCodeService.verify(target, code);
  }

  @Override
  public void invalidate(String target) {
    smsCodeService.invalidate(target);
  }
}
