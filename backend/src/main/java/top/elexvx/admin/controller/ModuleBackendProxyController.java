package top.elexvx.admin.controller;

import top.elexvx.admin.service.ModuleBackendProcessManager;
import top.elexvx.admin.service.ModuleRegistryService;
import top.elexvx.admin.util.PermissionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/module-api")
public class ModuleBackendProxyController {
  private static final Logger log = LoggerFactory.getLogger(ModuleBackendProxyController.class);
  private static final Pattern MODULE_KEY_PATTERN = Pattern.compile("^[a-z0-9-]+$");
  private static final Set<String> BLOCKED_REQUEST_HEADERS = Set.of(
    "host", "content-length", "transfer-encoding", "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "te", "trailer", "upgrade"
  );
  private static final Set<String> BLOCKED_RESPONSE_HEADERS = Set.of(
    "content-length", "transfer-encoding", "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "te", "trailer", "upgrade"
  );

  private final ModuleRegistryService moduleRegistryService;
  private final ModuleBackendProcessManager processManager;
  private final boolean forwardAuthToken;
  private final boolean requireModuleQueryPermission;
  private final HttpClient httpClient = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.NORMAL)
    .connectTimeout(Duration.ofSeconds(10))
    .build();

  public ModuleBackendProxyController(
    ModuleRegistryService moduleRegistryService,
    ModuleBackendProcessManager processManager,
    @Value("${elexvx.modules.backend.forward-auth-token:false}") boolean forwardAuthToken,
    @Value("${elexvx.modules.backend.require-module-query-permission:true}") boolean requireModuleQueryPermission
  ) {
    this.moduleRegistryService = moduleRegistryService;
    this.processManager = processManager;
    this.forwardAuthToken = forwardAuthToken;
    this.requireModuleQueryPermission = requireModuleQueryPermission;
  }

  @RequestMapping("/{moduleKey}/**")
  public void proxy(@PathVariable String moduleKey, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (requireModuleQueryPermission) {
      PermissionUtil.check("system:SystemModule:query");
    }
    String key = normalizeKey(moduleKey);
    if (!MODULE_KEY_PATTERN.matcher(key).matches()) {
      response.setStatus(400);
      response.getWriter().write("非法 moduleKey");
      return;
    }

    moduleRegistryService.assertModuleAvailable(key);
    processManager.ensureRunning(key);
    int port = processManager.getPort(key);
    if (port <= 0) {
      response.setStatus(503);
      response.getWriter().write("模块后端未就绪");
      return;
    }

    String requestUri = request.getRequestURI();
    String contextPath = String.valueOf(request.getContextPath() == null ? "" : request.getContextPath());
    String prefix = contextPath + "/module-api/" + key + "/";
    String rest = requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : "";
    String query = request.getQueryString();
    String target = "http://127.0.0.1:" + port + "/" + rest + (query == null || query.isBlank() ? "" : "?" + query);

    String method = String.valueOf(request.getMethod()).toUpperCase(Locale.ROOT);
    Duration timeout = timeoutFor(method, rest);
    HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(URI.create(target))
      .timeout(timeout)
      .method(method, buildBodyPublisher(request));

    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      if (name == null) continue;
      String lower = name.toLowerCase(Locale.ROOT);
      if (BLOCKED_REQUEST_HEADERS.contains(lower)) continue;
      if (!forwardAuthToken && ("authorization".equals(lower) || "cookie".equals(lower))) continue;
      Enumeration<String> values = request.getHeaders(name);
      while (values.hasMoreElements()) {
        String v = values.nextElement();
        if (v != null) builder.header(name, v);
      }
    }

    try {
      HttpResponse<java.io.InputStream> proxied = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
      response.setStatus(proxied.statusCode());
      for (var header : proxied.headers().map().entrySet()) {
        String name = header.getKey();
        if (name == null) continue;
        if (BLOCKED_RESPONSE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) continue;
        for (String v : header.getValue()) {
          response.addHeader(name, v);
        }
      }
      try (java.io.InputStream body = proxied.body()) {
        body.transferTo(response.getOutputStream());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      response.setStatus(502);
      response.getWriter().write("模块后端请求中断");
    } catch (Exception e) {
      log.error("模块后端代理失败，moduleKey={}", key, e);
      response.setStatus(502);
      response.getWriter().write("模块后端代理失败，请稍后重试");
    }
  }

  private HttpRequest.BodyPublisher buildBodyPublisher(HttpServletRequest request) {
    String method = String.valueOf(request.getMethod()).toUpperCase(Locale.ROOT);
    if ("GET".equals(method) || "HEAD".equals(method)) return HttpRequest.BodyPublishers.noBody();
    return HttpRequest.BodyPublishers.ofInputStream(() -> {
      try {
        return request.getInputStream();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private Duration timeoutFor(String method, String restPath) {
    String path = String.valueOf(restPath == null ? "" : restPath).toLowerCase(Locale.ROOT);
    if (path.contains("/render/pdf") || "POST".equals(method)) return Duration.ofSeconds(30);
    return Duration.ofSeconds(15);
  }

  private String normalizeKey(String moduleKey) {
    return String.valueOf(moduleKey == null ? "" : moduleKey).trim().toLowerCase(Locale.ROOT);
  }
}
