package com.tencent.tdesign.config;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${tdesign.web.cors.allowed-origin-patterns:}")
  private String allowedOrigins;
  @Value("${tdesign.file.token-secret:}")
  private String fileTokenSecret;
  @Value("${tdesign.security.field-secret:}")
  private String fieldSecret;
  @Value("${springdoc.api-docs.enabled:true}")
  private boolean springdocApiDocsEnabled;
  @Value("${springdoc.swagger-ui.enabled:true}")
  private boolean swaggerUiEnabled;
  @Value("${spring.datasource.password:}")
  private String datasourcePassword;

  @PostConstruct
  public void validateProdSecurityBaseline() {
    String v = allowedOrigins == null ? "" : allowedOrigins.trim();
    if (v.isEmpty() || "*".equals(v) || v.contains("*")) {
      throw new IllegalStateException("生产环境必须显式配置 tdesign.web.cors.allowed-origin-patterns，且禁止使用通配符");
    }
    if (fileTokenSecret == null || fileTokenSecret.trim().isEmpty()) {
      throw new IllegalStateException("生产环境必须配置 tdesign.file.token-secret");
    }
    String safeFieldSecret = fieldSecret == null ? "" : fieldSecret.trim();
    if (safeFieldSecret.isEmpty() || DEFAULT_FIELD_SECRET.equals(safeFieldSecret)) {
      throw new IllegalStateException("生产环境必须替换 TDESIGN_SECURITY_FIELD_SECRET 默认值");
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
