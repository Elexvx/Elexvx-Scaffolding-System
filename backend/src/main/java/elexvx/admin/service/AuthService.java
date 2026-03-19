package elexvx.admin.service;

import elexvx.admin.dto.ChangePasswordRequest;
import elexvx.admin.dto.EmailLoginRequest;
import elexvx.admin.dto.EmailSendRequest;
import elexvx.admin.dto.ForgotPasswordRequest;
import elexvx.admin.dto.LoginRequest;
import elexvx.admin.dto.RegisterRequest;
import elexvx.admin.dto.RoleSwitchRequest;
import elexvx.admin.dto.SmsLoginRequest;
import elexvx.admin.dto.SmsSendRequest;
import elexvx.admin.dto.UserProfileUpdateRequest;
import elexvx.admin.service.auth.AuthLoginService;
import elexvx.admin.service.auth.AuthRegisterService;
import elexvx.admin.service.auth.CurrentUserProfileService;
import elexvx.admin.service.auth.PasswordResetService;
import elexvx.admin.service.auth.model.req.AuthLoginReq;
import elexvx.admin.service.auth.model.req.AuthRegisterReq;
import elexvx.admin.service.auth.model.req.ChangePasswordReq;
import elexvx.admin.service.auth.model.req.PasswordResetReq;
import elexvx.admin.vo.LoginResponse;
import elexvx.admin.vo.SmsSendResponse;
import elexvx.admin.vo.UserInfoResponse;
import elexvx.admin.vo.UserProfileResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final AuthLoginService authLoginService;
  private final AuthRegisterService authRegisterService;
  private final PasswordResetService passwordResetService;
  private final CurrentUserProfileService currentUserProfileService;

  public AuthService(
    AuthLoginService authLoginService,
    AuthRegisterService authRegisterService,
    PasswordResetService passwordResetService,
    CurrentUserProfileService currentUserProfileService
  ) {
    this.authLoginService = authLoginService;
    this.authRegisterService = authRegisterService;
    this.passwordResetService = passwordResetService;
    this.currentUserProfileService = currentUserProfileService;
  }

  public LoginResponse login(LoginRequest req) {
    AuthLoginReq authReq = new AuthLoginReq();
    authReq.setAccount(req.getAccount());
    authReq.setPassword(req.getPassword());
    authReq.setCaptchaId(req.getCaptchaId());
    authReq.setCaptchaCode(req.getCaptchaCode());
    authReq.setForce(req.getForce());
    return authLoginService.loginByAccount(authReq).toLoginResponse();
  }

  public SmsSendResponse sendSmsCode(SmsSendRequest req) {
    return authLoginService.sendSmsCode(req.getPhone(), req.getProvider());
  }

  public LoginResponse loginBySms(SmsLoginRequest req) {
    AuthLoginReq authReq = new AuthLoginReq();
    authReq.setPhone(req.getPhone());
    authReq.setCode(req.getCode());
    authReq.setForce(req.getForce());
    return authLoginService.loginBySms(authReq).toLoginResponse();
  }

  public SmsSendResponse sendEmailCode(EmailSendRequest req) {
    return authLoginService.sendEmailCode(req.getEmail());
  }

  public LoginResponse loginByEmail(EmailLoginRequest req) {
    AuthLoginReq authReq = new AuthLoginReq();
    authReq.setEmail(req.getEmail());
    authReq.setCode(req.getCode());
    authReq.setForce(req.getForce());
    return authLoginService.loginByEmail(authReq).toLoginResponse();
  }

  public LoginResponse confirmLogin(String requestId, String requestKey) {
    return authLoginService.confirmLogin(requestId, requestKey).toLoginResponse();
  }

  public UserInfoResponse currentUserInfo() {
    return currentUserProfileService.currentUserInfo();
  }

  public UserProfileResponse currentUserProfile() {
    return currentUserProfileService.currentUserProfile();
  }

  public UserProfileResponse updateCurrentUserProfile(UserProfileUpdateRequest req) {
    return currentUserProfileService.updateCurrentUserProfile(req);
  }

  public boolean changePassword(ChangePasswordRequest req) {
    ChangePasswordReq changeReq = new ChangePasswordReq();
    changeReq.setOldPassword(req.getOldPassword());
    changeReq.setNewPassword(req.getNewPassword());
    changeReq.setConfirmPassword(req.getConfirmPassword());
    return passwordResetService.changePassword(changeReq);
  }

  public boolean resetPassword(ForgotPasswordRequest req) {
    PasswordResetReq resetReq = new PasswordResetReq();
    resetReq.setAccount(req.getAccount());
    resetReq.setPhone(req.getPhone());
    resetReq.setCode(req.getCode());
    resetReq.setNewPassword(req.getNewPassword());
    resetReq.setConfirmPassword(req.getConfirmPassword());
    return passwordResetService.resetPassword(resetReq);
  }

  public UserInfoResponse switchRoles(RoleSwitchRequest req) {
    return currentUserProfileService.switchRoles(req);
  }

  public List<String> listAllRoleNames() {
    return currentUserProfileService.listAllRoleNames();
  }

  public boolean logout() {
    return authLoginService.logout();
  }

  public boolean register(RegisterRequest req) {
    AuthRegisterReq registerReq = new AuthRegisterReq();
    registerReq.setAccount(req.getAccount());
    registerReq.setPassword(req.getPassword());
    registerReq.setConfirmPassword(req.getConfirmPassword());
    registerReq.setCaptchaId(req.getCaptchaId());
    registerReq.setCaptchaCode(req.getCaptchaCode());
    return authRegisterService.register(registerReq);
  }
}
