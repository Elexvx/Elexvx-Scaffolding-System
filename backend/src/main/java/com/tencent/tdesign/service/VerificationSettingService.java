package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.UiSettingRequest;
import com.tencent.tdesign.entity.VerificationEmailSetting;
import com.tencent.tdesign.entity.VerificationSetting;
import com.tencent.tdesign.entity.VerificationSmsSetting;
import com.tencent.tdesign.mapper.VerificationEmailSettingMapper;
import com.tencent.tdesign.mapper.VerificationSmsSettingMapper;
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

  public boolean applyRequest(UiSettingRequest req) {
    boolean changed = false;

    VerificationSmsSetting sms = smsMapper.selectTop();
    if (sms == null) sms = new VerificationSmsSetting();
    boolean smsChanged = false;
    if (req.getSmsEnabled() != null) {
      sms.setSmsEnabled(req.getSmsEnabled());
      smsChanged = true;
    }
    if (req.getSmsProvider() != null) {
      sms.setSmsProvider(req.getSmsProvider());
      smsChanged = true;
    }
    if (req.getSmsAliyunEnabled() != null) {
      sms.setSmsAliyunEnabled(req.getSmsAliyunEnabled());
      smsChanged = true;
    }
    if (req.getSmsAliyunAccessKeyId() != null) {
      sms.setSmsAliyunAccessKeyId(cryptoService.encryptIfNeeded(req.getSmsAliyunAccessKeyId()));
      smsChanged = true;
    }
    if (req.getSmsAliyunAccessKeySecret() != null) {
      sms.setSmsAliyunAccessKeySecret(cryptoService.encryptIfNeeded(req.getSmsAliyunAccessKeySecret()));
      smsChanged = true;
    }
    if (req.getSmsAliyunSignName() != null) {
      sms.setSmsAliyunSignName(req.getSmsAliyunSignName());
      smsChanged = true;
    }
    if (req.getSmsAliyunTemplateCode() != null) {
      sms.setSmsAliyunTemplateCode(req.getSmsAliyunTemplateCode());
      smsChanged = true;
    }
    if (req.getSmsAliyunRegionId() != null) {
      sms.setSmsAliyunRegionId(req.getSmsAliyunRegionId());
      smsChanged = true;
    }
    if (req.getSmsAliyunEndpoint() != null) {
      sms.setSmsAliyunEndpoint(req.getSmsAliyunEndpoint());
      smsChanged = true;
    }
    if (req.getSmsTencentEnabled() != null) {
      sms.setSmsTencentEnabled(req.getSmsTencentEnabled());
      smsChanged = true;
    }
    if (req.getSmsTencentSecretId() != null) {
      sms.setSmsTencentSecretId(cryptoService.encryptIfNeeded(req.getSmsTencentSecretId()));
      smsChanged = true;
    }
    if (req.getSmsTencentSecretKey() != null) {
      sms.setSmsTencentSecretKey(cryptoService.encryptIfNeeded(req.getSmsTencentSecretKey()));
      smsChanged = true;
    }
    if (req.getSmsTencentSignName() != null) {
      sms.setSmsTencentSignName(req.getSmsTencentSignName());
      smsChanged = true;
    }
    if (req.getSmsTencentTemplateId() != null) {
      sms.setSmsTencentTemplateId(req.getSmsTencentTemplateId());
      smsChanged = true;
    }
    if (req.getSmsTencentRegion() != null) {
      sms.setSmsTencentRegion(req.getSmsTencentRegion());
      smsChanged = true;
    }
    if (req.getSmsTencentEndpoint() != null) {
      sms.setSmsTencentEndpoint(req.getSmsTencentEndpoint());
      smsChanged = true;
    }
    if (req.getSmsSdkAppId() != null) {
      sms.setSmsSdkAppId(req.getSmsSdkAppId());
      smsChanged = true;
    }
    if (smsChanged) {
      upsertSms(sms);
      changed = true;
    }

    VerificationEmailSetting email = emailMapper.selectTop();
    if (email == null) email = new VerificationEmailSetting();
    boolean emailChanged = false;
    if (req.getEmailEnabled() != null) {
      email.setEmailEnabled(req.getEmailEnabled());
      emailChanged = true;
    }
    if (req.getEmailHost() != null) {
      email.setEmailHost(req.getEmailHost());
      emailChanged = true;
    }
    if (req.getEmailPort() != null) {
      email.setEmailPort(req.getEmailPort());
      emailChanged = true;
    }
    if (req.getEmailUsername() != null) {
      email.setEmailUsername(req.getEmailUsername());
      emailChanged = true;
    }
    if (req.getEmailPassword() != null) {
      email.setEmailPassword(cryptoService.encryptIfNeeded(req.getEmailPassword()));
      emailChanged = true;
    }
    if (req.getEmailFrom() != null) {
      email.setEmailFrom(req.getEmailFrom());
      emailChanged = true;
    }
    if (req.getEmailSsl() != null) {
      email.setEmailSsl(req.getEmailSsl());
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
