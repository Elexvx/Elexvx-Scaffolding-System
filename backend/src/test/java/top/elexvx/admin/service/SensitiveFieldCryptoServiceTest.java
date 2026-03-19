package top.elexvx.admin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SensitiveFieldCryptoServiceTest {
  @Test
  void shouldFailFastWhenSecretMissing() {
    assertThrows(IllegalStateException.class, () -> new SensitiveFieldCryptoService(""));
  }

  @Test
  void shouldFailFastWhenSecretTooShort() {
    assertThrows(IllegalStateException.class, () -> new SensitiveFieldCryptoService("short-secret"));
  }
}
