package elexvx.admin.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import elexvx.admin.dto.LoginRequest;
import elexvx.admin.exception.LoginCaptchaException;
import elexvx.admin.exception.LoginCredentialException;
import elexvx.admin.service.AuthService;
import elexvx.admin.service.SecurityRateLimitService;
import elexvx.admin.vo.LoginResponse;
import org.junit.jupiter.api.Test;

public class AuthControllerTest {
  @Test
  void loginOnlyRecordsFailuresForCredentialErrors() {
    AuthService authService = mock(AuthService.class);
    SecurityRateLimitService rateLimitService = mock(SecurityRateLimitService.class);
    AuthController controller = new AuthController(authService, rateLimitService);

    LoginRequest req = new LoginRequest();
    req.setAccount("admin");
    req.setPassword("bad");
    when(authService.login(req)).thenThrow(LoginCredentialException.passwordMismatch("admin"));

    assertThrows(LoginCredentialException.class, () -> controller.login(req));

    verify(rateLimitService).checkLoginAttempt("admin");
    verify(rateLimitService).recordLoginFailure("admin");
    verify(rateLimitService, never()).clearLoginFailures("admin");
  }

  @Test
  void loginDoesNotRecordCaptchaFailuresAsPasswordFailures() {
    AuthService authService = mock(AuthService.class);
    SecurityRateLimitService rateLimitService = mock(SecurityRateLimitService.class);
    AuthController controller = new AuthController(authService, rateLimitService);

    LoginRequest req = new LoginRequest();
    req.setAccount("admin");
    req.setPassword("123456");
    when(authService.login(req)).thenThrow(LoginCaptchaException.missing());

    assertThrows(LoginCaptchaException.class, () -> controller.login(req));

    verify(rateLimitService).checkLoginAttempt("admin");
    verify(rateLimitService, never()).recordLoginFailure("admin");
    verify(rateLimitService, never()).clearLoginFailures("admin");
  }

  @Test
  void loginClearsFailuresAfterSuccess() {
    AuthService authService = mock(AuthService.class);
    SecurityRateLimitService rateLimitService = mock(SecurityRateLimitService.class);
    AuthController controller = new AuthController(authService, rateLimitService);

    LoginRequest req = new LoginRequest();
    req.setAccount("admin");
    req.setPassword("123456");
    when(authService.login(req)).thenReturn(new LoginResponse());

    controller.login(req);

    verify(rateLimitService).checkLoginAttempt("admin");
    verify(rateLimitService).clearLoginFailures("admin");
    verify(rateLimitService, never()).recordLoginFailure("admin");
  }
}
