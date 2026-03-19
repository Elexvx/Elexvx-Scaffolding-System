package elexvx.admin.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import elexvx.admin.dto.UiSettingRequest;
import elexvx.admin.entity.SecurityCaptchaSetting;
import elexvx.admin.entity.SecurityPasswordPolicy;
import elexvx.admin.entity.SecuritySetting;
import elexvx.admin.entity.SecurityTokenSetting;
import elexvx.admin.mapper.SecurityCaptchaSettingMapper;
import elexvx.admin.mapper.SecurityPasswordPolicyMapper;
import elexvx.admin.mapper.SecurityTokenSettingMapper;
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

  public boolean applyRequest(UiSettingRequest req) {
    boolean changed = false;

    SecurityTokenSetting token = tokenMapper.selectTop();
    if (token == null)
      token = new SecurityTokenSetting();
    boolean tokenChanged = false;
    if (req.getSessionTimeoutMinutes() != null) {
      token.setSessionTimeoutMinutes(req.getSessionTimeoutMinutes());
      tokenChanged = true;
    }
    if (req.getTokenTimeoutMinutes() != null) {
      token.setTokenTimeoutMinutes(req.getTokenTimeoutMinutes());
      tokenChanged = true;
    }
    if (req.getTokenRefreshGraceMinutes() != null) {
      token.setTokenRefreshGraceMinutes(req.getTokenRefreshGraceMinutes());
      tokenChanged = true;
    }
    if (req.getAllowUrlTokenParam() != null) {
      token.setAllowUrlTokenParam(req.getAllowUrlTokenParam());
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
    if (req.getCaptchaEnabled() != null) {
      captcha.setCaptchaEnabled(req.getCaptchaEnabled());
      captchaChanged = true;
    }
    if (req.getCaptchaType() != null) {
      captcha.setCaptchaType(req.getCaptchaType());
      captchaChanged = true;
    }
    if (req.getDragCaptchaWidth() != null) {
      captcha.setDragCaptchaWidth(req.getDragCaptchaWidth());
      captchaChanged = true;
    }
    if (req.getDragCaptchaHeight() != null) {
      captcha.setDragCaptchaHeight(req.getDragCaptchaHeight());
      captchaChanged = true;
    }
    if (req.getDragCaptchaThreshold() != null) {
      captcha.setDragCaptchaThreshold(req.getDragCaptchaThreshold());
      captchaChanged = true;
    }
    if (req.getImageCaptchaLength() != null) {
      captcha.setImageCaptchaLength(req.getImageCaptchaLength());
      captchaChanged = true;
    }
    if (req.getImageCaptchaNoiseLines() != null) {
      captcha.setImageCaptchaNoiseLines(req.getImageCaptchaNoiseLines());
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
    if (req.getPasswordMinLength() != null) {
      policy.setPasswordMinLength(req.getPasswordMinLength());
      policyChanged = true;
    }
    if (req.getPasswordRequireUppercase() != null) {
      policy.setPasswordRequireUppercase(req.getPasswordRequireUppercase());
      policyChanged = true;
    }
    if (req.getPasswordRequireLowercase() != null) {
      policy.setPasswordRequireLowercase(req.getPasswordRequireLowercase());
      policyChanged = true;
    }
    if (req.getPasswordRequireSpecial() != null) {
      policy.setPasswordRequireSpecial(req.getPasswordRequireSpecial());
      policyChanged = true;
    }
    if (req.getPasswordAllowSequential() != null) {
      policy.setPasswordAllowSequential(req.getPasswordAllowSequential());
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
