package elexvx.admin.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import elexvx.admin.dao.AuthQueryDao;
import elexvx.admin.entity.SecuritySetting;
import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.entity.UserEntity;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.mapper.SecurityCaptchaSettingMapper;
import elexvx.admin.mapper.SecurityPasswordPolicyMapper;
import elexvx.admin.mapper.SecurityTokenSettingMapper;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.mapper.VerificationEmailSettingMapper;
import elexvx.admin.mapper.VerificationSmsSettingMapper;
import elexvx.admin.security.AuthContext;
import elexvx.admin.security.AuthSession;
import elexvx.admin.service.AuthTokenService;
import elexvx.admin.service.AuthValidationService;
import elexvx.admin.service.CaptchaService;
import elexvx.admin.service.ConcurrentLoginService;
import elexvx.admin.service.EmailCodeService;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.PasswordPolicyService;
import elexvx.admin.service.PermissionFacade;
import elexvx.admin.service.SecuritySettingService;
import elexvx.admin.service.SmsCodeService;
import elexvx.admin.service.VerificationSettingService;
import elexvx.admin.service.auth.model.req.AuthLoginReq;
import elexvx.admin.service.auth.model.req.AuthRegisterReq;
import elexvx.admin.service.auth.model.req.PasswordResetReq;
import elexvx.admin.service.auth.model.resp.AuthLoginResp;
import elexvx.admin.verification.VerificationProviderRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class AuthDomainServiceTest {

  @Test
  void loginSuccess() {
    UserMapper userMapper = mock(UserMapper.class);
    SecuritySettingService securitySettingService = fixedSecuritySettingService(false);
    ConcurrentLoginOrchestrator orchestrator = mock(ConcurrentLoginOrchestrator.class);
    AuthTokenService authTokenService = mock(AuthTokenService.class);
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setAccount("admin");
    user.setName("admin");
    user.setGuid("g1");
    user.setStatus(1);
    user.setPasswordHash(BCrypt.hashpw("123456", BCrypt.gensalt()));
    when(userMapper.selectByAccount("admin")).thenReturn(user);
    ConcurrentLoginOrchestrator.DeviceSnapshot snapshot = new ConcurrentLoginOrchestrator.DeviceSnapshot("pc", "win", "chrome", "127.0.0.1", "local");
    when(orchestrator.buildDeviceSnapshot(any())).thenReturn(snapshot);
    when(orchestrator.resolveLoginDecision(any(), any(), any())).thenReturn(ConcurrentLoginOrchestrator.LoginDecision.pass());
    when(orchestrator.resolveTokenTimeoutSeconds()).thenReturn(3600L);
    when(orchestrator.buildSession(any(), any())).thenReturn(new AuthSession());
    when(authTokenService.createToken(anyLong(), any(), anyLong())).thenReturn("token-1");
    AuthLoginService service = new AuthLoginService(
      userMapper,
      mock(CaptchaService.class),
      securitySettingService,
      fixedVerificationSettingService(),
      mock(VerificationProviderRegistry.class),
      mock(SmsCodeService.class),
      mock(EmailCodeService.class),
      new AuthValidationService(Optional.empty(), Optional.empty()),
      orchestrator,
      mock(ConcurrentLoginService.class),
      authTokenService,
      mock(OperationLogService.class),
      mock(AuthContext.class),
      mock(PermissionFacade.class),
      mock(HttpServletRequest.class)
    );
    AuthLoginReq req = new AuthLoginReq();
    req.setAccount("admin");
    req.setPassword("123456");
    AuthLoginResp resp = service.loginByAccount(req);
    assertEquals("token-1", resp.getToken());
  }

  @Test
  void loginFailed() {
    UserMapper userMapper = mock(UserMapper.class);
    SecuritySettingService securitySettingService = fixedSecuritySettingService(false);
    UserEntity user = new UserEntity();
    user.setAccount("admin");
    user.setPasswordHash(BCrypt.hashpw("123456", BCrypt.gensalt()));
    when(userMapper.selectByAccount("admin")).thenReturn(user);
    AuthLoginService service = new AuthLoginService(
      userMapper,
      mock(CaptchaService.class),
      securitySettingService,
      fixedVerificationSettingService(),
      mock(VerificationProviderRegistry.class),
      mock(SmsCodeService.class),
      mock(EmailCodeService.class),
      new AuthValidationService(Optional.empty(), Optional.empty()),
      mock(ConcurrentLoginOrchestrator.class),
      mock(ConcurrentLoginService.class),
      mock(AuthTokenService.class),
      mock(OperationLogService.class),
      mock(AuthContext.class),
      mock(PermissionFacade.class),
      mock(HttpServletRequest.class)
    );
    AuthLoginReq req = new AuthLoginReq();
    req.setAccount("admin");
    req.setPassword("bad");
    assertThrows(BusinessException.class, () -> service.loginByAccount(req));
  }

  @Test
  void registerValidationFailed() {
    AuthRegisterService service = new AuthRegisterService(
      mock(UserMapper.class),
      fixedSecuritySettingService(false),
      mock(CaptchaService.class),
      new PasswordPolicyService(fixedSecuritySettingService(false)),
      new AuthValidationService(Optional.empty(), Optional.empty()),
      mock(AuthQueryDao.class)
    );
    AuthRegisterReq req = new AuthRegisterReq();
    req.setPassword("a");
    req.setConfirmPassword("b");
    assertThrows(BusinessException.class, () -> service.register(req));
  }

  @Test
  void resetPasswordBranchFailedByCode() {
    UserMapper userMapper = mock(UserMapper.class);
    AuthValidationService validationService = new TestAuthValidationService();
    VerificationSettingService verificationSettingService = fixedVerificationSettingService();
    SmsCodeService smsCodeService = mock(SmsCodeService.class);
    when(smsCodeService.verify("13800000000", "1234")).thenReturn(false);
    PasswordResetService service = new PasswordResetService(
      userMapper,
      mock(AuthContext.class),
      new PasswordPolicyService(fixedSecuritySettingService(false)),
      validationService,
      verificationSettingService,
      smsCodeService,
      mock(EmailCodeService.class),
      mock(OperationLogService.class),
      mock(AuthTokenService.class)
    );
    PasswordResetReq req = new PasswordResetReq();
    req.setAccount("admin");
    req.setPhone("13800000000");
    req.setCode("1234");
    req.setNewPassword("Aa123456!");
    req.setConfirmPassword("Aa123456!");
    assertThrows(BusinessException.class, () -> service.resetPassword(req));
  }

  @Test
  void concurrentLoginConflictReturnsPending() {
    UserMapper userMapper = mock(UserMapper.class);
    SecuritySettingService securitySettingService = fixedSecuritySettingService(false);
    UserEntity user = new UserEntity();
    user.setId(9L);
    user.setAccount("demo");
    user.setPasswordHash(BCrypt.hashpw("123456", BCrypt.gensalt()));
    user.setStatus(1);
    when(userMapper.selectByAccount("demo")).thenReturn(user);
    ConcurrentLoginOrchestrator orchestrator = mock(ConcurrentLoginOrchestrator.class);
    when(orchestrator.buildDeviceSnapshot(any())).thenReturn(new ConcurrentLoginOrchestrator.DeviceSnapshot("pc", "win", "chrome", "127.0.0.1", "local"));
    when(orchestrator.resolveLoginDecision(any(), any(), any()))
      .thenReturn(ConcurrentLoginOrchestrator.LoginDecision.pending("req-1", "key-1"));
    AuthLoginService service = new AuthLoginService(
      userMapper,
      mock(CaptchaService.class),
      securitySettingService,
      fixedVerificationSettingService(),
      mock(VerificationProviderRegistry.class),
      mock(SmsCodeService.class),
      mock(EmailCodeService.class),
      new AuthValidationService(Optional.empty(), Optional.empty()),
      orchestrator,
      mock(ConcurrentLoginService.class),
      mock(AuthTokenService.class),
      mock(OperationLogService.class),
      mock(AuthContext.class),
      mock(PermissionFacade.class),
      mock(HttpServletRequest.class)
    );
    AuthLoginReq req = new AuthLoginReq();
    req.setAccount("demo");
    req.setPassword("123456");
    AuthLoginResp resp = service.loginByAccount(req);
    assertEquals("req-1", resp.getPendingRequestId());
    assertEquals("key-1", resp.getPendingRequestKey());
  }

  private static SecuritySettingService fixedSecuritySettingService(boolean captchaEnabled) {
    SecurityTokenSettingMapper tokenMapper = mock(SecurityTokenSettingMapper.class);
    SecurityCaptchaSettingMapper captchaMapper = mock(SecurityCaptchaSettingMapper.class);
    SecurityPasswordPolicyMapper policyMapper = mock(SecurityPasswordPolicyMapper.class);
    SecuritySettingService service = new SecuritySettingService(tokenMapper, captchaMapper, policyMapper) {
      @Override
      public SecuritySetting getOrCreate() {
        SecuritySetting setting = new SecuritySetting();
        setting.setCaptchaEnabled(captchaEnabled);
        setting.setPasswordMinLength(6);
        setting.setPasswordRequireUppercase(false);
        setting.setPasswordRequireLowercase(false);
        setting.setPasswordRequireSpecial(false);
        setting.setPasswordAllowSequential(true);
        return setting;
      }
    };
    return service;
  }

  private static VerificationSettingService fixedVerificationSettingService() {
    VerificationSmsSettingMapper smsMapper = mock(VerificationSmsSettingMapper.class);
    VerificationEmailSettingMapper emailMapper = mock(VerificationEmailSettingMapper.class);
    VerificationSettingService service = new VerificationSettingService(
      smsMapper,
      emailMapper,
      null
    ) {
      @Override
      public VerificationSetting getDecryptedCopy() {
        VerificationSetting setting = new VerificationSetting();
        setting.setSmsEnabled(true);
        setting.setEmailEnabled(true);
        return setting;
      }
    };
    return service;
  }

  private static class TestAuthValidationService extends AuthValidationService {
    TestAuthValidationService() {
      super(Optional.empty(), Optional.empty());
    }

    @Override
    public void ensureSmsEnabled(VerificationSetting setting) {}

    @Override
    public String normalizePhone(String phone) {
      return phone == null ? "" : phone.trim();
    }

    @Override
    public String normalizeAccount(String account) {
      return account == null ? "" : account.trim();
    }

    @Override
    public String normalizeCode(String code) {
      return code == null ? "" : code.trim();
    }
  }
}
