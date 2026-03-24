package elexvx.admin.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import elexvx.admin.entity.SecurityCaptchaSetting;
import elexvx.admin.entity.SecurityPasswordPolicy;
import elexvx.admin.entity.SecuritySetting;
import elexvx.admin.entity.SecurityTokenSetting;
import elexvx.admin.mapper.SecurityCaptchaSettingMapper;
import elexvx.admin.mapper.SecurityPasswordPolicyMapper;
import elexvx.admin.mapper.SecurityTokenSettingMapper;
import elexvx.admin.model.req.setting.SecurityPolicySettingRequest;
import elexvx.admin.model.req.setting.SessionPolicySettingRequest;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class SecuritySettingService {
  private static final String CACHE_KEY = "security:setting";

  private final SecurityTokenSettingMapper tokenMapper;
  private final SecurityCaptchaSettingMapper captchaMapper;
  private final SecurityPasswordPolicyMapper passwordMapper;
  private final Cache<String, SecuritySetting> settingCache =
    Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(15)).maximumSize(1).build();

  public SecuritySettingService(
      SecurityTokenSettingMapper tokenMapper,
      SecurityCaptchaSettingMapper captchaMapper,
      SecurityPasswordPolicyMapper passwordMapper) {
    this.tokenMapper = tokenMapper;
    this.captchaMapper = captchaMapper;
    this.passwordMapper = passwordMapper;
  }

  public SecuritySetting getOrCreate() {
    return settingCache.get(CACHE_KEY, key -> loadOrCreate());
  }

  public boolean applyRequest(SessionPolicySettingRequest sessionReq, SecurityPolicySettingRequest securityReq) {
    boolean changed = false;

    SecurityTokenSetting token = tokenMapper.selectTop();
    if (token == null)
      token = new SecurityTokenSetting();
    boolean tokenChanged = false;
    if (sessionReq.sessionTimeoutMinutes() != null) {
      token.setSessionTimeoutMinutes(sessionReq.sessionTimeoutMinutes());
      tokenChanged = true;
    }
    if (sessionReq.tokenTimeoutMinutes() != null) {
      token.setTokenTimeoutMinutes(sessionReq.tokenTimeoutMinutes());
      tokenChanged = true;
    }
    if (sessionReq.tokenRefreshGraceMinutes() != null) {
      token.setTokenRefreshGraceMinutes(sessionReq.tokenRefreshGraceMinutes());
      tokenChanged = true;
    }
    if (sessionReq.allowUrlTokenParam() != null) {
      token.setAllowUrlTokenParam(sessionReq.allowUrlTokenParam());
      tokenChanged = true;
    }
    if (tokenChanged) {
      upsertToken(token);
      changed = true;
    }

    SecurityCaptchaSetting captcha = captchaMapper.selectTop();
    if (captcha == null)
      captcha = new SecurityCaptchaSetting();
    boolean captchaChanged = false;
    if (securityReq.captchaEnabled() != null) {
      captcha.setCaptchaEnabled(securityReq.captchaEnabled());
      captchaChanged = true;
    }
    if (securityReq.captchaType() != null) {
      captcha.setCaptchaType(securityReq.captchaType());
      captchaChanged = true;
    }
    if (securityReq.dragCaptchaWidth() != null) {
      captcha.setDragCaptchaWidth(securityReq.dragCaptchaWidth());
      captchaChanged = true;
    }
    if (securityReq.dragCaptchaHeight() != null) {
      captcha.setDragCaptchaHeight(securityReq.dragCaptchaHeight());
      captchaChanged = true;
    }
    if (securityReq.dragCaptchaThreshold() != null) {
      captcha.setDragCaptchaThreshold(securityReq.dragCaptchaThreshold());
      captchaChanged = true;
    }
    if (securityReq.imageCaptchaLength() != null) {
      captcha.setImageCaptchaLength(securityReq.imageCaptchaLength());
      captchaChanged = true;
    }
    if (securityReq.imageCaptchaNoiseLines() != null) {
      captcha.setImageCaptchaNoiseLines(securityReq.imageCaptchaNoiseLines());
      captchaChanged = true;
    }
    if (captchaChanged) {
      upsertCaptcha(captcha);
      changed = true;
    }

    SecurityPasswordPolicy policy = passwordMapper.selectTop();
    if (policy == null)
      policy = new SecurityPasswordPolicy();
    boolean policyChanged = false;
    if (securityReq.passwordMinLength() != null) {
      policy.setPasswordMinLength(securityReq.passwordMinLength());
      policyChanged = true;
    }
    if (securityReq.passwordRequireUppercase() != null) {
      policy.setPasswordRequireUppercase(securityReq.passwordRequireUppercase());
      policyChanged = true;
    }
    if (securityReq.passwordRequireLowercase() != null) {
      policy.setPasswordRequireLowercase(securityReq.passwordRequireLowercase());
      policyChanged = true;
    }
    if (securityReq.passwordRequireSpecial() != null) {
      policy.setPasswordRequireSpecial(securityReq.passwordRequireSpecial());
      policyChanged = true;
    }
    if (securityReq.passwordAllowSequential() != null) {
      policy.setPasswordAllowSequential(securityReq.passwordAllowSequential());
      policyChanged = true;
    }
    if (policyChanged) {
      upsertPasswordPolicy(policy);
      changed = true;
    }

    if (changed) {
      settingCache.invalidate(CACHE_KEY);
    }
    return changed;
  }

  private SecuritySetting loadOrCreate() {
    SecuritySetting out = new SecuritySetting();
    SecurityTokenSetting token = getOrCreateToken();
    out.setSessionTimeoutMinutes(token.getSessionTimeoutMinutes());
    out.setTokenTimeoutMinutes(token.getTokenTimeoutMinutes());
    out.setTokenRefreshGraceMinutes(token.getTokenRefreshGraceMinutes());
    out.setAllowUrlTokenParam(token.getAllowUrlTokenParam());

    SecurityCaptchaSetting captcha = getOrCreateCaptcha();
    out.setCaptchaEnabled(captcha.getCaptchaEnabled());
    out.setCaptchaType(captcha.getCaptchaType());
    out.setDragCaptchaWidth(captcha.getDragCaptchaWidth());
    out.setDragCaptchaHeight(captcha.getDragCaptchaHeight());
    out.setDragCaptchaThreshold(captcha.getDragCaptchaThreshold());
    out.setImageCaptchaLength(captcha.getImageCaptchaLength());
    out.setImageCaptchaNoiseLines(captcha.getImageCaptchaNoiseLines());

    SecurityPasswordPolicy policy = getOrCreatePasswordPolicy();
    out.setPasswordMinLength(policy.getPasswordMinLength());
    out.setPasswordRequireUppercase(policy.getPasswordRequireUppercase());
    out.setPasswordRequireLowercase(policy.getPasswordRequireLowercase());
    out.setPasswordRequireSpecial(policy.getPasswordRequireSpecial());
    out.setPasswordAllowSequential(policy.getPasswordAllowSequential());
    return out;
  }

  private SecurityTokenSetting getOrCreateToken() {
    SecurityTokenSetting token = tokenMapper.selectTop();
    if (token == null) {
      token = new SecurityTokenSetting();
      tokenMapper.insert(token);
    }
    return token;
  }

  private SecurityCaptchaSetting getOrCreateCaptcha() {
    SecurityCaptchaSetting captcha = captchaMapper.selectTop();
    if (captcha == null) {
      captcha = new SecurityCaptchaSetting();
      captchaMapper.insert(captcha);
    }
    return captcha;
  }

  private SecurityPasswordPolicy getOrCreatePasswordPolicy() {
    SecurityPasswordPolicy policy = passwordMapper.selectTop();
    if (policy == null) {
      policy = new SecurityPasswordPolicy();
      passwordMapper.insert(policy);
    }
    return policy;
  }

  private void upsertToken(SecurityTokenSetting setting) {
    if (setting.getId() == null)
      tokenMapper.insert(setting);
    else
      tokenMapper.update(setting);
  }

  private void upsertCaptcha(SecurityCaptchaSetting setting) {
    if (setting.getId() == null)
      captchaMapper.insert(setting);
    else
      captchaMapper.update(setting);
  }

  private void upsertPasswordPolicy(SecurityPasswordPolicy policy) {
    if (policy.getId() == null)
      passwordMapper.insert(policy);
    else
      passwordMapper.update(policy);
  }
}
