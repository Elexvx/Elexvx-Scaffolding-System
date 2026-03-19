package top.elexvx.admin.config;

import top.elexvx.admin.config.properties.ElexvxCoreProperties;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class SecurityBaselineValidator {
  private static final String DEFAULT_FIELD_SECRET = "sdfghjh533gfdc8768vgcchg6fgvhbjhgf";
  private static final Set<String> WEAK_DB_PASSWORDS = Set.of(
    "123456",
    "12345678",
    "password",
    "root",
    "admin",
    "qwe123",
    "111111"
  );

  private final ElexvxCoreProperties properties;
  private final boolean springdocApiDocsEnabled;
  private final boolean swaggerUiEnabled;
  private final String datasourcePassword;

  public SecurityBaselineValidator(
    ElexvxCoreProperties properties,
    @org.springframework.beans.factory.annotation.Value("${springdoc.api-docs.enabled:true}") boolean springdocApiDocsEnabled,
    @org.springframework.beans.factory.annotation.Value("${springdoc.swagger-ui.enabled:true}") boolean swaggerUiEnabled,
    @org.springframework.beans.factory.annotation.Value("${spring.datasource.password:}") String datasourcePassword
  ) {
    this.properties = properties;
    this.springdocApiDocsEnabled = springdocApiDocsEnabled;
    this.swaggerUiEnabled = swaggerUiEnabled;
    this.datasourcePassword = datasourcePassword;
  }

  @PostConstruct
  public void validateProdSecurityBaseline() {
    String allowedOrigins = properties.getWeb().getCors().getAllowedOriginPatterns();
    String fileTokenSecret = properties.getFile().getTokenSecret();
    String fieldSecret = properties.getSecurity().getFieldSecret();
    String v = allowedOrigins == null ? "" : allowedOrigins.trim();
    if (v.isEmpty() || "*".equals(v) || v.contains("*")) {
      throw new IllegalStateException("生产环境必须显式配置 elexvx.web.cors.allowed-origin-patterns，且禁止使用通配符");
    }
    if (fileTokenSecret == null || fileTokenSecret.trim().isEmpty()) {
      throw new IllegalStateException("生产环境必须配置 elexvx.file.token-secret");
    }
    String safeFieldSecret = fieldSecret == null ? "" : fieldSecret.trim();
    if (safeFieldSecret.isEmpty() || DEFAULT_FIELD_SECRET.equals(safeFieldSecret)) {
      throw new IllegalStateException("生产环境必须替换 ELEXVX_SECURITY_FIELD_SECRET 默认值");
    }
    if (springdocApiDocsEnabled || swaggerUiEnabled) {
      throw new IllegalStateException("生产环境必须关闭 Swagger 与 springdoc 文档端点");
    }
    String safeDatasourcePassword = datasourcePassword == null ? "" : datasourcePassword.trim().toLowerCase();
    if (safeDatasourcePassword.isEmpty() || WEAK_DB_PASSWORDS.contains(safeDatasourcePassword)) {
      throw new IllegalStateException("生产环境检测到数据库弱口令，请使用高强度密码");
    }
  }
}
