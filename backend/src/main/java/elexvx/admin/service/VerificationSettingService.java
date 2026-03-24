package elexvx.admin.service;

import elexvx.admin.entity.VerificationEmailSetting;
import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.entity.VerificationSmsSetting;
import elexvx.admin.mapper.VerificationEmailSettingMapper;
import elexvx.admin.mapper.VerificationSmsSettingMapper;
import elexvx.admin.model.req.setting.VerificationProviderSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class VerificationSettingService {
  private final VerificationSmsSettingMapper smsMapper;
  private final VerificationEmailSettingMapper emailMapper;
  private final SensitiveFieldCryptoService cryptoService;

  public VerificationSettingService(
    VerificationSmsSettingMapper smsMapper,
    VerificationEmailSettingMapper emailMapper,
    SensitiveFieldCryptoService cryptoService
  ) {
    this.smsMapper = smsMapper;
    this.emailMapper = emailMapper;
    this.cryptoService = cryptoService;
  }

  public VerificationSetting getOrCreate() {
    VerificationSetting out = new VerificationSetting();
    VerificationSmsSetting sms = getOrCreateSms();
    VerificationEmailSetting email = getOrCreateEmail();
    Long id = sms.getId() != null ? sms.getId() : email.getId();
    out.setId(id);
    out.setSmsEnabled(sms.getSmsEnabled());
    out.setSmsProvider(sms.getSmsProvider());
    out.setSmsAliyunEnabled(sms.getSmsAliyunEnabled());
    out.setSmsAliyunAccessKeyId(sms.getSmsAliyunAccessKeyId());
    out.setSmsAliyunAccessKeySecret(sms.getSmsAliyunAccessKeySecret());
    out.setSmsAliyunSignName(sms.getSmsAliyunSignName());
    out.setSmsAliyunTemplateCode(sms.getSmsAliyunTemplateCode());
    out.setSmsAliyunRegionId(sms.getSmsAliyunRegionId());
    out.setSmsAliyunEndpoint(sms.getSmsAliyunEndpoint());
    out.setSmsTencentEnabled(sms.getSmsTencentEnabled());
    out.setSmsTencentSecretId(sms.getSmsTencentSecretId());
    out.setSmsTencentSecretKey(sms.getSmsTencentSecretKey());
    out.setSmsTencentSignName(sms.getSmsTencentSignName());
    out.setSmsTencentTemplateId(sms.getSmsTencentTemplateId());
    out.setSmsTencentRegion(sms.getSmsTencentRegion());
    out.setSmsTencentEndpoint(sms.getSmsTencentEndpoint());
    out.setSmsSdkAppId(sms.getSmsSdkAppId());
    out.setEmailEnabled(email.getEmailEnabled());
    out.setEmailHost(email.getEmailHost());
    out.setEmailPort(email.getEmailPort());
    out.setEmailUsername(email.getEmailUsername());
    out.setEmailPassword(email.getEmailPassword());
    out.setEmailFrom(email.getEmailFrom());
    out.setEmailSsl(email.getEmailSsl());
    return out;
  }

  public VerificationSetting getDecryptedCopy() {
    VerificationSetting current = getOrCreate();
    VerificationSetting copy = copyOf(current);
    copy.setSmsAliyunAccessKeyId(cryptoService.decryptIfNeeded(copy.getSmsAliyunAccessKeyId()));
    copy.setSmsAliyunAccessKeySecret(cryptoService.decryptIfNeeded(copy.getSmsAliyunAccessKeySecret()));
    copy.setSmsTencentSecretId(cryptoService.decryptIfNeeded(copy.getSmsTencentSecretId()));
    copy.setSmsTencentSecretKey(cryptoService.decryptIfNeeded(copy.getSmsTencentSecretKey()));
    copy.setEmailPassword(cryptoService.decryptIfNeeded(copy.getEmailPassword()));
    return copy;
  }

  public boolean applyRequest(VerificationProviderSettingRequest req) {
    boolean changed = false;

    VerificationSmsSetting sms = smsMapper.selectTop();
    if (sms == null) sms = new VerificationSmsSetting();
    boolean smsChanged = false;
    if (req.smsEnabled() != null) {
      sms.setSmsEnabled(req.smsEnabled());
      smsChanged = true;
    }
    if (req.smsProvider() != null) {
      sms.setSmsProvider(req.smsProvider());
      smsChanged = true;
    }
    if (req.smsAliyunEnabled() != null) {
      sms.setSmsAliyunEnabled(req.smsAliyunEnabled());
      smsChanged = true;
    }
    if (req.smsAliyunAccessKeyId() != null) {
      sms.setSmsAliyunAccessKeyId(cryptoService.encryptIfNeeded(req.smsAliyunAccessKeyId()));
      smsChanged = true;
    }
    if (req.smsAliyunAccessKeySecret() != null) {
      sms.setSmsAliyunAccessKeySecret(cryptoService.encryptIfNeeded(req.smsAliyunAccessKeySecret()));
      smsChanged = true;
    }
    if (req.smsAliyunSignName() != null) {
      sms.setSmsAliyunSignName(req.smsAliyunSignName());
      smsChanged = true;
    }
    if (req.smsAliyunTemplateCode() != null) {
      sms.setSmsAliyunTemplateCode(req.smsAliyunTemplateCode());
      smsChanged = true;
    }
    if (req.smsAliyunRegionId() != null) {
      sms.setSmsAliyunRegionId(req.smsAliyunRegionId());
      smsChanged = true;
    }
    if (req.smsAliyunEndpoint() != null) {
      sms.setSmsAliyunEndpoint(req.smsAliyunEndpoint());
      smsChanged = true;
    }
    if (req.smsTencentEnabled() != null) {
      sms.setSmsTencentEnabled(req.smsTencentEnabled());
      smsChanged = true;
    }
    if (req.smsTencentSecretId() != null) {
      sms.setSmsTencentSecretId(cryptoService.encryptIfNeeded(req.smsTencentSecretId()));
      smsChanged = true;
    }
    if (req.smsTencentSecretKey() != null) {
      sms.setSmsTencentSecretKey(cryptoService.encryptIfNeeded(req.smsTencentSecretKey()));
      smsChanged = true;
    }
    if (req.smsTencentSignName() != null) {
      sms.setSmsTencentSignName(req.smsTencentSignName());
      smsChanged = true;
    }
    if (req.smsTencentTemplateId() != null) {
      sms.setSmsTencentTemplateId(req.smsTencentTemplateId());
      smsChanged = true;
    }
    if (req.smsTencentRegion() != null) {
      sms.setSmsTencentRegion(req.smsTencentRegion());
      smsChanged = true;
    }
    if (req.smsTencentEndpoint() != null) {
      sms.setSmsTencentEndpoint(req.smsTencentEndpoint());
      smsChanged = true;
    }
    if (req.smsSdkAppId() != null) {
      sms.setSmsSdkAppId(req.smsSdkAppId());
      smsChanged = true;
    }
    if (smsChanged) {
      upsertSms(sms);
      changed = true;
    }

    VerificationEmailSetting email = emailMapper.selectTop();
    if (email == null) email = new VerificationEmailSetting();
    boolean emailChanged = false;
    if (req.emailEnabled() != null) {
      email.setEmailEnabled(req.emailEnabled());
      emailChanged = true;
    }
    if (req.emailHost() != null) {
      email.setEmailHost(req.emailHost());
      emailChanged = true;
    }
    if (req.emailPort() != null) {
      email.setEmailPort(req.emailPort());
      emailChanged = true;
    }
    if (req.emailUsername() != null) {
      email.setEmailUsername(req.emailUsername());
      emailChanged = true;
    }
    if (req.emailPassword() != null) {
      email.setEmailPassword(cryptoService.encryptIfNeeded(req.emailPassword()));
      emailChanged = true;
    }
    if (req.emailFrom() != null) {
      email.setEmailFrom(req.emailFrom());
      emailChanged = true;
    }
    if (req.emailSsl() != null) {
      email.setEmailSsl(req.emailSsl());
      emailChanged = true;
    }
    if (emailChanged) {
      upsertEmail(email);
      changed = true;
    }

    return changed;
  }

  private VerificationSmsSetting getOrCreateSms() {
    VerificationSmsSetting sms = smsMapper.selectTop();
    if (sms == null) {
      sms = new VerificationSmsSetting();
      smsMapper.insert(sms);
    }
    return sms;
  }

  private VerificationEmailSetting getOrCreateEmail() {
    VerificationEmailSetting email = emailMapper.selectTop();
    if (email == null) {
      email = new VerificationEmailSetting();
      emailMapper.insert(email);
    }
    return email;
  }

  private void upsertSms(VerificationSmsSetting setting) {
    if (setting.getId() == null) smsMapper.insert(setting);
    else smsMapper.update(setting);
  }

  private void upsertEmail(VerificationEmailSetting setting) {
    if (setting.getId() == null) emailMapper.insert(setting);
    else emailMapper.update(setting);
  }

  private VerificationSetting copyOf(VerificationSetting source) {
    VerificationSetting copy = new VerificationSetting();
    copy.setId(source.getId());
    copy.setSmsEnabled(source.getSmsEnabled());
    copy.setSmsProvider(source.getSmsProvider());
    copy.setSmsAliyunEnabled(source.getSmsAliyunEnabled());
    copy.setSmsAliyunAccessKeyId(source.getSmsAliyunAccessKeyId());
    copy.setSmsAliyunAccessKeySecret(source.getSmsAliyunAccessKeySecret());
    copy.setSmsAliyunSignName(source.getSmsAliyunSignName());
    copy.setSmsAliyunTemplateCode(source.getSmsAliyunTemplateCode());
    copy.setSmsAliyunRegionId(source.getSmsAliyunRegionId());
    copy.setSmsAliyunEndpoint(source.getSmsAliyunEndpoint());
    copy.setSmsTencentEnabled(source.getSmsTencentEnabled());
    copy.setSmsTencentSecretId(source.getSmsTencentSecretId());
    copy.setSmsTencentSecretKey(source.getSmsTencentSecretKey());
    copy.setSmsTencentSignName(source.getSmsTencentSignName());
    copy.setSmsTencentTemplateId(source.getSmsTencentTemplateId());
    copy.setSmsTencentRegion(source.getSmsTencentRegion());
    copy.setSmsTencentEndpoint(source.getSmsTencentEndpoint());
    copy.setSmsSdkAppId(source.getSmsSdkAppId());
    copy.setEmailEnabled(source.getEmailEnabled());
    copy.setEmailHost(source.getEmailHost());
    copy.setEmailPort(source.getEmailPort());
    copy.setEmailUsername(source.getEmailUsername());
    copy.setEmailPassword(source.getEmailPassword());
    copy.setEmailFrom(source.getEmailFrom());
    copy.setEmailSsl(source.getEmailSsl());
    return copy;
  }
}
