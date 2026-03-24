package elexvx.admin.security;

public class AuthPrincipal {
  private final long userId;
  private final String token;

  public AuthPrincipal(long userId, String token) {
    this.userId = userId;
    this.token = token;
  }

  public long getUserId() {
    return userId;
  }

  public String getToken() {
    return token;
  }
}
