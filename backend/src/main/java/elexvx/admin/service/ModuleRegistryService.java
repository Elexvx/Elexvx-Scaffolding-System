package elexvx.admin.service;

import elexvx.admin.entity.ModuleRegistry;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.exception.ModuleUnavailableException;
import elexvx.admin.mapper.ModuleRegistryMapper;
import elexvx.admin.module.ModuleDefinition;
import elexvx.admin.module.ModuleDefinitionRegistry;
import elexvx.admin.module.ModuleInstallationContext;
import elexvx.admin.vo.ModuleDescriptor;
import elexvx.admin.vo.ModuleRegistryResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModuleRegistryService {
  private static final Logger log = LoggerFactory.getLogger(ModuleRegistryService.class);
  private static final String LICENSE_APACHE = "Apache-2.0";
  private static final String STATE_PENDING = "PENDING";
  private static final String STATE_INSTALLED = "INSTALLED";
  private static final String STATE_FAILED = "FAILED";
  private static final String STATE_UNINSTALLED = "UNINSTALLED";

  private final Environment environment;
  private final ModuleRegistryMapper registryMapper;
  private final ModuleDefinitionRegistry definitionRegistry;
  private final DataSource dataSource;
  private final ResourceLoader resourceLoader;
  private final ModulePackageService modulePackageService;
  private final ModuleBackendProcessManager moduleBackendProcessManager;

  public ModuleRegistryService(
    Environment environment,
    ModuleRegistryMapper registryMapper,
    ModuleDefinitionRegistry definitionRegistry,
    DataSource dataSource,
    ResourceLoader resourceLoader,
    ModulePackageService modulePackageService,
    ModuleBackendProcessManager moduleBackendProcessManager
  ) {
    this.environment = environment;
    this.registryMapper = registryMapper;
    this.definitionRegistry = definitionRegistry;
    this.dataSource = dataSource;
    this.resourceLoader = resourceLoader;
    this.modulePackageService = modulePackageService;
    this.moduleBackendProcessManager = moduleBackendProcessManager;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public List<ModuleDescriptor> listModules() {
    return buildRegistryMap().values().stream()
        .sorted(Comparator.comparing(ModuleRegistry::getModuleKey, Comparator.nullsLast(String::compareToIgnoreCase)))
        .map(this::toDescriptor)
        .toList();
  }

  public List<ModuleRegistryResponse> listRegistries() {
    return buildRegistryMap().values().stream()
        .sorted(Comparator.comparing(ModuleRegistry::getModuleKey, Comparator.nullsLast(String::compareToIgnoreCase)))
        .map(ModuleRegistryResponse::from)
        .toList();
  }

  public void assertModuleAvailable(String moduleKey) {
    String key = normalizeKey(moduleKey);
    ModuleRegistry registry = registryMapper.selectByKey(key);
    if (registry == null) {
      throw new ModuleUnavailableException("模块未安装: " + moduleKey);
    }
    if (!STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) {
      throw new ModuleUnavailableException("模块未安装: " + moduleKey);
    }
    if (!Boolean.TRUE.equals(registry.getEnabled())) {
      throw new ModuleUnavailableException("模块未启用: " + moduleKey);
    }
    if (!isRuntimeDependencyAvailable(key)) {
      throw new ModuleUnavailableException("模块依赖未就绪: " + moduleKey);
    }
  }

  public boolean isModuleAvailable(String moduleKey) {
    String key = normalizeKey(moduleKey);
    ModuleRegistry registry = registryMapper.selectByKey(key);
    if (registry == null) return false;
    if (!STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) return false;
    if (!Boolean.TRUE.equals(registry.getEnabled())) return false;
    return isRuntimeDependencyAvailable(key);
  }

  @Transactional
  public ModuleRegistryResponse enableModule(String moduleKey, boolean enabled) {
    ModuleRegistry registry = requireRegistry(moduleKey);
    if (!STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) {
      throw badRequest("模块未安装: " + moduleKey);
    }
    if (enabled) {
      moduleBackendProcessManager.ensureRunning(registry.getModuleKey());
    }
    registry.setEnabled(enabled);
    registryMapper.update(registry);
    if (!enabled) {
      moduleBackendProcessManager.stop(registry.getModuleKey());
    }
    return ModuleRegistryResponse.from(registry);
  }

  @Transactional
  public ModuleRegistryResponse installModule(String moduleKey) {
    ModuleDefinition definition = requireDefinition(moduleKey);
    ModuleRegistry registry = getOrCreateRegistry(definition);
    if (STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) {
      return ModuleRegistryResponse.from(registry);
    }
    ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader, modulePackageService.getExternalRoot());
    try {
      definition.initialize(context);
      moduleBackendProcessManager.ensureDependenciesInstalled(registry.getModuleKey());
      registry.setInstallState(STATE_INSTALLED);
      registry.setInstalledAt(LocalDateTime.now());
      registry.setEnabled(true);
      registryMapper.update(registry);
      return ModuleRegistryResponse.from(registry);
    } catch (Exception ex) {
      registry.setInstallState(STATE_FAILED);
      registryMapper.update(registry);
      log.error("模块安装失败，moduleKey={}", registry.getModuleKey(), ex);
      throw new BusinessException(ErrorCodes.INTERNAL_SERVER_ERROR, "模块安装失败，请稍后重试", ex);
    }
  }

  @Transactional
  public ModuleRegistryResponse uninstallModule(String moduleKey) {
    ModuleDefinition definition = requireDefinition(moduleKey);
    ModuleRegistry registry = getOrCreateRegistry(definition);
    ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader, modulePackageService.getExternalRoot());
    moduleBackendProcessManager.stop(registry.getModuleKey());
    try {
      definition.uninstall(context);
      registry.setInstallState(STATE_UNINSTALLED);
      registry.setEnabled(false);
      registry.setInstalledAt(null);
      registryMapper.update(registry);
      return ModuleRegistryResponse.from(registry);
    } catch (Exception ex) {
      registry.setInstallState(STATE_FAILED);
      registryMapper.update(registry);
      log.error("模块卸载失败，moduleKey={}", registry.getModuleKey(), ex);
      throw new BusinessException(ErrorCodes.INTERNAL_SERVER_ERROR, "模块卸载失败，请稍后重试", ex);
    }
  }

  @Transactional
  public void autoInstallModules() {
    for (ModuleDefinition definition : definitionRegistry.getDefinitions().values()) {
      if (definition == null || definition.getKey() == null) continue;
      ModuleRegistry registry = getOrCreateRegistry(definition);
      String state = normalizeState(registry.getInstallState());
      if (STATE_INSTALLED.equalsIgnoreCase(state)) continue;
      if (!definition.isEnabledByDefault() && (state == null || STATE_PENDING.equalsIgnoreCase(state))) {
        continue;
      }
      try {
        ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader, modulePackageService.getExternalRoot());
        definition.initialize(context);
        registry.setInstallState(STATE_INSTALLED);
        registry.setEnabled(true);
        registry.setInstalledAt(LocalDateTime.now());
        registryMapper.update(registry);
      } catch (Exception ex) {
        registry.setInstallState(STATE_FAILED);
        registryMapper.update(registry);
        log.warn("模块自动安装失败，moduleKey={}", registry.getModuleKey(), ex);
      }
    }
  }

  private ModuleDescriptor buildModule(
    String key,
    String name,
    String source,
    String license,
    String version,
    boolean enabled
  ) {
    ModuleDescriptor descriptor = new ModuleDescriptor();
    descriptor.setKey(key);
    descriptor.setName(name);
    descriptor.setSource(source);
    descriptor.setLicense(license);
    descriptor.setVersion(version);
    descriptor.setEnabled(enabled);
    return descriptor;
  }


  private ModuleDefinition requireDefinition(String moduleKey) {
    ModuleDefinition definition = definitionRegistry.getDefinition(moduleKey);
    if (definition == null) {
      throw badRequest("模块不存在: " + moduleKey);
    }
    return definition;
  }

  private ModuleRegistry requireRegistry(String moduleKey) {
    ModuleRegistry registry = registryMapper.selectByKey(normalizeKey(moduleKey));
    if (registry == null) {
      throw badRequest("模块未安装: " + moduleKey);
    }
    return registry;
  }

  private ModuleRegistry getOrCreateRegistry(ModuleDefinition definition) {
    String key = normalizeKey(definition.getKey());
    ModuleRegistry registry = registryMapper.selectByKey(key);
    if (registry != null) return registry;
    registry = buildRegistry(definition);
    registryMapper.insert(registry);
    return registry;
  }

  private ModuleRegistry buildRegistry(ModuleDefinition definition) {
    ModuleRegistry registry = new ModuleRegistry();
    registry.setModuleKey(normalizeKey(definition.getKey()));
    registry.setName(definition.getName());
    registry.setVersion(definition.getVersion());
    registry.setEnabled(definition.isEnabledByDefault());
    registry.setInstallState(STATE_PENDING);
    registry.setInstalledAt(null);
    return registry;
  }

  private boolean getFlag(String key, boolean fallback) {
    String raw = environment.getProperty(key);
    if (raw == null) return fallback;
    return Boolean.parseBoolean(raw);
  }


  private Map<String, ModuleRegistry> buildRegistryMap() {
    List<ModuleRegistry> registries = registryMapper.selectAll();
    Map<String, ModuleRegistry> map = new LinkedHashMap<>();
    if (registries != null) {
      for (ModuleRegistry registry : registries) {
        if (registry == null || registry.getModuleKey() == null) continue;
        map.put(normalizeKey(registry.getModuleKey()), registry);
      }
    }
    for (ModuleDefinition definition : definitionRegistry.getDefinitions().values()) {
      if (definition == null || definition.getKey() == null) continue;
      String key = normalizeKey(definition.getKey());
      if (map.containsKey(key)) continue;
      map.put(key, buildRegistry(definition));
    }
    return map;
  }

  private ModuleDescriptor toDescriptor(ModuleRegistry registry) {
    String key = normalizeKey(registry.getModuleKey());
    boolean enabled =
      STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))
      && Boolean.TRUE.equals(registry.getEnabled())
      && isRuntimeDependencyAvailable(key);
    return buildModule(
      key,
      registry.getName(),
      resolveSource(key),
      LICENSE_APACHE,
      registry.getVersion(),
      enabled
    );
  }

  private String resolveSource(String key) {
    if ("sms".equals(key)) {
      return "Aliyun Dysmsapi SDK / TencentCloud SMS SDK";
    }
    if ("email".equals(key)) {
      return "Spring Boot Mail Starter";
    }
    return "Built-in Module";
  }

  private boolean isRuntimeDependencyAvailable(String key) {
    if ("sms".equals(key)) {
      if (!getFlag("elexvx.modules.sms.enabled", true)) return false;
      return isClassPresent("com.aliyuncs.DefaultAcsClient")
        || isClassPresent("com.tencentcloudapi.sms.v20210111.SmsClient");
    }
    if ("email".equals(key)) {
      if (!getFlag("elexvx.modules.email.enabled", true)) return false;
      return isClassPresent("org.springframework.mail.javamail.JavaMailSenderImpl");
    }
    return true;
  }
  private String normalizeKey(String moduleKey) {
    if (moduleKey == null) return "";
    return moduleKey.trim().toLowerCase();
  }

  private String normalizeState(String state) {
    if (state == null) return null;
    return state.trim().toUpperCase();
  }

  private boolean isClassPresent(String className) {
    return ClassUtils.isPresent(className, getClass().getClassLoader());
  }
}
