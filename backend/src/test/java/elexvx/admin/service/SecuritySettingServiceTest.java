package elexvx.admin.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import elexvx.admin.dto.UiSettingRequest;
import elexvx.admin.entity.SecurityCaptchaSetting;
import elexvx.admin.entity.SecurityPasswordPolicy;
import elexvx.admin.entity.SecuritySetting;
import elexvx.admin.entity.SecurityTokenSetting;
import elexvx.admin.mapper.SecurityCaptchaSettingMapper;
import elexvx.admin.mapper.SecurityPasswordPolicyMapper;
import elexvx.admin.mapper.SecurityTokenSettingMapper;
import org.junit.jupiter.api.Test;

class SecuritySettingServiceTest {

  @Test
  void getOrCreateUsesShortLivedCache() {
    SecurityTokenSettingMapper tokenMapper = org.mockito.Mockito.mock(SecurityTokenSettingMapper.class);
    SecurityCaptchaSettingMapper captchaMapper = org.mockito.Mockito.mock(SecurityCaptchaSettingMapper.class);
    SecurityPasswordPolicyMapper passwordMapper = org.mockito.Mockito.mock(SecurityPasswordPolicyMapper.class);

    when(tokenMapper.selectTop()).thenReturn(new SecurityTokenSetting());
    when(captchaMapper.selectTop()).thenReturn(new SecurityCaptchaSetting());
    when(passwordMapper.selectTop()).thenReturn(new SecurityPasswordPolicy());

    SecuritySettingService service = new SecuritySettingService(tokenMapper, captchaMapper, passwordMapper);

    SecuritySetting first = service.getOrCreate();
    SecuritySetting second = service.getOrCreate();

    assertNotNull(first);
    assertNotNull(second);
    verify(tokenMapper, times(1)).selectTop();
    verify(captchaMapper, times(1)).selectTop();
    verify(passwordMapper, times(1)).selectTop();
  }

  @Test
  void applyRequestInvalidatesCache() {
    SecurityTokenSettingMapper tokenMapper = org.mockito.Mockito.mock(SecurityTokenSettingMapper.class);
    SecurityCaptchaSettingMapper captchaMapper = org.mockito.Mockito.mock(SecurityCaptchaSettingMapper.class);
    SecurityPasswordPolicyMapper passwordMapper = org.mockito.Mockito.mock(SecurityPasswordPolicyMapper.class);

    SecurityTokenSetting existingToken = new SecurityTokenSetting();
    existingToken.setId(1L);
    when(tokenMapper.selectTop()).thenReturn(existingToken);
    when(captchaMapper.selectTop()).thenReturn(new SecurityCaptchaSetting());
    when(passwordMapper.selectTop()).thenReturn(new SecurityPasswordPolicy());

    SecuritySettingService service = new SecuritySettingService(tokenMapper, captchaMapper, passwordMapper);
    service.getOrCreate();

    UiSettingRequest req = new UiSettingRequest();
    req.setAllowUrlTokenParam(true);
    service.applyRequest(req);
    service.getOrCreate();

    verify(tokenMapper, times(3)).selectTop();
  }
}
