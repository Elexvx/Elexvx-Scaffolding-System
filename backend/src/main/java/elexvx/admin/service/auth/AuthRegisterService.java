package elexvx.admin.service.auth;

import elexvx.admin.dao.AuthQueryDao;
import elexvx.admin.entity.UserEntity;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.service.AuthValidationService;
import elexvx.admin.service.CaptchaService;
import elexvx.admin.service.PasswordPolicyService;
import elexvx.admin.service.SecuritySettingService;
import elexvx.admin.service.auth.model.req.AuthRegisterReq;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthRegisterService {
  private static final String MSG_ACCOUNT_REQUIRED = "请输入账号";
  private static final String MSG_ACCOUNT_EXISTS = "账号已存在，请更换后重试";
  private static final String MSG_REGISTER_RETRY = "注册失败，请稍后重试";

  private final UserMapper userMapper;
  private final SecuritySettingService securitySettingService;
  private final CaptchaService captchaService;
  private final PasswordPolicyService passwordPolicyService;
  private final AuthValidationService authValidationService;
  private final AuthQueryDao authQueryDao;

  public AuthRegisterService(
    UserMapper userMapper,
    SecuritySettingService securitySettingService,
    CaptchaService captchaService,
    PasswordPolicyService passwordPolicyService,
    AuthValidationService authValidationService,
    AuthQueryDao authQueryDao
  ) {
    this.userMapper = userMapper;
    this.securitySettingService = securitySettingService;
    this.captchaService = captchaService;
    this.passwordPolicyService = passwordPolicyService;
    this.authValidationService = authValidationService;
    this.authQueryDao = authQueryDao;
  }

  @Transactional
  public boolean register(AuthRegisterReq req) {
    Boolean captchaEnabled = securitySettingService.getOrCreate().getCaptchaEnabled();
    if (Boolean.TRUE.equals(captchaEnabled)) {
      if (req.getCaptchaId() == null || req.getCaptchaId().isBlank() || req.getCaptchaCode() == null || req.getCaptchaCode().isBlank()) {
        throw badRequest("请输入验证码");
      }
      String captchaCode = authValidationService.normalizeCode(req.getCaptchaCode());
      if (!captchaService.verify(req.getCaptchaId(), captchaCode)) {
        throw badRequest("验证码错误或已过期");
      }
    }
    if (!safeEquals(req.getPassword(), req.getConfirmPassword())) {
      throw badRequest("两次密码不一致");
    }
    passwordPolicyService.validate(req.getPassword());
    String account = authValidationService.normalizeAccount(req.getAccount());
    if (account.isBlank()) {
      throw badRequest(MSG_ACCOUNT_REQUIRED);
    }
    if (userMapper.countByAccount(account) > 0) {
      throw badRequest(MSG_ACCOUNT_EXISTS);
    }
    UserEntity entity = new UserEntity();
    entity.setAccount(account);
    entity.setGuid(UUID.randomUUID().toString());
    entity.setName(account);
    entity.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
    entity.setStatus(1);
    try {
      saveUser(entity);
    } catch (DataIntegrityViolationException ex) {
      throw badRequest(resolveRegisterIntegrityMessage(account, ex));
    }
    authQueryDao.replaceUserRoles(entity.getId(), List.of("user"));
    return true;
  }

  private UserEntity saveUser(UserEntity user) {
    if (user.getId() == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
    return user;
  }

  private String resolveRegisterIntegrityMessage(String account, DataIntegrityViolationException ex) {
    if (userMapper.countByAccount(account) > 0) {
      return MSG_ACCOUNT_EXISTS;
    }
    String detail = resolveRootMessage(ex).toLowerCase(Locale.ROOT);
    if (looksLikeAccountDuplicate(detail)) {
      return MSG_ACCOUNT_EXISTS;
    }
    return MSG_REGISTER_RETRY;
  }

  private String resolveRootMessage(Throwable throwable) {
    if (throwable == null) {
      return "";
    }
    Throwable cursor = throwable;
    while (cursor.getCause() != null) {
      cursor = cursor.getCause();
    }
    return cursor.getMessage() == null ? "" : cursor.getMessage();
  }

  private boolean looksLikeAccountDuplicate(String message) {
    if (message == null || message.isBlank()) {
      return false;
    }
    boolean duplicateViolation = message.contains("duplicate")
      || message.contains("unique constraint")
      || message.contains("unique index")
      || message.contains("unique key")
      || message.contains("duplicate key");
    if (!duplicateViolation) {
      return false;
    }
    return message.contains("account")
      || message.contains("users.account")
      || message.contains("users_account")
      || message.contains("uk_users_account")
      || message.contains("idx_users_account")
      || message.contains("uq_users_account");
  }

  private boolean safeEquals(String left, String right) {
    if (left == null) {
      return right == null;
    }
    return left.equals(right);
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
}
