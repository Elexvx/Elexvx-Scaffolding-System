package com.tencent.tdesign.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.tencent.tdesign.entity.VerificationSetting;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SmsSenderService {
  private static final Logger log = LoggerFactory.getLogger(SmsSenderService.class);
  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public enum Provider {
    ALIYUN,
    TENCENT
  }

  public Provider resolveProvider(String raw) {
    if (raw == null || raw.isBlank()) return Provider.ALIYUN;
    try {
      return Provider.valueOf(raw.trim().toUpperCase());
    } catch (Exception e) {
      return Provider.ALIYUN;
    }
  }

  public void sendCode(VerificationSetting setting, String phone, String code, String sourceIp, String providerHint) {
    List<Provider> providers = resolveProviders(setting, providerHint);
    if (providers.isEmpty()) {
      throw badRequest("短信配置不完整");
    }
    RuntimeException lastError = null;
    for (Provider provider : providers) {
      try {
        switch (provider) {
          case TENCENT -> sendTencent(setting, phone, code);
          case ALIYUN -> sendAliyun(setting, phone, code, sourceIp);
        }
        return;
      } catch (RuntimeException ex) {
        lastError = ex;
        log.warn("SMS send failed via {}: {}", provider, ex.getMessage());
      }
    }
    if (lastError != null) throw lastError;
    throw badRequest("短信发送失败");
  }

  public List<Provider> resolveProviders(VerificationSetting setting, String providerHint) {
    List<Provider> providers = new ArrayList<>();
    if (providerHint != null && !providerHint.isBlank()) {
      Provider preferred = resolveProvider(providerHint);
      if (preferred == Provider.ALIYUN && isAliyunEnabled(setting)) {
        providers.add(Provider.ALIYUN);
      } else if (preferred == Provider.TENCENT && isTencentEnabled(setting)) {
        providers.add(Provider.TENCENT);
      }
      return providers;
    }
    Provider preferred = resolveProvider(setting == null ? null : setting.getSmsProvider());
    if (preferred == Provider.TENCENT) {
      if (isTencentEnabled(setting)) providers.add(Provider.TENCENT);
      if (isAliyunEnabled(setting)) providers.add(Provider.ALIYUN);
    } else {
      if (isAliyunEnabled(setting)) providers.add(Provider.ALIYUN);
      if (isTencentEnabled(setting)) providers.add(Provider.TENCENT);
    }
    return providers;
  }

  public boolean isAliyunEnabled(VerificationSetting setting) {
    if (setting == null) return false;
    if (Boolean.TRUE.equals(setting.getSmsAliyunEnabled())) return true;
    if (Boolean.FALSE.equals(setting.getSmsAliyunEnabled())) return false;
    return hasText(setting.getSmsAliyunAccessKeyId());
  }

  public boolean isTencentEnabled(VerificationSetting setting) {
    if (setting == null) return false;
    if (Boolean.TRUE.equals(setting.getSmsTencentEnabled())) return true;
    if (Boolean.FALSE.equals(setting.getSmsTencentEnabled())) return false;
    return hasText(setting.getSmsTencentSecretId())
      || hasText(setting.getSmsTencentTemplateId())
      || hasText(setting.getSmsSdkAppId());
  }

  private void sendAliyun(VerificationSetting setting, String phone, String code, String sourceIp) {
    String regionId = firstNonBlank(setting.getSmsAliyunRegionId(), "cn-hangzhou");
    IClientProfile profile = DefaultProfile.getProfile(
      regionId,
      firstNonBlank(setting.getSmsAliyunAccessKeyId(), null),
      firstNonBlank(setting.getSmsAliyunAccessKeySecret(), null)
    );
    String endpoint = firstNonBlank(setting.getSmsAliyunEndpoint(), "dysmsapi.aliyuncs.com");
    try {
      tryAddAliyunEndpoint(regionId, endpoint);
    } catch (Exception e) {
      log.warn("Failed to set Aliyun SMS endpoint: {}", e.getMessage());
    }
    IAcsClient client = new DefaultAcsClient(profile);
    com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest request =
      new com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest();
    request.setPhoneNumbers(normalizeAliyunPhone(phone));
    request.setSignName(firstNonBlank(setting.getSmsAliyunSignName(), null));
    request.setTemplateCode(firstNonBlank(setting.getSmsAliyunTemplateCode(), null));
    request.setTemplateParam("{\"code\":\"" + code + "\"}");
    if (sourceIp != null && !sourceIp.isBlank()) {
      request.setOutId(sourceIp);
    }
    try {
      com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse response = client.getAcsResponse(request);
      String respCode = response == null ? null : response.getCode();
      if (response == null || respCode == null || !"OK".equalsIgnoreCase(respCode)) {
        String message = response == null ? "unknown" : response.getMessage();
        throw badRequest("短信发送失败（阿里云）: " + message);
      }
    } catch (Exception e) {
      throw badRequest("短信发送失败（阿里云）: " + e.getMessage());
    }
  }

  private void sendTencent(VerificationSetting setting, String phone, String code) {
    String region = firstNonBlank(setting.getSmsTencentRegion(), "ap-guangzhou");
    Credential cred = new Credential(setting.getSmsTencentSecretId(), setting.getSmsTencentSecretKey());
    HttpProfile httpProfile = new HttpProfile();
    String endpoint = firstNonBlank(setting.getSmsTencentEndpoint(), "sms.tencentcloudapi.com");
    if (endpoint != null && !endpoint.isBlank()) {
      httpProfile.setEndpoint(endpoint.trim());
    }
    ClientProfile clientProfile = new ClientProfile();
    clientProfile.setHttpProfile(httpProfile);
    SmsClient client = new SmsClient(cred, region, clientProfile);
    SendSmsRequest request = new SendSmsRequest();
    request.setSmsSdkAppId(setting.getSmsSdkAppId());
    request.setSignName(setting.getSmsTencentSignName());
    request.setTemplateId(setting.getSmsTencentTemplateId());
    request.setPhoneNumberSet(new String[] { normalizeTencentPhone(phone) });
    request.setTemplateParamSet(new String[] { code });
    try {
      SendSmsResponse response = client.SendSms(request);
      SendStatus[] statuses = response == null ? null : response.getSendStatusSet();
      if (statuses == null || statuses.length == 0) {
        throw badRequest("短信发送失败（腾讯云）: empty response");
      }
      SendStatus status = statuses[0];
      if (status.getCode() == null || !"Ok".equalsIgnoreCase(status.getCode())) {
        String msg = status.getMessage() == null ? "unknown" : status.getMessage();
        throw badRequest("短信发送失败（腾讯云）: " + msg);
      }
    } catch (TencentCloudSDKException e) {
      throw badRequest("短信发送失败（腾讯云）: " + e.getMessage());
    }
  }

  @SuppressWarnings("deprecation")
  private void tryAddAliyunEndpoint(String regionId, String endpoint) throws Exception {
    DefaultProfile.addEndpoint(regionId, regionId, "Dysmsapi", endpoint);
  }

  private String normalizeAliyunPhone(String phone) {
    if (phone == null) return "";
    String cleaned = phone.trim().replaceAll("[\\s-]", "");
    if (cleaned.startsWith("+86")) cleaned = cleaned.substring(3);
    if (cleaned.startsWith("86") && cleaned.length() > 11) cleaned = cleaned.substring(2);
    return cleaned;
  }

  private String normalizeTencentPhone(String phone) {
    if (phone == null) return "";
    String cleaned = phone.trim().replaceAll("[\\s-]", "");
    if (cleaned.startsWith("+")) return cleaned;
    if (cleaned.startsWith("00")) return "+" + cleaned.substring(2);
    if (cleaned.length() == 11 && cleaned.startsWith("1")) return "+86" + cleaned;
    return "+" + cleaned;
  }

  private String firstNonBlank(String value, String fallback, String defaultValue) {
    if (value != null && !value.isBlank()) return value.trim();
    if (fallback != null && !fallback.isBlank()) return fallback.trim();
    return defaultValue;
  }

  private String firstNonBlank(String value, String fallback) {
    return firstNonBlank(value, fallback, null);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
