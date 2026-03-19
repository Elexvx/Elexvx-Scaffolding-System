package top.elexvx.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SensitiveEndpointHeaderFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    filterChain.doFilter(request, response);
    String uri = request.getRequestURI();
    if (uri == null) return;
    String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
    String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
    if (!path.startsWith("/")) path = "/" + path;
    if (path.startsWith("/files/") || path.startsWith("/auth") || path.startsWith("/system/file")) {
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
    }
  }
}
