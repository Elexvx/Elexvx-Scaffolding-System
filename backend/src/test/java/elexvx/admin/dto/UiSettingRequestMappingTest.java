package elexvx.admin.dto;

import static org.assertj.core.api.Assertions.assertThat;

import elexvx.admin.model.req.setting.SecurityPolicySettingRequest;
import elexvx.admin.model.req.setting.SessionPolicySettingRequest;
import elexvx.admin.model.req.setting.UiBrandSettingRequest;
import elexvx.admin.model.req.setting.VerificationProviderSettingRequest;
import org.junit.jupiter.api.Test;

class UiSettingRequestMappingTest {
  @Test
  void shouldKeepFlatPayloadCompatibility() {
    UiSettingRequest req = new UiSettingRequest();
    req.setWebsiteName("站点A");
    req.setSmsEnabled(true);
    req.setSessionTimeoutMinutes(30);
    req.setCaptchaType("drag");

    UiBrandSettingRequest brand = req.toBrandSettingRequest();
    VerificationProviderSettingRequest verification = req.toVerificationProviderSettingRequest();
    SessionPolicySettingRequest sessionPolicy = req.toSessionPolicySettingRequest();
    SecurityPolicySettingRequest securityPolicy = req.toSecurityPolicySettingRequest();

    assertThat(brand.websiteName()).isEqualTo("站点A");
    assertThat(verification.smsEnabled()).isTrue();
    assertThat(sessionPolicy.sessionTimeoutMinutes()).isEqualTo(30);
    assertThat(securityPolicy.captchaType()).isEqualTo("drag");
  }

  @Test
  void shouldPreferNestedRequestFields() {
    UiSettingRequest req = new UiSettingRequest();
    req.setWebsiteName("旧值");
    req.setBrandSetting(new UiBrandSettingRequest("新值", null, null, null, null, null));

    UiBrandSettingRequest brand = req.toBrandSettingRequest();
    assertThat(brand.websiteName()).isEqualTo("新值");
  }
}
