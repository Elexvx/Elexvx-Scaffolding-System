package elexvx.admin.controller;

import elexvx.admin.dto.LoginRequest;
import elexvx.admin.dto.LoginConfirmRequest;
import elexvx.admin.dto.RegisterRequest;
import elexvx.admin.dto.SmsLoginRequest;
import elexvx.admin.dto.SmsSendRequest;
import elexvx.admin.dto.EmailLoginRequest;
import elexvx.admin.dto.EmailSendRequest;
import elexvx.admin.dto.UserProfileUpdateRequest;
import elexvx.admin.dto.ChangePasswordRequest;
import elexvx.admin.dto.ForgotPasswordRequest;
import elexvx.admin.dto.RoleSwitchRequest;
import elexvx.admin.annotation.RepeatSubmit;
import elexvx.admin.exception.LoginCredentialException;
import elexvx.admin.service.AuthService;
import elexvx.admin.service.SecurityRateLimitService;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.LoginResponse;
import elexvx.admin.vo.SmsSendResponse;
import elexvx.admin.vo.UserInfoResponse;
import elexvx.admin.vo.UserProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;
  private final SecurityRateLimitService rateLimitService;

  public AuthController(AuthService authService, SecurityRateLimitService rateLimitService) {
    this.authService = authService;
    this.rateLimitService = rateLimitService;
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
    rateLimitService.checkLoginAttempt(req.getAccount());
    try {
      LoginResponse response = authService.login(req);
      rateLimitService.clearLoginFailures(req.getAccount());
      return ApiResponse.success(response);
    } catch (LoginCredentialException ex) {
      rateLimitService.recordLoginFailure(req.getAccount());
      throw ex;
    }
  }

  @PostMapping("/login/sms")
  public ApiResponse<LoginResponse> loginBySms(@RequestBody @Valid SmsLoginRequest req) {
    return ApiResponse.success(authService.loginBySms(req));
  }

  @PostMapping("/login/email")
  public ApiResponse<LoginResponse> loginByEmail(@RequestBody @Valid EmailLoginRequest req) {
    return ApiResponse.success(authService.loginByEmail(req));
  }

  @PostMapping("/sms/send")
  @RepeatSubmit
  public ApiResponse<SmsSendResponse> sendSms(@RequestBody @Valid SmsSendRequest req) {
    rateLimitService.checkVerificationSendQuota(req.getPhone(), "sms");
    return ApiResponse.success(authService.sendSmsCode(req));
  }

  @PostMapping("/email/send")
  @RepeatSubmit
  public ApiResponse<SmsSendResponse> sendEmail(@RequestBody @Valid EmailSendRequest req) {
    rateLimitService.checkVerificationSendQuota(req.getEmail(), "email");
    return ApiResponse.success(authService.sendEmailCode(req));
  }

  @PostMapping("/login/confirm")
  @RepeatSubmit
  public ApiResponse<LoginResponse> confirmLogin(@RequestBody @Valid LoginConfirmRequest req) {
    return ApiResponse.success(authService.confirmLogin(req.getRequestId(), req.getRequestKey()));
  }

  @PostMapping("/register")
  @RepeatSubmit
  public ApiResponse<Boolean> register(@RequestBody @Valid RegisterRequest req) {
    return ApiResponse.success(authService.register(req));
  }

  @GetMapping("/user")
  public ApiResponse<UserInfoResponse> userInfo() {
    return ApiResponse.success(authService.currentUserInfo());
  }

  @GetMapping("/profile")
  public ApiResponse<UserProfileResponse> profile() {
    return ApiResponse.success(authService.currentUserProfile());
  }

  @PutMapping("/profile")
  @RepeatSubmit
  public ApiResponse<UserProfileResponse> updateProfile(@RequestBody @Valid UserProfileUpdateRequest req) {
    return ApiResponse.success(authService.updateCurrentUserProfile(req));
  }

  @PostMapping("/password")
  @RepeatSubmit
  public ApiResponse<Boolean> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
    return ApiResponse.success(authService.changePassword(req));
  }

  @PostMapping("/password/reset")
  @RepeatSubmit
  public ApiResponse<Boolean> resetPassword(@RequestBody @Valid ForgotPasswordRequest req) {
    return ApiResponse.success(authService.resetPassword(req));
  }

  @PostMapping("/logout")
  @RepeatSubmit
  public ApiResponse<Boolean> logout() {
    return ApiResponse.success(authService.logout());
  }

  @PostMapping("/assume-role")
  @RepeatSubmit
  public ApiResponse<UserInfoResponse> switchRole(@RequestBody @Valid RoleSwitchRequest req) {
    return ApiResponse.success(authService.switchRoles(req));
  }

  @GetMapping("/assume-role/options")
  public ApiResponse<List<String>> assumeRoleOptions() {
    return ApiResponse.success(authService.listAllRoleNames());
  }
}
