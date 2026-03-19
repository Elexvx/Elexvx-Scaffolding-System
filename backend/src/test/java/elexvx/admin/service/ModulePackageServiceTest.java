package elexvx.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import elexvx.admin.module.SmsVerificationModuleDefinition;
import java.io.ByteArrayInputStream;
import elexvx.admin.module.ModulePackageManifest;
import elexvx.admin.exception.BusinessException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModulePackageServiceTest {
  private Path tempDir;

  @AfterEach
  void cleanup() throws Exception {
    if (tempDir == null) return;
    if (!Files.exists(tempDir)) return;
    try (var stream = Files.walk(tempDir)) {
      for (Path p : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(p);
      }
    }
  }

  @Test
  void stagePackageRejectsMissingManifest() throws Exception {
    ModulePackageService service = newService();
    byte[] zip = buildZip(zos -> {
      writeEntry(zos, "modules/demo/mysql/install.sql", "select 1;");
    });
    MockMultipartFile file = new MockMultipartFile("file", "module.zip", "application/zip", zip);
    assertThrows(BusinessException.class, () -> service.stagePackage(file));
  }

  @Test
  void stagePackageRejectsMissingFrontendOrBackend() throws Exception {
    ModulePackageService service = newService();
    byte[] zip = buildZip(zos -> {
      ModulePackageManifest manifest = new ModulePackageManifest();
      manifest.setKey("demo");
      manifest.setName("Demo");
      manifest.setVersion("1.0.0");
      writeEntry(zos, "module.json", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(manifest));
      writeEntry(zos, "modules/demo/mysql/install.sql", "select 1;");
    });
    MockMultipartFile file = new MockMultipartFile("file", "module.zip", "application/zip", zip);
    assertThrows(BusinessException.class, () -> service.stagePackage(file));
  }

  @Test
  void stageCommitWritesFrontendAndBackendFiles() throws Exception {
    ModulePackageService service = newService();
    byte[] zip = buildZip(zos -> {
      ModulePackageManifest manifest = new ModulePackageManifest();
      manifest.setKey("demo2");
      manifest.setName("Demo2");
      manifest.setVersion("1.0.0");
      writeEntry(zos, "module.json", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(manifest));
      writeEntry(zos, "modules/demo2/mysql/install.sql", "select 1;");
      writeEntry(zos, "frontend/index.html", "<html>demo</html>");
      writeEntry(zos, "backend/package.json", "{\"name\":\"demo2\",\"version\":\"1.0.0\",\"scripts\":{\"start\":\"node server.js\"}}");
      writeEntry(zos, "backend/server.js", "require('http').createServer((req,res)=>res.end('ok')).listen(process.env.PORT||3000);");
    });
    MockMultipartFile file = new MockMultipartFile("file", "module.zip", "application/zip", zip);
    ModulePackageService.StagedModulePackage staged = service.stagePackage(file);
    ModulePackageService.CommitResult commit = service.commitStagedPackage(staged);

    Path indexHtml = service.getExternalRoot().resolve("frontend").resolve("demo2").resolve("index.html");
    Path pkgJson = service.getExternalRoot().resolve("backend").resolve("demo2").resolve("package.json");
    assertTrue(Files.exists(indexHtml));
    assertTrue(Files.exists(pkgJson));

    assertDoesNotThrow(() -> service.rollbackCommit(commit));
  }

  @Test
  void buildPackageAddsBuiltinFrontendAndBackendWhenMissing() throws Exception {
    ModulePackageService service = newService();
    byte[] zip = service.buildPackage(new SmsVerificationModuleDefinition());
    Set<String> entries = new HashSet<>();
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        entries.add(entry.getName());
      }
    }
    assertTrue(entries.contains("frontend/index.html"));
    assertTrue(entries.contains("backend/package.json"));
    assertTrue(entries.contains("backend/server.js"));
  }

  private ModulePackageService newService() throws Exception {
    tempDir = Files.createTempDirectory("tdesign-module-packages-test");
    MockEnvironment env = new MockEnvironment();
    env.setProperty("tdesign.modules.packageDir", tempDir.toString());
    return new ModulePackageService(new ObjectMapper(), new DefaultResourceLoader(), env);
  }

  private interface ZipWriter {
    void write(ZipOutputStream zos) throws Exception;
  }

  private byte[] buildZip(ZipWriter writer) throws Exception {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
      writer.write(zos);
      zos.finish();
      return bos.toByteArray();
    }
  }

  private static void writeEntry(ZipOutputStream zos, String name, String content) throws Exception {
    ZipEntry entry = new ZipEntry(name);
    zos.putNextEntry(entry);
    zos.write(content.getBytes(StandardCharsets.UTF_8));
    zos.closeEntry();
  }
}
