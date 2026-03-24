package elexvx.admin.config;

import elexvx.admin.web.RequestTraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestIdFilter extends OncePerRequestFilter {
  private static final int MAX_REQUEST_ID_LENGTH = 64;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    String requestId = resolveRequestId(request.getHeader(RequestTraceContext.REQUEST_ID_HEADER));
    request.setAttribute(RequestTraceContext.REQUEST_ID_ATTRIBUTE, requestId);
    response.setHeader(RequestTraceContext.REQUEST_ID_HEADER, requestId);
    MDC.put(RequestTraceContext.MDC_REQUEST_ID_KEY, requestId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(RequestTraceContext.MDC_REQUEST_ID_KEY);
    }
  }

  private String resolveRequestId(String headerValue) {
    if (headerValue == null) {
      return generateRequestId();
    }
    String candidate = headerValue.trim();
    if (
      candidate.isBlank()
        || candidate.length() > MAX_REQUEST_ID_LENGTH
        || !candidate.matches("^[A-Za-z0-9._:-]+$")
    ) {
      return generateRequestId();
    }
    return candidate;
  }

  private String generateRequestId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
