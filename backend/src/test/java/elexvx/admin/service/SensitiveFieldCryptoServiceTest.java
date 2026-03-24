package elexvx.admin.service;

import elexvx.admin.config.properties.ElexvxCoreProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SensitiveFieldCryptoServiceTest {
  @Test
  void shouldFailFastWhenSecretMissing() {
    ElexvxCoreProperties properties = new ElexvxCoreProperties();
    assertThrows(IllegalStateException.class, () -> new SensitiveFieldCryptoService(properties));
  }

  @Test
  void shouldFailFastWhenSecretTooShort() {
    ElexvxCoreProperties properties = new ElexvxCoreProperties();
    properties.getSecurity().setFieldSecret("short-secret");
    assertThrows(IllegalStateException.class, () -> new SensitiveFieldCryptoService(properties));
  }
}
