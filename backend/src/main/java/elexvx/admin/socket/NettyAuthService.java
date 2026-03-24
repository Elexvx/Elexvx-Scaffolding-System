package elexvx.admin.socket;

import elexvx.admin.security.AuthSession;
import elexvx.admin.service.AuthTokenService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
/**
 * Socket 鉴权适配服务。
 *
 * <p>复用 HTTP 侧的 Token 会话存储（{@link AuthTokenService}），将 token 映射为 userId。
 */
public class NettyAuthService {
  private final AuthTokenService authTokenService;

  public NettyAuthService(AuthTokenService authTokenService) {
    this.authTokenService = authTokenService;
  }

  public Optional<Long> authenticate(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    AuthSession session = authTokenService.getSession(token);
    if (session == null) {
      return Optional.empty();
    }
    return Optional.of(session.getUserId());
  }
}
