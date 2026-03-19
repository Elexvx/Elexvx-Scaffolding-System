package elexvx.admin.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestTraceContext {
  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String REQUEST_ID_ATTRIBUTE = "elexvx.requestId";
  public static final String MDC_REQUEST_ID_KEY = "requestId";

  private RequestTraceContext() {}

  public static String currentRequestId() {
    String requestId = MDC.get(MDC_REQUEST_ID_KEY);
    if (requestId != null && !requestId.isBlank()) {
      return requestId;
    }
    HttpServletRequest request = currentRequest();
    if (request == null) {
      return null;
    }
    Object attr = request.getAttribute(REQUEST_ID_ATTRIBUTE);
    if (attr instanceof String value && !value.isBlank()) {
      return value;
    }
    String header = request.getHeader(REQUEST_ID_HEADER);
    if (header == null || header.isBlank()) {
      return null;
    }
    return header.trim();
  }

  public static String currentPath() {
    HttpServletRequest request = currentRequest();
    if (request == null) {
      return null;
    }
    String uri = request.getRequestURI();
    if (uri == null || uri.isBlank()) {
      return null;
    }
    return uri;
  }

  public static HttpServletRequest currentRequest() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
      return null;
    }
    return servletAttributes.getRequest();
  }

  public static Optional<String> optionalRequestId() {
    return Optional.ofNullable(currentRequestId());
  }
}
