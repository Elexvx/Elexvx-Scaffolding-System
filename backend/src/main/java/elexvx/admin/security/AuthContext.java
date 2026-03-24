package elexvx.admin.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {
  public long requireUserId() {
    AuthPrincipal principal = getPrincipal();
    if (principal == null) {
      throw new AuthenticationCredentialsNotFoundException("未登录或登录已失效，请重新登录");
    }
    return principal.getUserId();
  }

  public String requireToken() {
    AuthPrincipal principal = getPrincipal();
    if (principal == null || principal.getToken() == null || principal.getToken().isBlank()) {
      throw new AuthenticationCredentialsNotFoundException("未登录或登录已失效，请重新登录");
    }
    return principal.getToken();
  }

  public long getUserIdOrDefault(long defaultValue) {
    AuthPrincipal principal = getPrincipal();
    if (principal == null) return defaultValue;
    return principal.getUserId();
  }

  public String getToken() {
    AuthPrincipal principal = getPrincipal();
    return principal == null ? null : principal.getToken();
  }

  public boolean isAuthenticated() {
    return getPrincipal() != null;
  }

  private AuthPrincipal getPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof AuthPrincipal authPrincipal) {
      return authPrincipal;
    }
    return null;
  }
}
