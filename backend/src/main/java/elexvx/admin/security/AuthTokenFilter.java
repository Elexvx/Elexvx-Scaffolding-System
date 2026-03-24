package elexvx.admin.security;

import elexvx.admin.service.AuthTokenService;
import elexvx.admin.service.ConcurrentLoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
/**
 * Token 鉴权过滤器。
 *
 * <p>职责：
 * <ul>
 *   <li>从请求头/参数解析 Token。</li>
 *   <li>从 {@link AuthTokenService} 读取服务端会话，并写入 {@code SecurityContext}。</li>
 *   <li>按访问频率刷新会话（touch），避免每次请求都写存储造成压力。</li>
 * </ul>
 *
 * <p>安全注意：允许通过 URL 参数传 Token 是一种折中方案，默认应关闭，仅在确有需要的端点开启。
 */
public class AuthTokenFilter extends OncePerRequestFilter {
  private final AuthTokenService tokenService;
  private final ConcurrentLoginService concurrentLoginService;

  public AuthTokenFilter(
    AuthTokenService tokenService,
    ConcurrentLoginService concurrentLoginService
  ) {
    this.tokenService = tokenService;
    this.concurrentLoginService = concurrentLoginService;
  }

  @Override
  /**
   * 构建 Spring Security 认证信息。
   *
   * <p>当请求已存在认证信息时不会重复解析；当 Token 无效或会话不存在时保持匿名继续向下游传递，由安全配置拦截。
   */
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);
    if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
      AuthSession session = tokenService.getSession(token);
      if (session != null) {
        touchSession(token, session);
        AuthPrincipal principal = new AuthPrincipal(session.getUserId(), token);
        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, token, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  /**
   * 以固定间隔刷新一次会话访问时间，减少频繁写存储。
   *
   * <p>用于“滑动过期/在线状态”类能力；间隔过小会增加 Redis/DB 写入压力，间隔过大会降低在线状态实时性。
   */
  private void touchSession(String token, AuthSession session) {
    long now = System.currentTimeMillis();
    Object lastAccessObj = session.getAttributes().get("lastAccessTime");
    Long lastAccess = lastAccessObj instanceof Number ? ((Number) lastAccessObj).longValue() : null;
    if (lastAccess == null || now - lastAccess > 30_000L) {
      session.getAttributes().put("lastAccessTime", now);
      tokenService.updateSession(token, session);
    }
  }

  /**
   * 解析 Token。
   *
   * <p>优先读取 {@code Authorization} 请求头，支持 {@code Bearer <token>} 与直接传值两种形式。
   * 当系统配置允许（或特定 SSE 端点需要）时，退化为从 URL 参数读取 Token。
   */
  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(header)) {
      return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
    String uri = request.getRequestURI();
    boolean isConcurrentStream = uri != null && uri.endsWith("/auth/concurrent/stream");
    if (isConcurrentStream) {
      String ticket = request.getParameter("ticket");
      String token = concurrentLoginService.consumeConcurrentStreamToken(ticket);
      if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
        token = token.substring(7);
      }
      return StringUtils.hasText(token) ? token : null;
    }
    // 仅保留并发登录 SSE 短期 ticket 场景，其他接口不再接受 URL query token。
    return null;
  }
}
