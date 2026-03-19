package elexvx.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elexvx.admin.dto.AiProviderRequest;
import elexvx.admin.entity.AiProviderSetting;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.AiProviderMapper;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.AiChatResult;
import elexvx.admin.vo.AiProviderResponse;
import elexvx.admin.vo.AiTestResult;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiProviderService {
  private static final Logger log = LoggerFactory.getLogger(AiProviderService.class);
  private static final Map<String, String> DEFAULT_BASE_URL = Map.ofEntries(
    Map.entry("OPENAI", "https://api.openai.com"),
    Map.entry("AZURE_OPENAI", ""),
    Map.entry("DEEPSEEK", "https://api.deepseek.com"),
    Map.entry("MOONSHOT", "https://api.moonshot.cn"),
    Map.entry("QWEN", "https://dashscope.aliyuncs.com/compatible-mode"),
    Map.entry("OLLAMA", "http://127.0.0.1:11434")
  );

  private final AiProviderMapper mapper;
  private final ObjectMapper objectMapper;

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public AiProviderService(AiProviderMapper mapper, ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.objectMapper = objectMapper;
  }

  public List<AiProviderResponse> list() {
    PermissionUtil.check("system:SystemAi:query");
    return mapper.selectAll().stream().map(AiProviderResponse::from).toList();
  }

    public AiProviderSetting requireProvider(Long id) {
    if (id == null) {
      AiProviderSetting def = mapper.selectFirstDefaultEnabled();
      return def != null ? def : firstEnabledProvider();
    }
    AiProviderSetting found = mapper.selectById(id);
    if (found == null) {
      throw badRequest("指定的 AI 配置不存在");
    }
    return found;
  }


    private AiProviderSetting firstEnabledProvider() {
    AiProviderSetting found = mapper.selectFirstEnabled();
    if (found != null) return found;
    throw new IllegalStateException("系统中没有可用的 AI 提供商，请先配置并启用至少一个");
  }


    @Transactional
  public AiProviderResponse save(AiProviderRequest req) {
    PermissionUtil.check("system:SystemAi:update");
    AiProviderSetting entity = req.getId() == null ? null : mapper.selectById(req.getId());
    if (entity == null) entity = new AiProviderSetting();
    apply(entity, req);
    if (Boolean.TRUE.equals(req.getIsDefaultProvider())) {
      entity.setIsDefaultProvider(true);
    } else if (entity.getIsDefaultProvider() == null) {
      entity.setIsDefaultProvider(false);
    }
    entity.setUpdatedAt(LocalDateTime.now());
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(LocalDateTime.now());
    }
    AiProviderSetting saved = saveProvider(entity);
    if (Boolean.TRUE.equals(saved.getIsDefaultProvider())) {
      mapper.clearDefaultExcept(saved.getId());
    }
    return AiProviderResponse.from(saved);
  }



  private AiProviderSetting saveProvider(AiProviderSetting entity) {
    if (entity.getId() == null) {
      mapper.insert(entity);
    } else {
      mapper.update(entity);
    }
    return entity;
  }

  private void apply(AiProviderSetting target, AiProviderRequest req) {
    target.setName(req.getName().trim());
    target.setVendor(req.getVendor().trim().toUpperCase());
    target.setBaseUrl(normalizeBaseUrl(req.getBaseUrl(), target.getVendor()));
    target.setEndpointPath(normalizeEndpoint(req.getEndpointPath(), target.getVendor()));
    target.setModel(blankToNull(req.getModel()));
    target.setApiVersion(blankToNull(req.getApiVersion()));
    target.setTemperature(req.getTemperature() == null ? 0.7 : req.getTemperature());
    target.setMaxTokens(req.getMaxTokens());
    target.setIsDefaultProvider(Boolean.TRUE.equals(req.getIsDefaultProvider()));
    target.setEnabled(req.getEnabled() == null ? Boolean.TRUE : req.getEnabled());
    target.setExtraHeaders(blankToNull(req.getExtraHeaders()));
    target.setRemark(blankToNull(req.getRemark()));
    if (StringUtils.hasText(req.getApiKey())) {
      target.setApiKey(req.getApiKey().trim());
    } else if (!Boolean.TRUE.equals(req.getReuseApiKey()) && req.getId() != null) {
      target.setApiKey(null);
    }
  }

  @Transactional
  public void delete(Long id) {
    PermissionUtil.check("system:SystemAi:delete");
    mapper.deleteById(id);
  }

  @Transactional
  public AiTestResult test(AiProviderRequest req) {
    PermissionUtil.check("system:SystemAi:update");
    AiProviderSetting tmp = new AiProviderSetting();
    apply(tmp, req);
    return doTest(tmp, "连通性测试");
  }

    @Transactional
  public AiTestResult testSaved(Long id) {
    PermissionUtil.check("system:SystemAi:update");
    AiProviderSetting saved = java.util.Optional.ofNullable(mapper.selectById(id))
      .orElseThrow(() -> badRequest("AI 配置不存在或已被删除"));
    AiTestResult result = doTest(saved, "已保存配置测试");
    saved.setLastTestStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
    saved.setLastTestMessage(result.getMessage());
    saved.setLastTestedAt(LocalDateTime.now());
    saveProvider(saved);
    return result;
  }


  public AiChatResult chat(AiProviderSetting provider, String systemPrompt, String message) throws Exception {
    AiProviderSetting ready = normalize(provider);
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    Map<String, Object> payload = new HashMap<>();
    payload.put("model", ready.getModel());
    payload.put(
      "messages",
      List.of(
        Map.of("role", "system", "content", systemPrompt == null ? "" : systemPrompt),
        Map.of("role", "user", "content", message == null ? "" : message)
      )
    );
    payload.put("stream", false);
    if (ready.getTemperature() != null) payload.put("temperature", ready.getTemperature());
    if (ready.getMaxTokens() != null) payload.put("max_tokens", ready.getMaxTokens());

    String url = buildUrl(ready);
    HttpRequest.Builder builder = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .timeout(Duration.ofSeconds(20))
      .header("Content-Type", "application/json");
    applyHeaders(builder, ready);

    String body = objectMapper.writeValueAsString(payload);
    HttpRequest req = builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 400) {
      throw new IllegalStateException("AI 请求失败，HTTP " + resp.statusCode() + ": " + safeMessage(resp.body()));
    }
    String text = extractMessage(resp.body());
    AiChatResult result = new AiChatResult();
    result.setReply(text);
    result.setProviderName(ready.getName());
    result.setModel(ready.getModel());
    return result;
  }

  private AiTestResult doTest(AiProviderSetting setting, String scene) {
    long start = System.currentTimeMillis();
    try {
      String prompt = "健康检查：" + scene;
      AiChatResult result = chat(setting, "你是一个健康检查助手，请简要回答即可。", prompt);
      long cost = System.currentTimeMillis() - start;
      return AiTestResult.ok("连接成功，返回: " + trimPreview(result.getReply()), cost);
    } catch (Exception e) {
      log.warn("AI provider test failed for {}: {}", scene, e.getMessage());
      String friendlyMsg = translateError(e);
      return AiTestResult.failed(friendlyMsg);
    }
  }

  private String translateError(Exception e) {
    String msg = e.getMessage();
    if (e instanceof java.net.ConnectException || (msg != null && msg.contains("Connection refused"))) {
      return "连接失败：目标服务器拒绝连接，请检查 Base URL 是否正确。";
    }
    if (e instanceof java.net.http.HttpConnectTimeoutException || (msg != null && msg.contains("connect timed out"))) {
      return "连接超时：无法连接到服务器，请检查网络或 Base URL。";
    }
    if (e instanceof java.net.http.HttpTimeoutException || (msg != null && msg.contains("request timed out"))) {
      return "请求超时：服务器响应过慢。";
    }
    if (e instanceof java.net.UnknownHostException || (msg != null && msg.contains("UnknownHostException"))) {
      return "连接失败：域名解析失败，请检查 Base URL。";
    }
    if (e instanceof IllegalStateException && msg != null && msg.contains("HTTP 401")) {
      return "认证失败：API Key 无效或已过期。";
    }
    if (e instanceof IllegalStateException && msg != null && msg.contains("HTTP 404")) {
      return "连接失败：接口路径 (Endpoint) 不存在。";
    }
    if (e instanceof IllegalStateException && msg != null && msg.contains("HTTP 429")) {
      return "请求过多：API 调用额度已耗尽或触发限流。";
    }
    if (e instanceof IllegalStateException && msg != null && msg.contains("HTTP")) {
      return "服务异常：" + msg;
    }
    return msg != null ? msg : "未知错误";
  }

  private AiProviderSetting normalize(AiProviderSetting raw) {
    AiProviderSetting cloned = new AiProviderSetting();
    cloned.setId(raw.getId());
    cloned.setName(raw.getName());
    cloned.setVendor(raw.getVendor());
    cloned.setBaseUrl(normalizeBaseUrl(raw.getBaseUrl(), raw.getVendor()));
    cloned.setEndpointPath(normalizeEndpoint(raw.getEndpointPath(), raw.getVendor()));
    cloned.setModel(raw.getModel());
    cloned.setApiKey(raw.getApiKey());
    cloned.setApiVersion(raw.getApiVersion());
    cloned.setTemperature(raw.getTemperature());
    cloned.setMaxTokens(raw.getMaxTokens());
    cloned.setIsDefaultProvider(raw.getIsDefaultProvider());
    cloned.setEnabled(raw.getEnabled());
    cloned.setExtraHeaders(raw.getExtraHeaders());
    cloned.setRemark(raw.getRemark());
    return cloned;
  }

  private void applyHeaders(HttpRequest.Builder builder, AiProviderSetting setting) {
    Map<String, String> headers = parseHeaders(setting.getExtraHeaders());
    headers.forEach(builder::header);
    String vendor = safeUpper(setting.getVendor());
    if ("AZURE_OPENAI".equals(vendor)) {
      builder.header("api-key", safe(setting.getApiKey()));
      if (StringUtils.hasText(setting.getApiVersion())) {
        builder.header("x-api-version", setting.getApiVersion());
      }
    } else if ("OLLAMA".equals(vendor)) {
      // no auth by default
      if (StringUtils.hasText(setting.getApiKey())) {
        builder.header("Authorization", "Bearer " + setting.getApiKey().trim());
      }
    } else {
      builder.header("Authorization", "Bearer " + safe(setting.getApiKey()));
    }
  }

  private Map<String, String> parseHeaders(String json) {
    if (!StringUtils.hasText(json)) return Map.of();
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (Exception e) {
      log.warn("Parse custom headers failed: {}", e.getMessage());
      return Map.of();
    }
  }

  private String buildUrl(AiProviderSetting setting) {
    String base = setting.getBaseUrl();
    String path = StringUtils.hasText(setting.getEndpointPath()) ? setting.getEndpointPath() : "/v1/chat/completions";
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
    if (!path.startsWith("/")) path = "/" + path;
    return base + path;
  }

  private String extractMessage(String body) throws Exception {
    Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<>() {});
    Object choicesObj = parsed.get("choices");
    if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
      Object first = choices.get(0);
      if (first instanceof Map<?, ?> choice) {
        Object messageObj = choice.get("message");
        if (messageObj instanceof Map<?, ?> m) {
          Object content = m.get("content");
          if (content != null) return content.toString();
        }
        Object text = choice.get("text");
        if (text != null) return text.toString();
      }
    }
    Object data = parsed.get("data");
    return data == null ? "" : data.toString();
  }


  private String normalizeBaseUrl(String baseUrl, String vendor) {
    String cleaned = blankToNull(baseUrl);
    if (!StringUtils.hasText(cleaned)) {
      cleaned = DEFAULT_BASE_URL.getOrDefault(safeUpper(vendor), "");
    }
    if (!cleaned.startsWith("http")) {
      cleaned = "https://" + cleaned;
    }
    if (cleaned.endsWith("/")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1);
    }
    return cleaned;
  }

  private String normalizeEndpoint(String endpoint, String vendor) {
    if (StringUtils.hasText(endpoint)) {
      return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }
    if ("AZURE_OPENAI".equalsIgnoreCase(vendor)) {
      return "/openai/deployments/{deployment}/chat/completions?api-version=2024-02-15-preview";
    }
    if ("OLLAMA".equalsIgnoreCase(vendor)) {
      return "/api/chat";
    }
    return "/v1/chat/completions";
  }

  private String blankToNull(String value) {
    if (value == null) return null;
    String v = value.trim();
    return v.isEmpty() ? null : v;
  }

  private String safe(String value) {
    return value == null ? "" : value.trim();
  }

  private String safeUpper(String value) {
    return value == null ? "" : value.trim().toUpperCase();
  }

  private String trimPreview(String text) {
    if (text == null) return "";
    String t = text.replaceAll("\\s+", " ").trim();
    return t.length() > 80 ? t.substring(0, 80) + "..." : t;
  }

  private String safeMessage(String body) {
    if (body == null) return "";
    String trimmed = body.trim();
    return trimmed.length() > 200 ? trimmed.substring(0, 200) + "..." : trimmed;
  }
}
