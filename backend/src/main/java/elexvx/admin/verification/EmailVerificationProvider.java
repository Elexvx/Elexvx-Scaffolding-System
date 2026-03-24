package elexvx.admin.verification;

import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.service.EmailCodeService;
import elexvx.admin.service.EmailSenderService;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationProvider implements VerificationProvider {
  private final EmailCodeService emailCodeService;
  private final EmailSenderService emailSenderService;

  public EmailVerificationProvider(EmailCodeService emailCodeService, EmailSenderService emailSenderService) {
    this.emailCodeService = emailCodeService;
    this.emailSenderService = emailSenderService;
  }

  @Override
  public String getType() {
    return "email";
  }

  @Override
  public int getExpiresInSeconds() {
    return emailCodeService.getExpiresInSeconds();
  }

  @Override
  public void sendCode(VerificationSetting setting, String target, String sourceIp, String providerHint) {
    String code = emailCodeService.generateCode(target);
    try {
      emailSenderService.sendLoginCode(setting, target, code, emailCodeService.getExpiresInSeconds());
    } catch (Exception e) {
      emailCodeService.invalidate(target);
      throw e;
    }
  }

  @Override
  public boolean verify(String target, String code) {
    return emailCodeService.verify(target, code);
  }

  @Override
  public void invalidate(String target) {
    emailCodeService.invalidate(target);
  }
}
