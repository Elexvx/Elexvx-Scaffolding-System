package top.elexvx.admin.service;

import top.elexvx.admin.exception.BusinessException;
import top.elexvx.admin.exception.ErrorCodes;
import top.elexvx.admin.module.ModulePackageManifest;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class ModuleBackendProcessManager {
  private static final DateTimeFormatter LOG_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final Logger log = LoggerFactory.getLogger(ModuleBackendProcessManager.class);
  private final ModulePackageService modulePackageService;
  private final Environment environment;
  private final Map<String, RunningProcess> running = new ConcurrentHashMap<>();

  public ModuleBackendProcessManager(ModulePackageService modulePackageService, Environment environment) {
    this.modulePackageService = modulePackageService;
    this.environment = environment;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public boolean isRunning(String moduleKey) {
    String key = normalizeKey(moduleKey);
    RunningProcess proc = running.get(key);
    if (proc == null) return false;
    return proc.process.isAlive();
  }

  public int getPort(String moduleKey) {
    String key = normalizeKey(moduleKey);
    RunningProcess proc = running.get(key);
    if (proc == null) return -1;
    if (!proc.process.isAlive()) return -1;
    return proc.port;
  }

  public void ensureRunning(String moduleKey) {
    String key = normalizeKey(moduleKey);
    ModulePackageManifest manifest = modulePackageService.readInstalledManifest(key);
    if (manifest == null) return;
    ModulePackageManifest.BackendSpec backend = manifest.getBackend();
    if (backend == null) return;
    String type = String.valueOf(backend.getType() == null ? "" : backend.getType()).trim().toLowerCase(Locale.ROOT);
    if (!"node".equals(type)) return;

    RunningProcess existing = running.get(key);
    if (existing != null && existing.process.isAlive()) return;
    startNodeBackend(key, backend);
  }

  public void ensureDependenciesInstalled(String moduleKey) {
    String key = normalizeKey(moduleKey);
    ModulePackageManifest manifest = modulePackageService.readInstalledManifest(key);
    if (manifest == null) return;
    ModulePackageManifest.BackendSpec backend = manifest.getBackend();
    if (backend == null) return;
    String type = String.valueOf(backend.getType() == null ? "" : backend.getType()).trim().toLowerCase(Locale.ROOT);
    if (!"node".equals(type)) return;

    Path dir = modulePackageService.getBackendDir().resolve(key).toAbsolutePath().normalize();
    if (!Files.isDirectory(dir)) {
      throw badRequest("模块后端目录不存在: " + key);
    }
    if (!Files.exists(dir.resolve("package.json"))) {
      throw badRequest("模块后端缺少 package.json: " + key);
    }
    ensureNodeDependencies(dir, backend);
  }

  public void stop(String moduleKey) {
    String key = normalizeKey(moduleKey);
    RunningProcess proc = running.remove(key);
    if (proc == null) return;
    try {
      proc.process.destroy();
      proc.process.waitFor();
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      log.warn("停止模块后端进程被中断，moduleKey={}", key, interruptedException);
      try {
        proc.process.destroyForcibly();
      } catch (Exception forceKillException) {
        log.warn("强制停止模块后端进程失败，moduleKey={}", key, forceKillException);
      }
    } catch (Exception destroyException) {
      log.warn("优雅停止模块后端进程失败，尝试强制停止，moduleKey={}", key, destroyException);
      try {
        proc.process.destroyForcibly();
      } catch (Exception forceKillException) {
        log.warn("强制停止模块后端进程失败，moduleKey={}", key, forceKillException);
      }
    }
  }

  private void startNodeBackend(String key, ModulePackageManifest.BackendSpec backend) {
    Path dir = modulePackageService.getBackendDir().resolve(key).toAbsolutePath().normalize();
    if (!Files.isDirectory(dir)) {
      throw badRequest("模块后端目录不存在: " + key);
    }
    if (!Files.exists(dir.resolve("package.json"))) {
      throw badRequest("模块后端缺少 package.json: " + key);
    }

    ensureNodeDependencies(dir, backend);

    String startScript = String.valueOf(backend.getStartScript() == null ? "" : backend.getStartScript()).trim();
    if (startScript.isEmpty()) startScript = "start";

    int port = PortAllocator.allocatePort();
    Path logDir = dir.resolve("logs").toAbsolutePath().normalize();
    try {
      Files.createDirectories(logDir);
    } catch (IOException createLogDirException) {
      log.warn("创建模块日志目录失败，moduleKey={}, path={}", key, logDir, createLogDirException);
    }
    Path logFile = logDir.resolve("run-" + LOG_TS.format(LocalDateTime.now()) + ".log");

    ProcessBuilder pb = new ProcessBuilder("npm", "run", startScript);
    pb.directory(dir.toFile());
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
    Map<String, String> env = pb.environment();
    env.put("PORT", String.valueOf(port));
    env.put("MODULE_KEY", key);
    boolean exposeDbCredentials = Boolean.parseBoolean(getEnv("elexvx.modules.backend.expose-db-credentials", "false"));
    if (exposeDbCredentials) {
      env.put("ELEXVX_DB_URL", getEnv("spring.datasource.url", ""));
      env.put("ELEXVX_DB_USER", getEnv("spring.datasource.username", ""));
      env.put("ELEXVX_DB_PASSWORD", getEnv("spring.datasource.password", ""));
      env.put("ELEXVX_DB_DRIVER", getEnv("spring.datasource.driver-class-name", ""));
      env.put("ELEXVX_DB_TYPE", getEnv("elexvx.db.type", "mysql"));
    }
    String contextPath = getEnv("server.servlet.context-path", "/api");
    if (!contextPath.startsWith("/")) contextPath = "/" + contextPath;
    int serverPort = parsePort(getEnv("server.port", "9999"), 9999);
    env.put("ELEXVX_CORE_API_BASE", "http://127.0.0.1:" + serverPort + contextPath);

    try {
      Process process = pb.start();
      waitPortReady(port, 15_000);
      running.put(key, new RunningProcess(process, port));
    } catch (Exception e) {
      log.error("启动模块后端失败，moduleKey={}, port={}, logFile={}", key, port, logFile, e);
      throw badRequest("启动模块后端失败，请检查模块日志");
    }
  }

  private void installNodeDependencies(Path dir) {
    boolean useCi = Files.exists(dir.resolve("package-lock.json"));
    boolean allowNpmScripts = Boolean.parseBoolean(getEnv("elexvx.modules.backend.npm-allow-scripts", "false"));
    List<String> command = new ArrayList<>();
    command.add("npm");
    command.add(useCi ? "ci" : "install");
    command.add("--no-audit");
    command.add("--no-fund");
    if (!allowNpmScripts) {
      command.add("--ignore-scripts");
    }
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(dir.toFile());
    pb.redirectErrorStream(true);
    File log = dir.resolve("npm-install.log").toFile();
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
    try {
      Process proc = pb.start();
      int code = proc.waitFor();
      if (code != 0) {
        throw badRequest("模块依赖安装失败，请查看: " + log.getAbsolutePath());
      }
    } catch (IOException e) {
      throw badRequest("执行 npm 失败，请确认服务器已安装 Node.js/npm");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw badRequest("模块依赖安装被中断");
    }
  }

  private void ensureNodeDependencies(Path dir, ModulePackageManifest.BackendSpec backend) {
    boolean autoInstall = backend.getAutoInstallDependencies() == null || Boolean.TRUE.equals(backend.getAutoInstallDependencies());
    if (!autoInstall) return;
    if (Files.isDirectory(dir.resolve("node_modules"))) return;
    installNodeDependencies(dir);
  }

  private void waitPortReady(int port, long timeoutMs) {
    long end = System.currentTimeMillis() + timeoutMs;
    IOException lastConnectException = null;
    while (System.currentTimeMillis() < end) {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
        return;
      } catch (IOException connectException) {
        lastConnectException = connectException;
        try {
          Thread.sleep(250);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
    if (lastConnectException == null) {
      throw badRequest("模块后端端口未就绪: " + port);
    }
    throw badRequest("模块后端端口未就绪: " + port);
  }

  private String normalizeKey(String moduleKey) {
    return String.valueOf(moduleKey == null ? "" : moduleKey).trim().toLowerCase(Locale.ROOT);
  }

  private String getEnv(String key, String defaultValue) {
    String v = environment.getProperty(key);
    return v == null || v.isBlank() ? defaultValue : v.trim();
  }

  private int parsePort(String value, int defaultValue) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException numberFormatException) {
      log.debug("端口配置解析失败，使用默认值，value={}, default={}", value, defaultValue, numberFormatException);
      return defaultValue;
    }
  }

  private record RunningProcess(Process process, int port) {}

  private static class PortAllocator {
    static int allocatePort() {
      try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
        socket.setReuseAddress(true);
        return socket.getLocalPort();
      } catch (IOException e) {
        throw badRequest("无法分配端口: " + e.getMessage());
      }
    }
  }
}
