package com.tencent.tdesign.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.service.ModulePackageService;
import com.tencent.tdesign.service.ModuleRegistryService;
import com.tencent.tdesign.service.SensitiveService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
  private final String corsAllowedOriginPatterns;
  private final ModulePackageService modulePackageService;
  private final boolean exposeUploads;

  public WebConfig(
    @Value("${tdesign.web.cors.allowed-origin-patterns:}") String corsAllowedOriginPatterns,
    @Value("${tdesign.web.expose-uploads:false}") boolean exposeUploads,
    ModulePackageService modulePackageService
  ) {
    this.corsAllowedOriginPatterns = corsAllowedOriginPatterns;
    this.exposeUploads = exposeUploads;
    this.modulePackageService = modulePackageService;
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
    List<String> allowedPatterns = splitCsv(corsAllowedOriginPatterns);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
    registration.setAsyncSupported(true);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

    // Keep CORS disabled when no whitelist is configured.
    // In this mode we expect same-origin access through reverse proxy (/api).
    if (allowedPatterns.isEmpty()) {
      registration.setEnabled(false);
      return registration;
    }

    CorsConfiguration config = new CorsConfiguration();
    for (String pattern : allowedPatterns) {
      config.addAllowedOriginPattern(pattern);
    }
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(false);
    config.addExposedHeader("Content-Disposition");
    config.setMaxAge(3600L);
    source.registerCorsConfiguration("/**", config);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<SensitiveWordFilter> sensitiveWordFilterRegistration(
    ModuleRegistryService moduleRegistryService,
    SensitiveService sensitiveService,
    ObjectMapper objectMapper
  ) {
    FilterRegistrationBean<SensitiveWordFilter> registration =
      new FilterRegistrationBean<>(new SensitiveWordFilter(moduleRegistryService, sensitiveService, objectMapper));
    registration.setAsyncSupported(true);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
    return registration;
  }

  @Override
  public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
    if (exposeUploads) {
      Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
      try {
        Files.createDirectories(uploadDir);
        Files.createDirectories(uploadDir.resolve("system"));
        Files.createDirectories(uploadDir.resolve("business"));
      } catch (Exception createUploadDirException) {
        log.warn("创建上传资源目录失败，path={}", uploadDir, createUploadDirException);
      }
      String base = "file:" + uploadDir.toAbsolutePath() + "/";
      registry.addResourceHandler("/uploads/system/**").addResourceLocations(base + "system/");
      registry.addResourceHandler("/uploads/business/**").addResourceLocations(base + "business/");
    }

    Path moduleFrontend = modulePackageService.getFrontendDir();
    try {
      Files.createDirectories(moduleFrontend);
    } catch (Exception createModuleFrontendDirException) {
      log.warn("创建模块前端目录失败，path={}", moduleFrontend, createModuleFrontendDirException);
    }
    String moduleBase = "file:" + moduleFrontend.toAbsolutePath() + "/";
    registry.addResourceHandler("/modules/**").addResourceLocations(moduleBase);
  }

  private List<String> splitCsv(String raw) {
    if (raw == null) return List.of();
    String cleaned = raw.trim();
    if (cleaned.isEmpty()) return List.of();
    String[] parts = cleaned.split(",");
    List<String> out = new ArrayList<>();
    for (String p : parts) {
      String v = p == null ? "" : p.trim();
      if (!v.isEmpty()) out.add(v);
    }
    return out;
  }
}
