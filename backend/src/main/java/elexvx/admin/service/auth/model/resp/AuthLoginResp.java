package elexvx.admin.service.auth.model.resp;

import elexvx.admin.vo.LoginResponse;

public class AuthLoginResp {
  private String token;
  private Long expiresIn;
  private String pendingRequestId;
  private String pendingRequestKey;

  public static AuthLoginResp success(String token, long expiresIn) {
    AuthLoginResp resp = new AuthLoginResp();
    resp.setToken(token);
    resp.setExpiresIn(expiresIn);
    return resp;
  }

  public static AuthLoginResp pending(String requestId, String requestKey) {
    AuthLoginResp resp = new AuthLoginResp();
    resp.setPendingRequestId(requestId);
    resp.setPendingRequestKey(requestKey);
    return resp;
  }

  public LoginResponse toLoginResponse() {
    if (pendingRequestId != null && pendingRequestKey != null) {
      return LoginResponse.pending(pendingRequestId, pendingRequestKey);
    }
    return LoginResponse.success(token, expiresIn == null ? 0L : expiresIn);
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getPendingRequestId() {
    return pendingRequestId;
  }

  public void setPendingRequestId(String pendingRequestId) {
    this.pendingRequestId = pendingRequestId;
  }

  public String getPendingRequestKey() {
    return pendingRequestKey;
  }

  public void setPendingRequestKey(String pendingRequestKey) {
    this.pendingRequestKey = pendingRequestKey;
  }
}
