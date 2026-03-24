package elexvx.admin.service.auth;

import elexvx.admin.entity.UserEntity;
import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.exception.LoginAccountDisabledException;
import elexvx.admin.exception.LoginCaptchaException;
import elexvx.admin.exception.LoginCredentialException;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.security.AuthContext;
import elexvx.admin.security.AuthSession;
import elexvx.admin.service.AuthTokenService;
import elexvx.admin.service.AuthValidationService;
import elexvx.admin.service.CaptchaService;
import elexvx.admin.service.ConcurrentLoginService;
import elexvx.admin.service.EmailCodeService;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.PermissionFacade;
import elexvx.admin.service.SecuritySettingService;
import elexvx.admin.service.SmsCodeService;
import elexvx.admin.service.VerificationSettingService;
import elexvx.admin.service.auth.model.command.AuthLoginCommand;
import elexvx.admin.service.auth.model.query.AuthUserQuery;
import elexvx.admin.service.auth.model.req.AuthLoginReq;
import elexvx.admin.service.auth.model.resp.AuthLoginResp;
import elexvx.admin.verification.VerificationProvider;
import elexvx.admin.verification.VerificationProviderRegistry;
import elexvx.admin.vo.SmsSendResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AuthLoginService {
  private static final Logger log = LoggerFactory.getLogger(AuthLoginService.class);
  private final UserMapper userMapper;
  private final CaptchaService captchaService;
  private final SecuritySettingService securitySettingService;
  private final VerificationSettingService verificationSettingService;
  private final VerificationProviderRegistry verificationProviderRegistry;
  private final SmsCodeService smsCodeService;
  private final EmailCodeService emailCodeService;
  private final AuthValidationService authValidationService;
  private final ConcurrentLoginOrchestrator concurrentLoginOrchestrator;
  private final ConcurrentLoginService concurrentLoginService;
  private final AuthTokenService authTokenService;
  private final OperationLogService operationLogService;
  private final AuthContext authContext;
  private final PermissionFacade permissionFacade;
  private final HttpServletRequest request;
  private final boolean captchaFeatureEnabled;

  public AuthLoginService(
    UserMapper userMapper,
    CaptchaService captchaService,
    SecuritySettingService securitySettingService,
    VerificationSettingService verificationSettingService,
    VerificationProviderRegistry verificationProviderRegistry,
    SmsCodeService smsCodeService,
    EmailCodeService emailCodeService,
    AuthValidationService authValidationService,
    ConcurrentLoginOrchestrator concurrentLoginOrchestrator,
    ConcurrentLoginService concurrentLoginService,
    AuthTokenService authTokenService,
    OperationLogService operationLogService,
    AuthContext authContext,
    PermissionFacade permissionFacade,
    HttpServletRequest request,
    @Value("${elexvx.security.captcha.enabled:true}") boolean captchaFeatureEnabled
  ) {
    this.userMapper = userMapper;
    this.captchaService = captchaService;
    this.securitySettingService = securitySettingService;
    this.verificationSettingService = verificationSettingService;
    this.verificationProviderRegistry = verificationProviderRegistry;
    this.smsCodeService = smsCodeService;
    this.emailCodeService = emailCodeService;
    this.authValidationService = authValidationService;
    this.concurrentLoginOrchestrator = concurrentLoginOrchestrator;
    this.concurrentLoginService = concurrentLoginService;
    this.authTokenService = authTokenService;
    this.operationLogService = operationLogService;
    this.authContext = authContext;
    this.permissionFacade = permissionFacade;
    this.request = request;
    this.captchaFeatureEnabled = captchaFeatureEnabled;
  }

  public AuthLoginResp loginByAccount(AuthLoginReq req) {
    String account = authValidationService.normalizeAccount(req.getAccount());
    String captchaCode = authValidationService.normalizeCode(req.getCaptchaCode());
    boolean captchaEnabled = captchaFeatureEnabled && Boolean.TRUE.equals(securitySettingService.getOrCreate().getCaptchaEnabled());
    log.info("LOGIN_ENTER account={} captchaEnabled={} clientIp={}", account, captchaEnabled, getClientIp());
    if (captchaEnabled) {
      if (req.getCaptchaId() == null || req.getCaptchaId().isBlank() || captchaCode.isBlank()) {
        log.warn("LOGIN_CAPTCHA_MISSING account={} clientIp={}", account, getClientIp());
        throw LoginCaptchaException.missing();
      }
      boolean ok = captchaService.verify(req.getCaptchaId(), captchaCode);
      if (!ok) {
        log.warn("LOGIN_CAPTCHA_INVALID account={} clientIp={}", account, getClientIp());
        throw LoginCaptchaException.invalidOrExpired();
      }
    }
    UserEntity user = userMapper.selectByAccount(account);
    if (user == null || !Objects.equals(user.getAccount(), account)) {
      log.warn("LOGIN_CREDENTIAL_INVALID reason=ACCOUNT_NOT_FOUND account={} clientIp={}", account, getClientIp());
      throw LoginCredentialException.accountNotFound(account);
    }
    if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
      log.warn("LOGIN_CREDENTIAL_INVALID reason=PASSWORD_MISMATCH account={} clientIp={}", account, getClientIp());
      throw LoginCredentialException.passwordMismatch(account);
    }
    if (user.getStatus() != null && user.getStatus() == 0) {
      log.warn("LOGIN_ACCOUNT_DISABLED account={} clientIp={}", account, getClientIp());
      throw LoginAccountDisabledException.disabled(account);
    }
    AuthLoginCommand command = new AuthLoginCommand();
    command.setUser(user);
    command.setForce(req.getForce());
    return completeLogin(command);
  }

  public AuthLoginResp loginBySms(AuthLoginReq req) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    authValidationService.ensureSmsEnabled(setting);
    String phone = authValidationService.normalizePhone(req.getPhone());
    String code = authValidationService.normalizeCode(req.getCode());
    if (!smsCodeService.verify(phone, code)) {
      throw badRequest("验证码错误");
    }
    UserEntity user = findUser(new AuthUserQuery(), phone, null);
    if (user == null) {
      throw badRequest("手机号未注册");
    }
    AuthLoginCommand command = new AuthLoginCommand();
    command.setUser(user);
    command.setForce(req.getForce());
    return completeLogin(command);
  }

  public AuthLoginResp loginByEmail(AuthLoginReq req) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    authValidationService.ensureEmailEnabled(setting);
    String email = authValidationService.normalizeEmail(req.getEmail());
    String code = authValidationService.normalizeCode(req.getCode());
    if (!emailCodeService.verify(email, code)) {
      throw badRequest("验证码错误");
    }
    UserEntity user = findUser(new AuthUserQuery(), null, email);
    if (user == null) {
      throw badRequest("邮箱未注册");
    }
    AuthLoginCommand command = new AuthLoginCommand();
    command.setUser(user);
    command.setForce(req.getForce());
    return completeLogin(command);
  }

  public SmsSendResponse sendSmsCode(String phoneValue, String providerName) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    authValidationService.ensureSmsEnabled(setting);
    authValidationService.ensureSmsConfig(setting);
    String phone = authValidationService.normalizePhone(phoneValue);
    UserEntity user = findUser(new AuthUserQuery(), phone, null);
    if (user == null) {
      throw badRequest("手机号未注册");
    }
    VerificationProvider provider = verificationProviderRegistry.require("sms");
    try {
      provider.sendCode(setting, phone, getClientIp(), providerName);
    } catch (Exception ex) {
      throw badRequest("短信验证码发送失败，请稍后重试");
    }
    return new SmsSendResponse(provider.getExpiresInSeconds());
  }

  public SmsSendResponse sendEmailCode(String emailValue) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    authValidationService.ensureEmailEnabled(setting);
    authValidationService.ensureEmailConfig(setting);
    String email = authValidationService.normalizeEmail(emailValue);
    UserEntity user = findUser(new AuthUserQuery(), null, email);
    if (user == null) {
      throw badRequest("邮箱未注册");
    }
    VerificationProvider provider = verificationProviderRegistry.require("email");
    try {
      provider.sendCode(setting, email, getClientIp(), null);
    } catch (Exception ex) {
      throw badRequest("邮箱验证码发送失败，请稍后重试");
    }
    return new SmsSendResponse(provider.getExpiresInSeconds());
  }

  public AuthLoginResp confirmLogin(String requestId, String requestKey) {
    ConcurrentLoginService.PendingLogin pending = concurrentLoginService.consumeApproved(requestId, requestKey);
    UserEntity user = Optional.ofNullable(userMapper.selectById(pending.getLoginId())).orElseThrow(() -> badRequest("User not found"));
    ensureUserGuid(user);
    saveUser(user);
    long expiresIn = concurrentLoginOrchestrator.resolveTokenTimeoutSeconds();
    ConcurrentLoginOrchestrator.DeviceSnapshot snapshot = concurrentLoginOrchestrator.toSnapshot(pending);
    AuthSession session = concurrentLoginOrchestrator.buildSession(user, snapshot);
    String token = authTokenService.createToken(user.getId(), session, expiresIn);
    operationLogService.logLogin(user, snapshot.deviceModel(), snapshot.os(), snapshot.browser(), snapshot.ipAddress());
    log.info("LOGIN_CONFIRM_SUCCESS account={} requestId={} clientIp={} expiresIn={}", user.getAccount(), requestId, snapshot.ipAddress(), expiresIn);
    return AuthLoginResp.success(token, expiresIn);
  }

  public boolean logout() {
    if (authContext.isAuthenticated()) {
      long userId = authContext.requireUserId();
      permissionFacade.clearAssumedRoles(userId);
      String token = authContext.getToken();
      if (token != null) {
        authTokenService.removeToken(token);
      }
    }
    return true;
  }

  private AuthLoginResp completeLogin(AuthLoginCommand command) {
    UserEntity user = command.getUser();
    ensureUserActive(user);
    ConcurrentLoginOrchestrator.DeviceSnapshot snapshot = concurrentLoginOrchestrator.buildDeviceSnapshot(request);
    ConcurrentLoginOrchestrator.LoginDecision decision = concurrentLoginOrchestrator.resolveLoginDecision(
      user,
      command.getForce(),
      snapshot
    );
    if (decision.isPending()) {
      log.info(
        "LOGIN_PENDING_CONFIRM account={} requestId={} clientIp={}",
        user.getAccount(),
        decision.getRequestId(),
        snapshot.ipAddress()
      );
      return AuthLoginResp.pending(decision.getRequestId(), decision.getRequestKey());
    }
    ensureUserGuid(user);
    saveUser(user);
    long expiresIn = concurrentLoginOrchestrator.resolveTokenTimeoutSeconds();
    AuthSession session = concurrentLoginOrchestrator.buildSession(user, snapshot);
    String token = authTokenService.createToken(user.getId(), session, expiresIn);
    operationLogService.logLogin(user, snapshot.deviceModel(), snapshot.os(), snapshot.browser(), snapshot.ipAddress());
    log.info("LOGIN_SUCCESS account={} clientIp={} expiresIn={}", user.getAccount(), snapshot.ipAddress(), expiresIn);
    return AuthLoginResp.success(token, expiresIn);
  }

  private UserEntity findUser(AuthUserQuery query, String phone, String email) {
    query.setPhone(phone);
    query.setEmail(email);
    if (query.getPhone() != null) {
      String normalized = authValidationService.normalizePhone(query.getPhone());
      if (normalized.isBlank()) {
        return null;
      }
      String[] candidates = new String[] {normalized, "+86" + normalized, "+86 " + normalized, "86" + normalized, "86 " + normalized};
      for (String candidate : candidates) {
        UserEntity user = userMapper.selectByMobile(candidate);
        if (user != null) {
          return user;
        }
        user = userMapper.selectByPhone(candidate);
        if (user != null) {
          return user;
        }
      }
      return null;
    }
    if (query.getEmail() != null && !query.getEmail().isBlank()) {
      return userMapper.selectByEmail(query.getEmail());
    }
    return null;
  }

  private String getClientIp() {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }
    return ip == null || ip.isBlank() ? "unknown" : ip;
  }

  private void ensureUserActive(UserEntity user) {
    if (user.getStatus() != null && user.getStatus() == 0) {
      throw badRequest("账号已禁用");
    }
  }

  private void ensureUserGuid(UserEntity user) {
    if (user.getGuid() == null || user.getGuid().isBlank()) {
      user.setGuid(java.util.UUID.randomUUID().toString());
    }
  }

  private UserEntity saveUser(UserEntity user) {
    if (user.getId() == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
    return user;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
}
