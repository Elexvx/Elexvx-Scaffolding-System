package com.tencent.tdesign.controller;

import com.tencent.tdesign.dto.AiProviderRequest;
import com.tencent.tdesign.service.AiProviderService;
import com.tencent.tdesign.service.AiService;
import com.tencent.tdesign.service.ModuleRegistryService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.AiChatResult;
import com.tencent.tdesign.vo.AiProviderResponse;
import com.tencent.tdesign.vo.AiTestResult;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 助手控制器
 * 负责提供聊天、工具和模型配置能力
 */
@RestController
@RequestMapping("/ai")
public class AiController {
  private final AiService aiService;
  private final AiProviderService providerService;
  private final ModuleRegistryService moduleRegistryService;
  private final ExecutorService executor = Executors.newCachedThreadPool();

  public AiController(AiService aiService, AiProviderService providerService, ModuleRegistryService moduleRegistryService) {
    this.aiService = aiService;
    this.providerService = providerService;
    this.moduleRegistryService = moduleRegistryService;
  }

  private void requireModule() {
    moduleRegistryService.assertModuleAvailable("ai");
  }

  private void requireAiQueryPermission() {
    PermissionUtil.check("system:SystemAi:query");
  }

  @GetMapping("/tools")
  public ApiResponse<List<Map<String, Object>>> getTools() {
    requireModule();
    requireAiQueryPermission();
    return ApiResponse.success(aiService.getToolsSchema());
  }

  @GetMapping("/providers")
  public ApiResponse<List<AiProviderResponse>> providers() {
    requireModule();
    requireAiQueryPermission();
    return ApiResponse.success(providerService.list());
  }

  @PostMapping("/providers")
  public ApiResponse<AiProviderResponse> saveProvider(@RequestBody @Valid AiProviderRequest req) {
    requireModule();
    return ApiResponse.success(providerService.save(req));
  }

  @PostMapping("/providers/test")
  public ApiResponse<AiTestResult> testProvider(@RequestBody @Valid AiProviderRequest req) {
    requireModule();
    return ApiResponse.success(providerService.test(req));
  }

  @PostMapping("/providers/{id}/test")
  public ApiResponse<AiTestResult> testSavedProvider(@PathVariable Long id) {
    requireModule();
    return ApiResponse.success(providerService.testSaved(id));
  }

  @DeleteMapping("/providers/{id}")
  public ApiResponse<Boolean> deleteProvider(@PathVariable Long id) {
    requireModule();
    providerService.delete(id);
    return ApiResponse.success(true);
  }

  @PostMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter chatSse(@RequestBody AiChatRequest request) {
    requireModule();
    requireAiQueryPermission();
    SseEmitter emitter = new SseEmitter(0L);
    executor.execute(() -> {
      try {
        var provider = providerService.requireProvider(request.getProviderId());
        sendSseEvent(emitter, "meta", "正在使用模型：" + provider.getModel());
        String systemPrompt = buildSystemPrompt(request.getSystemPrompt());
        AiChatResult result = providerService.chat(provider, systemPrompt, request.getMessage());
        sendSseEvent(emitter, "text", result.getReply());
        emitter.complete();
      } catch (Exception e) {
        safeError(emitter, e);
      }
    });
    return emitter;
  }

  @PostMapping("/chat")
  public ApiResponse<AiChatResult> chat(@RequestBody AiChatRequest request) throws Exception {
    requireModule();
    requireAiQueryPermission();
    var provider = providerService.requireProvider(request.getProviderId());
    String systemPrompt = buildSystemPrompt(request.getSystemPrompt());
    AiChatResult result = providerService.chat(provider, systemPrompt, request.getMessage());
    return ApiResponse.success(result);
  }

  @PostMapping("/execute")
  public ApiResponse<Object> executeTool(@RequestBody AiExecuteRequest request) {
    requireModule();
    requireAiQueryPermission();
    try {
      Object result = aiService.executeTool(request.getToolName(), request.getArgs());
      return ApiResponse.success(result);
    } catch (Exception e) {
      return ApiResponse.failure(500, "AI 执行工具失败: " + e.getMessage());
    }
  }

  private void sendSseEvent(SseEmitter emitter, String type, String content) throws IOException {
    String json = String.format("{\"type\": \"%s\", \"content\": \"%s\"}", type, content.replace("\"", "\\\""));
    emitter.send(SseEmitter.event().data(Objects.requireNonNull(json)));
  }

  private void safeError(SseEmitter emitter, Exception e) {
    try {
      sendSseEvent(emitter, "error", e.getMessage());
    } catch (IOException ignored) {}
    emitter.completeWithError(e);
  }

  private String buildSystemPrompt(String customPrompt) {
    StringBuilder sb = new StringBuilder();
    sb.append("你是内置的运维助手，需遵守安全边界：严禁执行删除、重置、越权等危险操作；仅在拥有权限的情况下调用工具。");
    sb.append("如遇受限请求，请明确告诉用户无法执行。");
    List<AiService.AiToolDefinition> tools = aiService.getAccessibleTools();
    if (!tools.isEmpty()) {
      sb.append("可用工具列表：");
      for (AiService.AiToolDefinition tool : tools) {
        sb.append("- ").append(tool.getName()).append("：").append(tool.getDescription()).append("; ");
      }
    }
    if (customPrompt != null && !customPrompt.isBlank()) {
      sb.append(" 额外指令：").append(customPrompt.trim());
    }
    return sb.toString();
  }

  public static class AiChatRequest {
    private String message;
    private Long providerId;
    private String systemPrompt;

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public Long getProviderId() {
      return providerId;
    }

    public void setProviderId(Long providerId) {
      this.providerId = providerId;
    }

    public String getSystemPrompt() {
      return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
      this.systemPrompt = systemPrompt;
    }
  }

  public static class AiExecuteRequest {
    private String toolName;
    private Map<String, Object> args;

    public String getToolName() {
      return toolName;
    }

    public void setToolName(String toolName) {
      this.toolName = toolName;
    }

    public Map<String, Object> getArgs() {
      return args;
    }

    public void setArgs(Map<String, Object> args) {
      this.args = args;
    }
  }
}
