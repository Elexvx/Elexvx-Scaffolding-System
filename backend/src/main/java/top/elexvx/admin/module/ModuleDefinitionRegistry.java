package top.elexvx.admin.module;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import top.elexvx.admin.service.ModulePackageService;

@Component
/**
 * 模块定义注册表。
 *
 * <p>收集系统内声明的 {@link ModuleDefinition}（通常由各模块以 Bean 形式提供），并按 {@code key} 归一化
 *（trim + lower-case）后注册，供运行时查询模块元信息。
 */
public class ModuleDefinitionRegistry {
  private final Map<String, ModuleDefinition> builtinDefinitions;
  private final ModulePackageService modulePackageService;

  public ModuleDefinitionRegistry(List<ModuleDefinition> definitions, ModulePackageService modulePackageService) {
    Map<String, ModuleDefinition> map = new LinkedHashMap<>();
    if (definitions != null) {
      for (ModuleDefinition definition : definitions) {
        if (definition == null || definition.getKey() == null) {
          continue;
        }
        String key = definition.getKey().trim().toLowerCase();
        map.put(key, definition);
      }
    }
    this.builtinDefinitions = Collections.unmodifiableMap(map);
    this.modulePackageService = modulePackageService;
  }

  public Map<String, ModuleDefinition> getDefinitions() {
    Map<String, ModuleDefinition> merged = new LinkedHashMap<>(builtinDefinitions);
    if (modulePackageService != null) {
      for (ModuleDefinition def : modulePackageService.loadExternalDefinitions()) {
        if (def == null || def.getKey() == null || def.getKey().isBlank()) continue;
        String key = def.getKey().trim().toLowerCase();
        merged.putIfAbsent(key, def);
      }
    }
    return merged;
  }

  public ModuleDefinition getDefinition(String key) {
    if (key == null) return null;
    return getDefinitions().get(key.trim().toLowerCase());
  }
}
