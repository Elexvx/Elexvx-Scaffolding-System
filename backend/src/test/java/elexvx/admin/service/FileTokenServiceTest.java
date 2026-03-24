package elexvx.admin.service;

import elexvx.admin.config.properties.ElexvxCoreProperties;
import elexvx.admin.entity.StorageSetting;
import elexvx.admin.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FileTokenServiceTest {
  @Test
  void shouldRejectExpiredToken() throws InterruptedException {
    ElexvxCoreProperties properties = new ElexvxCoreProperties();
    properties.getFile().setTokenSecret("super-secure-secret-32-bytes-minimum!!");
    properties.getFile().setTokenTtlSeconds(1);

    FileTokenService service = new FileTokenService(properties, "/api");
    String url = service.buildAccessUrl(StorageSetting.Provider.LOCAL, "business/2026/a.pdf");
    String token = service.extractToken(url);
    Thread.sleep(1100);
    assertThrows(BusinessException.class, () -> service.decrypt(token));
  }
}
