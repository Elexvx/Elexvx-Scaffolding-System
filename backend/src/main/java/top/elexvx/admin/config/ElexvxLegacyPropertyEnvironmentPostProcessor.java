package top.elexvx.admin.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 新旧配置键兼容，后续版本再移除旧键。
 */
public class ElexvxLegacyPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
  private static final String ELEXVX_PREFIX = "elexvx.";
  private static final String LEGACY_PREFIX = "tdesign.";
  private static final String PROPERTY_SOURCE_NAME = "elexvxLegacyAliasProperties";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Map<String, Object> aliases = new LinkedHashMap<>();
    MutablePropertySources propertySources = environment.getPropertySources();
    for (PropertySource<?> propertySource : propertySources) {
      if (!(propertySource instanceof EnumerablePropertySource<?> enumerable)) {
        continue;
      }
      for (String propertyName : enumerable.getPropertyNames()) {
        if (propertyName.startsWith(ELEXVX_PREFIX)) {
          String legacyName = LEGACY_PREFIX + propertyName.substring(ELEXVX_PREFIX.length());
          if (environment.getProperty(legacyName) == null) {
            aliases.putIfAbsent(legacyName, environment.getProperty(propertyName));
          }
        } else if (propertyName.startsWith(LEGACY_PREFIX)) {
          String elexvxName = ELEXVX_PREFIX + propertyName.substring(LEGACY_PREFIX.length());
          if (environment.getProperty(elexvxName) == null) {
            aliases.putIfAbsent(elexvxName, environment.getProperty(propertyName));
          }
        }
      }
    }
    if (!aliases.isEmpty()) {
      propertySources.addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, aliases));
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
