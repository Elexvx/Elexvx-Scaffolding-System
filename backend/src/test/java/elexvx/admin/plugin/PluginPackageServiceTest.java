package elexvx.admin.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PluginPackageServiceTest {
  @Test
  void shouldComputeSha256() throws Exception {
    Path file = Files.createTempFile("plugin-hash", ".txt");
    Files.writeString(file, "elexvx-plugin");
    String hash = PluginPackageService.sha256(file);
    assertEquals(64, hash.length());
    assertTrue(hash.matches("[0-9a-f]+"));
  }
}
