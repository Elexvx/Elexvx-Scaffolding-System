package elexvx.admin.service.auth;

import elexvx.admin.entity.UserEntity;
import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.security.AuthContext;
import elexvx.admin.security.AuthSession;
import elexvx.admin.service.AuthTokenService;
import elexvx.admin.service.AuthValidationService;
import elexvx.admin.service.EmailCodeService;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.PasswordPolicyService;
import elexvx.admin.service.SmsCodeService;
import elexvx.admin.service.VerificationSettingService;
import elexvx.admin.service.auth.model.req.ChangePasswordReq;
import elexvx.admin.service.auth.model.req.PasswordResetReq;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
  private final UserMapper userMapper;
  private final AuthContext authContext;
  private final PasswordPolicyService passwordPolicyService;
  private final AuthValidationService authValidationService;
  private final VerificationSettingService verificationSettingService;
  private final SmsCodeService smsCodeService;
  private final EmailCodeService emailCodeService;
  private final OperationLogService operationLogService;
  private final AuthTokenService authTokenService;

  public PasswordResetService(
    UserMapper userMapper,
    AuthContext authContext,
    PasswordPolicyService passwordPolicyService,
    AuthValidationService authValidationService,
    VerificationSettingService verificationSettingService,
    SmsCodeService smsCodeService,
    EmailCodeService emailCodeService,
    OperationLogService operationLogService,
    AuthTokenService authTokenService
  ) {
    this.userMapper = userMapper;
    this.authContext = authContext;
    this.passwordPolicyService = passwordPolicyService;
    this.authValidationService = authValidationService;
    this.verificationSettingService = verificationSettingService;
    this.smsCodeService = smsCodeService;
    this.emailCodeService = emailCodeService;
    this.operationLogService = operationLogService;
    this.authTokenService = authTokenService;
  }

  @Transactional
  public boolean changePassword(ChangePasswordReq req) {
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
      throw badRequest("两次新密码不一致");
    }
    passwordPolicyService.validate(req.getNewPassword());
    long userId = authContext.requireUserId();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId)).orElseThrow(() -> badRequest("用户不存在"));
    if (!BCrypt.checkpw(req.getOldPassword(), user.getPasswordHash())) {
      throw badRequest("当前密码不正确");
    }
    user.setPasswordHash(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
    ensureUserGuid(user);
    saveUser(user);
    operationLogService.log("UPDATE", "password", "user changed password");
    invalidateCurrentToken();
    return true;
  }

  @Transactional
  public boolean resetPassword(PasswordResetReq req) {
    String account = authValidationService.normalizeAccount(req.getAccount());
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
      throw badRequest("两次新密码不一致");
    }
    passwordPolicyService.validate(req.getNewPassword());
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    authValidationService.ensureSmsEnabled(setting);
    String phone = authValidationService.normalizePhone(req.getPhone());
    String code = authValidationService.normalizeCode(req.getCode());
    if (!smsCodeService.verify(phone, code)) {
      throw badRequest("验证码错误或已过期");
    }
    UserEntity user = Optional.ofNullable(userMapper.selectByAccount(account)).orElseThrow(() -> badRequest("账号不存在"));
    String normalizedPhone = authValidationService.normalizePhone(user.getMobile());
    if (normalizedPhone.isBlank()) {
      throw badRequest("手机号未注册");
    }
    if (!phone.equals(normalizedPhone)) {
      throw badRequest("手机号与账号不匹配");
    }
    user.setPasswordHash(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
    ensureUserGuid(user);
    saveUser(user);
    operationLogService.log("UPDATE", "password-reset", "user reset password via sms");
    return true;
  }

  public boolean verifyEmailCode(String email, String code) {
    String normalizedEmail = authValidationService.normalizeEmail(email);
    String normalizedCode = authValidationService.normalizeCode(code);
    return emailCodeService.verify(normalizedEmail, normalizedCode);
  }

  private void invalidateCurrentToken() {
    String token = authContext.getToken();
    if (token == null) {
      return;
    }
    AuthSession session = authTokenService.getSession(token);
    if (session == null) {
      return;
    }
    authTokenService.removeToken(token);
  }

  private void ensureUserGuid(UserEntity user) {
    if (user.getGuid() == null || user.getGuid().isBlank()) {
      user.setGuid(UUID.randomUUID().toString());
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
