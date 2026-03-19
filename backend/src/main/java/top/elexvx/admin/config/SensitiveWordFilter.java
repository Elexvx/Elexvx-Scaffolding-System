package top.elexvx.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.elexvx.admin.exception.ErrorCodes;
import top.elexvx.admin.service.SensitiveService;
import top.elexvx.admin.vo.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 敏感词拦截过滤器（JSON Body 扫描）。
 *
 * <p>仅对非 GET/HEAD/OPTIONS 的请求进行检查，且默认只扫描 {@code application/json} 请求体。
 * 页面维度开关由 {@link SensitiveService#isPageEnabled(String)} 控制，页面标识优先从请求头
 * {@code X-Page-Path} 获取，其次从 {@code Referer} 提取路径。
 *
 * <p>命中敏感词时返回 422，并输出统一响应体 {@link ApiResponse}。
 */
public class SensitiveWordFilter extends OncePerRequestFilter {
  private static final String HEADER_PAGE_PATH = "X-Page-Path";

  private final SensitiveService sensitiveService;
  private final ObjectMapper objectMapper;

  public SensitiveWordFilter(
    SensitiveService sensitiveService,
    ObjectMapper objectMapper
  ) {
    this.sensitiveService = sensitiveService;
    this.objectMapper = objectMapper;
  }

  @Override
  /**
   * 读取并缓存请求体后做敏感词检测。
   *
   * <p>若请求体不是合法 JSON，则直接放行以避免误伤非 JSON 内容。
   */
  protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
  )
    throws ServletException, IOException {
    if (!shouldCheck(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
    String pageKey = resolvePageKey(request);
    if (!sensitiveService.isPageEnabled(pageKey)) {
      filterChain.doFilter(cachedRequest, response);
      return;
    }

    String body = cachedRequest.getCachedBodyString();
    if (body == null || body.isBlank()) {
      filterChain.doFilter(cachedRequest, response);
      return;
    }

    Object payload;
    try {
      payload = objectMapper.readValue(body, Object.class);
    } catch (Exception e) {
      filterChain.doFilter(cachedRequest, response);
      return;
    }

    SensitiveService.SensitiveHit hit = sensitiveService.findHit(payload);
    if (hit != null) {
      respondViolation(response, hit);
      return;
    }

    filterChain.doFilter(cachedRequest, response);
  }

  private String resolvePageKey(HttpServletRequest request) {
    String header = request.getHeader(HEADER_PAGE_PATH);
    if (header != null && !header.isBlank()) return header;
    String referer = request.getHeader("Referer");
    if (referer == null || referer.isBlank()) return null;
    try {
      URI uri = URI.create(referer);
      return uri.getPath();
    } catch (Exception ignored) {
      return null;
    }
  }

  private boolean shouldCheck(HttpServletRequest request) {
    String method = request.getMethod();
    if (method == null) return false;
    String upper = method.toUpperCase();
    if ("GET".equals(upper) || "HEAD".equals(upper) || "OPTIONS".equals(upper)) {
      return false;
    }
    String uri = request.getRequestURI();
    if (uri != null && uri.contains("/system/sensitive")) {
      return false;
    }
    String contentType = request.getContentType();
    if (contentType == null || contentType.isBlank()) {
      return true;
    }
    return contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);
  }

  private void respondViolation(HttpServletResponse response, SensitiveService.SensitiveHit hit) throws IOException {
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String field = (hit.fieldPath() == null || hit.fieldPath().isBlank()) ? "输入内容" : hit.fieldPath();
    String word = hit.word() == null ? "" : hit.word();
    String message = word.isBlank()
      ? "字段 " + field + " 包含敏感词，提交已被拒绝"
      : "字段 " + field + " 包含敏感词“" + word + "”，提交已被拒绝";
    ApiResponse<Object> api = ApiResponse.failure(ErrorCodes.UNPROCESSABLE_ENTITY, message);
    response.getWriter().write(objectMapper.writeValueAsString(api));
  }
}
