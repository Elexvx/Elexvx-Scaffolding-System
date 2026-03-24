package elexvx.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupEndpointLogger implements ApplicationListener<WebServerInitializedEvent> {
  private static final Logger log = LoggerFactory.getLogger(StartupEndpointLogger.class);
  private final Environment environment;

  public StartupEndpointLogger(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    int port = event.getWebServer().getPort();
    String scheme = isSslEnabled() ? "https" : "http";
    String host = resolveHost();
    String contextPath = normalizeContextPath(environment.getProperty("server.servlet.context-path", ""));

    String base = scheme + "://" + host + ":" + port + contextPath;
    log.info("Backend URL: {}", base);
    log.info("Health URL: {}/health", base);
    log.info("Swagger URL: {}/swagger-ui/index.html", base);
    log.info("OpenAPI URL: {}/v3/api-docs", base);
  }

  private boolean isSslEnabled() {
    String enabled = environment.getProperty("server.ssl.enabled");
    if (enabled != null && enabled.equalsIgnoreCase("true")) return true;
    return environment.getProperty("server.ssl.key-store") != null;
  }

  private String resolveHost() {
    String address = environment.getProperty("server.address");
    if (address == null || address.isBlank() || address.equals("0.0.0.0")) return "localhost";
    return address;
  }

  private String normalizeContextPath(String raw) {
    if (raw == null) return "";
    String v = raw.trim();
    if (v.isEmpty() || v.equals("/")) return "";
    return v.startsWith("/") ? v : "/" + v;
  }
}
