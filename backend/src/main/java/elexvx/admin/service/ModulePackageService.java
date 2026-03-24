package elexvx.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import elexvx.admin.module.ExternalScriptModuleDefinition;
import elexvx.admin.module.ModuleDefinition;
import elexvx.admin.module.ModulePackageManifest;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ModulePackageService {
  private static final List<String> SUPPORTED_DATABASES = List.of("mysql", "postgresql", "oracle", "sqlserver");
  private static final Logger log = LoggerFactory.getLogger(ModulePackageService.class);
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  private final Path externalRoot;
  private final Path manifestDir;
  private final Path moduleScriptDir;
  private final Path frontendDir;
  private final Path backendDir;
  private final Path stagingDir;
  private final Path rollbackDir;

  public ModulePackageService(ObjectMapper objectMapper, ResourceLoader resourceLoader, Environment environment) {
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
    String dir = environment.getProperty("elexvx.modules.packageDir", "data/module-packages");
    this.externalRoot = Paths.get(dir).toAbsolutePath().normalize();
    this.manifestDir = externalRoot.resolve("manifests");
    this.moduleScriptDir = externalRoot.resolve("modules");
    this.frontendDir = externalRoot.resolve("frontend");
    this.backendDir = externalRoot.resolve("backend");
    this.stagingDir = externalRoot.resolve(".staging");
    this.rollbackDir = externalRoot.resolve(".rollback");
    ensureDirs();
  }

  public Path getExternalRoot() {
    return externalRoot;
  }

  public Path getFrontendDir() {
    return frontendDir;
  }

  public Path getBackendDir() {
    return backendDir;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public ModulePackageManifest readInstalledManifest(String moduleKey) {
    String key = normalizeKey(moduleKey);
    Path file = manifestDir.resolve(key + ".json").toAbsolutePath().normalize();
    if (!file.startsWith(manifestDir) || !Files.exists(file)) return null;
    return readManifest(file);
  }

  public List<ModuleDefinition> loadExternalDefinitions() {
    List<ModuleDefinition> defs = new ArrayList<>();
    if (!Files.isDirectory(manifestDir)) return defs;
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(manifestDir, "*.json")) {
      for (Path file : stream) {
        ModulePackageManifest manifest = readManifest(file);
        if (manifest == null || manifest.getKey() == null || manifest.getKey().isBlank()) continue;
        defs.add(new ExternalScriptModuleDefinition(manifest));
      }
    } catch (IOException listException) {
      log.warn("扫描外部模块清单失败，manifestDir={}", manifestDir, listException);
    }
    return defs;
  }

  public byte[] buildPackage(ModuleDefinition definition) {
    if (definition == null || definition.getKey() == null || definition.getKey().isBlank()) {
      throw badRequest("模块不存在");
    }
    String key = definition.getKey().trim().toLowerCase(Locale.ROOT);
    ModulePackageManifest installedManifest = readInstalledManifest(key);
    boolean builtin = installedManifest == null;
    ModulePackageManifest manifest = applyDefaults(installedManifest, definition, key);

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
      byte[] manifestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
      writeZipEntry(zos, "module.json", manifestBytes);

      int sqlCount = 0;
      int installCount = 0;
      for (String db : SUPPORTED_DATABASES) {
        if (writeSqlIfExists(zos, key, db, "install")) {
          sqlCount++;
          installCount++;
        }
        if (writeSqlIfExists(zos, key, db, "uninstall")) {
          sqlCount++;
        }
      }
      if (installCount == 0) {
        throw badRequest("模块包缺少 install.sql");
      }
      if (sqlCount == 0) {
        throw badRequest("模块包缺少 SQL 脚本");
      }
      String index = manifest.getFrontend() == null
        ? "index.html"
        : String.valueOf(manifest.getFrontend().getIndex() == null ? "" : manifest.getFrontend().getIndex()).trim();
      if (index.isEmpty()) index = "index.html";
      Path frontendRoot = frontendDir.resolve(key).toAbsolutePath().normalize();
      Path frontendIndex = frontendRoot.resolve(index).toAbsolutePath().normalize();
      boolean hasFrontend = Files.isDirectory(frontendRoot)
        && frontendIndex.startsWith(frontendRoot)
        && Files.exists(frontendIndex);

      Path backendRoot = backendDir.resolve(key).toAbsolutePath().normalize();
      boolean hasBackend = Files.isDirectory(backendRoot) && Files.exists(backendRoot.resolve("package.json"));

      if (hasFrontend) {
        writeDirectoryRequired(zos, frontendDir.resolve(key), "frontend/", "模块包缺少 frontend");
      } else if (builtin) {
        writeBuiltinFrontend(zos, manifest, definition);
      } else {
        throw badRequest("模块包缺少前端入口文件: " + index);
      }

      if (hasBackend) {
        writeDirectoryRequired(zos, backendDir.resolve(key), "backend/", "模块包缺少 backend");
      } else if (builtin) {
        writeBuiltinBackend(zos, manifest, definition);
      } else {
        throw badRequest("模块包缺少后端 package.json");
      }
      zos.finish();
      return bos.toByteArray();
    } catch (IOException e) {
      throw badRequest("生成模块包失败: " + e.getMessage());
    }
  }

  public StagedModulePackage stagePackage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw badRequest("模块包不能为空");
    }
    Path stageRoot = stagingDir.resolve(UUID.randomUUID().toString()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(stageRoot);
    } catch (IOException e) {
      throw badRequest("创建临时目录失败: " + e.getMessage());
    }

    ModulePackageManifest manifest = null;
    int sqlCount = 0;
    int installSqlCount = 0;
    int frontendCount = 0;
    int backendCount = 0;

    try (ZipInputStream zis = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory()) continue;
        String name = normalizeZipEntryName(entry.getName());
        if (name == null) continue;
        String lower = name.toLowerCase(Locale.ROOT);

        if ("module.json".equalsIgnoreCase(name)) {
          byte[] bytes = zis.readAllBytes();
          manifest = objectMapper.readValue(bytes, ModulePackageManifest.class);
          continue;
        }

        if (!(lower.startsWith("modules/") || lower.startsWith("frontend/") || lower.startsWith("backend/"))) {
          continue;
        }

        Path target = stageRoot.resolve(name).toAbsolutePath().normalize();
        if (!target.startsWith(stageRoot)) {
          throw badRequest("模块包文件路径非法: " + name);
        }
        Files.createDirectories(target.getParent());
        Files.write(target, zis.readAllBytes());
        if (lower.startsWith("modules/") && lower.endsWith(".sql")) {
          sqlCount++;
          if (lower.endsWith("/install.sql")) installSqlCount++;
        }
        if (lower.startsWith("frontend/")) frontendCount++;
        if (lower.startsWith("backend/")) backendCount++;
      }
    } catch (Exception e) {
      safeDeleteDir(stageRoot);
      throw badRequest("解析模块包失败: " + e.getMessage());
    }

    if (manifest == null || manifest.getKey() == null || manifest.getKey().isBlank()) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块包缺少 module.json 或 key");
    }

    String key = normalizeKey(manifest.getKey());
    manifest.setKey(key);
    if (installSqlCount == 0) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块包缺少 install.sql");
    }
    if (sqlCount == 0) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块包必须包含 SQL 脚本");
    }
    if (frontendCount == 0) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块包必须包含 frontend");
    }
    if (backendCount == 0) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块包必须包含 backend");
    }
    manifest = applyDefaults(manifest, null, key);
    validateStagedPackage(stageRoot, manifest);
    return new StagedModulePackage(manifest, stageRoot);
  }

  private boolean writeSqlIfExists(ZipOutputStream zos, String key, String db, String action) throws IOException {
    String zipPath = String.format("modules/%s/%s/%s.sql", key, db, action);
    byte[] bytes = readSqlBytes(key, db, action);
    if (bytes == null || bytes.length == 0) return false;
    writeZipEntry(zos, zipPath, bytes);
    return true;
  }

  private byte[] readSqlBytes(String key, String db, String action) throws IOException {
    Path file = moduleScriptDir.resolve(key).resolve(db).resolve(action + ".sql");
    if (Files.exists(file)) {
      return Files.readAllBytes(file);
    }
    Resource resource = resourceLoader.getResource(String.format("classpath:modules/%s/%s/%s.sql", key, db, action));
    if (!resource.exists()) return null;
    try (InputStream in = resource.getInputStream()) {
      return in.readAllBytes();
    }
  }

  private void writeZipEntry(ZipOutputStream zos, String name, byte[] bytes) throws IOException {
    ZipEntry entry = new ZipEntry(name);
    zos.putNextEntry(entry);
    zos.write(bytes);
    zos.closeEntry();
  }

  private String normalizeZipEntryName(String name) {
    if (name == null) return null;
    String n = name.replace('\\', '/').trim();
    while (n.startsWith("/")) n = n.substring(1);
    if (n.contains("..")) return null;
    return n;
  }

  public CommitResult commitStagedPackage(StagedModulePackage staged) {
    if (staged == null || staged.manifest == null || staged.stagingRoot == null) {
      throw badRequest("模块包状态非法");
    }
    String key = normalizeKey(staged.manifest.getKey());
    Path stageRoot = staged.stagingRoot.toAbsolutePath().normalize();
    if (!stageRoot.startsWith(stagingDir) || !Files.isDirectory(stageRoot)) {
      throw badRequest("模块包临时目录不存在");
    }

    Path backupRoot = rollbackDir.resolve(UUID.randomUUID().toString()).resolve(key).toAbsolutePath().normalize();
    try {
      Files.createDirectories(backupRoot);
    } catch (IOException e) {
      safeDeleteDir(stageRoot);
      throw badRequest("创建回滚目录失败: " + e.getMessage());
    }

    Path targetManifestFile = manifestDir.resolve(key + ".json").toAbsolutePath().normalize();
    if (!targetManifestFile.startsWith(manifestDir)) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块 key 非法");
    }

    Path stageModulesDir = stageRoot.resolve("modules").resolve(key).toAbsolutePath().normalize();
    Path stageFrontendDir = stageRoot.resolve("frontend").toAbsolutePath().normalize();
    Path stageBackendDir = stageRoot.resolve("backend").toAbsolutePath().normalize();

    Path targetModulesDir = moduleScriptDir.resolve(key).toAbsolutePath().normalize();
    Path targetFrontendDir = frontendDir.resolve(key).toAbsolutePath().normalize();
    Path targetBackendDir = backendDir.resolve(key).toAbsolutePath().normalize();

    if (!targetModulesDir.startsWith(moduleScriptDir) || !targetFrontendDir.startsWith(frontendDir) || !targetBackendDir.startsWith(backendDir)) {
      safeDeleteDir(stageRoot);
      throw badRequest("模块 key 非法");
    }

    Map<Path, Path> moved = new java.util.LinkedHashMap<>();
    List<Path> created = new ArrayList<>();
    try {
      if (Files.exists(targetManifestFile)) {
        Path backup = backupRoot.resolve("manifest.json");
        Files.createDirectories(backup.getParent());
        Files.move(targetManifestFile, backup);
        moved.put(targetManifestFile, backup);
      }

      moveDirIfExists(targetModulesDir, backupRoot.resolve("modules"));
      if (Files.exists(backupRoot.resolve("modules"))) moved.put(targetModulesDir, backupRoot.resolve("modules"));

      moveDirIfExists(targetFrontendDir, backupRoot.resolve("frontend"));
      if (Files.exists(backupRoot.resolve("frontend"))) moved.put(targetFrontendDir, backupRoot.resolve("frontend"));

      moveDirIfExists(targetBackendDir, backupRoot.resolve("backend"));
      if (Files.exists(backupRoot.resolve("backend"))) moved.put(targetBackendDir, backupRoot.resolve("backend"));

      Files.createDirectories(manifestDir);
      Files.writeString(
        targetManifestFile,
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(staged.manifest),
        StandardCharsets.UTF_8
      );
      created.add(targetManifestFile);

      if (Files.isDirectory(stageModulesDir)) {
        Files.createDirectories(targetModulesDir.getParent());
        Files.move(stageModulesDir, targetModulesDir);
        created.add(targetModulesDir);
      }
      if (Files.isDirectory(stageFrontendDir)) {
        Files.createDirectories(targetFrontendDir.getParent());
        Files.move(stageFrontendDir, targetFrontendDir);
        created.add(targetFrontendDir);
      }
      if (Files.isDirectory(stageBackendDir)) {
        Files.createDirectories(targetBackendDir.getParent());
        Files.move(stageBackendDir, targetBackendDir);
        created.add(targetBackendDir);
      }

      safeDeleteDir(stageRoot);
      return new CommitResult(key, backupRoot, moved, created);
    } catch (Exception e) {
      rollbackCommit(new CommitResult(key, backupRoot, moved, created));
      safeDeleteDir(stageRoot);
      throw badRequest("安装模块包失败: " + e.getMessage());
    }
  }

  public void rollbackCommit(CommitResult commit) {
    if (commit == null) return;
    for (Path created : commit.createdTargets) {
      if (created == null) continue;
      if (!created.toAbsolutePath().normalize().startsWith(externalRoot)) continue;
      if (Files.isRegularFile(created)) {
        try {
          Files.deleteIfExists(created);
        } catch (Exception deleteException) {
          log.warn("删除回滚创建文件失败，path={}", created, deleteException);
        }
        continue;
      }
      if (Files.isDirectory(created)) {
        safeDeleteDir(created);
      }
    }

    List<Map.Entry<Path, Path>> entries = new ArrayList<>(commit.movedBackups.entrySet());
    entries.sort(Comparator.comparing(e -> e.getKey().toString(), Comparator.reverseOrder()));
    for (Map.Entry<Path, Path> moved : entries) {
      Path target = moved.getKey();
      Path backup = moved.getValue();
      try {
        if (Files.exists(backup)) {
          Files.createDirectories(target.getParent());
          Files.move(backup, target);
        }
      } catch (Exception restoreException) {
        log.warn("恢复回滚备份失败，target={}, backup={}", target, backup, restoreException);
      }
    }
  }

  private ModulePackageManifest readManifest(Path file) {
    try {
      byte[] bytes = Files.readAllBytes(file);
      ModulePackageManifest manifest = objectMapper.readValue(bytes, ModulePackageManifest.class);
      if (manifest == null) return null;
      if (manifest.getKey() != null) manifest.setKey(manifest.getKey().trim().toLowerCase(Locale.ROOT));
      return manifest;
    } catch (Exception parseException) {
      log.warn("解析模块清单失败，file={}", file, parseException);
      return null;
    }
  }

  private void ensureDirs() {
    try {
      Files.createDirectories(moduleScriptDir);
      Files.createDirectories(frontendDir);
      Files.createDirectories(backendDir);
      Files.createDirectories(manifestDir);
      Files.createDirectories(stagingDir);
      Files.createDirectories(rollbackDir);
    } catch (IOException createDirException) {
      log.warn("初始化模块目录失败，externalRoot={}", externalRoot, createDirException);
    }
  }

  private void validateStagedPackage(Path stageRoot, ModulePackageManifest manifest) {
    String key = normalizeKey(manifest == null ? "" : manifest.getKey());
    Path modules = stageRoot.resolve("modules").toAbsolutePath().normalize();
    if (Files.isDirectory(modules)) {
      try (var stream = Files.walk(modules)) {
        for (Path file : stream.filter(Files::isRegularFile).toList()) {
          Path rel = modules.relativize(file.toAbsolutePath().normalize());
          String normalized = rel.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
          if (!normalized.startsWith(key + "/")) {
            throw badRequest("模块脚本路径非法: " + normalized);
          }
          String[] parts = normalized.split("/");
          if (parts.length != 3) {
            throw badRequest("模块脚本路径非法: " + normalized);
          }
          String db = parts[1];
          if (!SUPPORTED_DATABASES.contains(db)) {
            throw badRequest("不支持的数据库脚本目录: " + db);
          }
          String filename = parts[2];
          if (!"install.sql".equals(filename) && !"uninstall.sql".equals(filename)) {
            throw badRequest("不支持的脚本文件: " + filename);
          }
        }
      } catch (IOException e) {
        throw badRequest("校验模块脚本失败: " + e.getMessage());
      }
    }

    ModulePackageManifest.FrontendSpec frontend = manifest == null ? null : manifest.getFrontend();
    String frontendType = String.valueOf(frontend == null || frontend.getType() == null ? "" : frontend.getType())
      .trim()
      .toLowerCase(Locale.ROOT);
    if (!frontendType.isEmpty() && !"static".equals(frontendType)) {
      throw badRequest("不支持的前端类型: " + frontendType);
    }
    String index = String.valueOf(frontend == null || frontend.getIndex() == null ? "" : frontend.getIndex()).trim();
    if (index.isEmpty()) index = "index.html";
    Path frontendRoot = stageRoot.resolve("frontend").toAbsolutePath().normalize();
    if (!Files.isDirectory(frontendRoot)) {
      throw badRequest("模块包缺少 frontend");
    }
    Path indexFile = frontendRoot.resolve(index).toAbsolutePath().normalize();
    if (!indexFile.startsWith(frontendRoot)) {
      throw badRequest("模块前端入口文件路径非法");
    }
    if (!Files.exists(indexFile)) {
      throw badRequest("模块前端缺少入口文件: " + index);
    }

    ModulePackageManifest.BackendSpec backend = manifest == null ? null : manifest.getBackend();
    String backendType = String.valueOf(backend == null || backend.getType() == null ? "" : backend.getType())
      .trim()
      .toLowerCase(Locale.ROOT);
    if (!backendType.isEmpty() && !"node".equals(backendType)) {
      throw badRequest("不支持的后端类型: " + backendType);
    }
    Path backendRoot = stageRoot.resolve("backend").toAbsolutePath().normalize();
    if (!Files.isDirectory(backendRoot)) {
      throw badRequest("模块包缺少 backend");
    }
    Path pkg = backendRoot.resolve("package.json").toAbsolutePath().normalize();
    if (!pkg.startsWith(backendRoot)) {
      throw badRequest("模块后端路径非法");
    }
    if (!Files.exists(pkg)) {
      throw badRequest("模块后端缺少 package.json");
    }
  }

  private void moveDirIfExists(Path targetDir, Path backupDir) throws IOException {
    if (targetDir == null || backupDir == null) return;
    if (!Files.exists(targetDir)) return;
    Files.createDirectories(backupDir.getParent());
    Files.move(targetDir, backupDir);
  }

  private void writeDirectoryIfExists(ZipOutputStream zos, Path dir, String zipPrefix) throws IOException {
    if (!Files.isDirectory(dir)) return;
    try (var stream = Files.walk(dir)) {
      for (Path file : stream.filter(Files::isRegularFile).toList()) {
        Path rel = dir.relativize(file.toAbsolutePath().normalize());
        String entryName = zipPrefix + rel.toString().replace('\\', '/');
        writeZipEntry(zos, entryName, Files.readAllBytes(file));
      }
    }
  }

  private void writeDirectoryRequired(ZipOutputStream zos, Path dir, String zipPrefix, String errorMessage) throws IOException {
    if (!Files.isDirectory(dir)) {
      throw badRequest(errorMessage);
    }
    boolean wrote = false;
    try (var stream = Files.walk(dir)) {
      for (Path file : stream.filter(Files::isRegularFile).toList()) {
        Path rel = dir.relativize(file.toAbsolutePath().normalize());
        String entryName = zipPrefix + rel.toString().replace('\\', '/');
        writeZipEntry(zos, entryName, Files.readAllBytes(file));
        wrote = true;
      }
    }
    if (!wrote) {
      throw badRequest(errorMessage);
    }
  }

  private void safeDeleteDir(Path dir) {
    if (dir == null) return;
    Path normalized = dir.toAbsolutePath().normalize();
    if (!normalized.startsWith(externalRoot)) return;
    if (!Files.exists(normalized)) return;
    try (var stream = Files.walk(normalized)) {
      for (Path file : stream.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(file);
      }
    } catch (Exception deleteException) {
      log.warn("删除目录失败，path={}", normalized, deleteException);
    }
  }

  private String normalizeKey(String key) {
    return String.valueOf(key == null ? "" : key).trim().toLowerCase(Locale.ROOT);
  }

  private ModulePackageManifest applyDefaults(ModulePackageManifest manifest, ModuleDefinition definition, String key) {
    ModulePackageManifest next = manifest == null ? new ModulePackageManifest() : manifest;
    if (key != null && !key.isBlank()) {
      next.setKey(key);
    } else if (next.getKey() != null) {
      next.setKey(normalizeKey(next.getKey()));
    }
    if (definition != null) {
      if (next.getName() == null || next.getName().isBlank()) next.setName(definition.getName());
      if (next.getVersion() == null || next.getVersion().isBlank()) next.setVersion(definition.getVersion());
      if (next.getEnabledByDefault() == null) next.setEnabledByDefault(definition.isEnabledByDefault());
      if (next.getRequiredTables() == null || next.getRequiredTables().isEmpty()) {
        next.setRequiredTables(definition.getRequiredTables());
      }
    }
    if (next.getFrontend() == null) {
      ModulePackageManifest.FrontendSpec frontendSpec = new ModulePackageManifest.FrontendSpec();
      frontendSpec.setType("static");
      frontendSpec.setBasePath("/modules/" + (key == null ? "" : key) + "/");
      frontendSpec.setIndex("index.html");
      next.setFrontend(frontendSpec);
    }
    if (next.getBackend() == null) {
      ModulePackageManifest.BackendSpec backendSpec = new ModulePackageManifest.BackendSpec();
      backendSpec.setType("node");
      backendSpec.setStartScript("start");
      backendSpec.setBasePath("/module-api/" + (key == null ? "" : key) + "/");
      backendSpec.setAutoInstallDependencies(true);
      next.setBackend(backendSpec);
    }
    return next;
  }

  private void writeBuiltinFrontend(ZipOutputStream zos, ModulePackageManifest manifest, ModuleDefinition definition) throws IOException {
    String key = normalizeKey(manifest == null ? "" : manifest.getKey());
    String name = manifest == null ? null : manifest.getName();
    if (name == null || name.isBlank()) {
      name = definition == null ? null : definition.getName();
    }
    if (name == null || name.isBlank()) name = key;
    String index = manifest == null || manifest.getFrontend() == null ? "" : String.valueOf(manifest.getFrontend().getIndex() == null ? "" : manifest.getFrontend().getIndex()).trim();
    if (index.isEmpty()) index = "index.html";
    String normalizedIndex = normalizeZipEntryName(index);
    if (normalizedIndex == null || normalizedIndex.isBlank()) normalizedIndex = "index.html";

    String title = name.replace("<", "&lt;").replace(">", "&gt;");
    String html =
      "<!doctype html>\n" +
      "<html lang=\"en\">\n" +
      "<head>\n" +
      "  <meta charset=\"utf-8\" />\n" +
      "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" />\n" +
      "  <title>" + title + "</title>\n" +
      "  <style>body{font-family:Arial,sans-serif;margin:32px;color:#333}h1{font-size:20px}p{color:#666}</style>\n" +
      "</head>\n" +
      "<body>\n" +
      "  <h1>Built-in module: " + title + "</h1>\n" +
      "  <p>This is a placeholder frontend packaged for a built-in module.</p>\n" +
      "</body>\n" +
      "</html>\n";
    writeZipEntry(zos, "frontend/" + normalizedIndex, html.getBytes(StandardCharsets.UTF_8));
  }

  private void writeBuiltinBackend(ZipOutputStream zos, ModulePackageManifest manifest, ModuleDefinition definition) throws IOException {
    String key = normalizeKey(manifest == null ? "" : manifest.getKey());
    String name = manifest == null ? null : manifest.getName();
    if (name == null || name.isBlank()) {
      name = definition == null ? null : definition.getName();
    }
    if (name == null || name.isBlank()) name = key;
    String version = manifest == null ? null : manifest.getVersion();
    if (version == null || version.isBlank()) version = "1.0.0";
    String startScript = manifest == null || manifest.getBackend() == null
      ? "start"
      : String.valueOf(manifest.getBackend().getStartScript() == null ? "" : manifest.getBackend().getStartScript()).trim();
    if (startScript.isEmpty()) startScript = "start";

    String packageJson =
      "{\n" +
      "  \"name\": \"" + key + "-module-backend\",\n" +
      "  \"version\": \"" + version + "\",\n" +
      "  \"private\": true,\n" +
      "  \"scripts\": {\n" +
      "    \"" + startScript + "\": \"node server.js\"\n" +
      "  }\n" +
      "}\n";
    String serverJs =
      "const http = require('http');\n" +
      "const port = Number(process.env.PORT || 0);\n" +
      "const moduleKey = process.env.MODULE_KEY || '" + key + "';\n" +
      "const moduleName = '" + name.replace("'", "\\'") + "';\n" +
      "const payload = JSON.stringify({ moduleKey, moduleName, status: 'ok' });\n" +
      "http.createServer((req, res) => {\n" +
      "  res.statusCode = 200;\n" +
      "  res.setHeader('Content-Type', 'application/json; charset=utf-8');\n" +
      "  res.end(payload);\n" +
      "}).listen(port, '127.0.0.1');\n";
    writeZipEntry(zos, "backend/package.json", packageJson.getBytes(StandardCharsets.UTF_8));
    writeZipEntry(zos, "backend/server.js", serverJs.getBytes(StandardCharsets.UTF_8));
  }

  public record StagedModulePackage(ModulePackageManifest manifest, Path stagingRoot) {}

  public record CommitResult(String key, Path backupRoot, Map<Path, Path> movedBackups, List<Path> createdTargets) {}
}
