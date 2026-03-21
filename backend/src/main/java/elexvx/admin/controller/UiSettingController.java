package elexvx.admin.controller;

import elexvx.admin.annotation.RepeatSubmit;
import elexvx.admin.dto.EmailSendRequest;
import elexvx.admin.dto.UiSettingRequest;
import elexvx.admin.entity.SecuritySetting;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.security.AccessControlService;
import elexvx.admin.security.AuthContext;
import elexvx.admin.service.EmailSenderService;
import elexvx.admin.service.ObjectStorageService;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.SecurityRateLimitService;
import elexvx.admin.service.SecuritySettingService;
import elexvx.admin.service.UiSettingService;
import elexvx.admin.service.VerificationSettingService;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.UiSettingResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/system/ui")
public class UiSettingController {
  private static final Logger log = LoggerFactory.getLogger(UiSettingController.class);
  private final UiSettingService uiSettingService;
  private final OperationLogService operationLogService;
  private final ObjectStorageService storageService;
  private final Optional<EmailSenderService> emailSenderService;
  private final VerificationSettingService verificationSettingService;
  private final SecuritySettingService securitySettingService;
  private final AuthContext authContext;
  private final AccessControlService accessControlService;
  private final SecurityRateLimitService rateLimitService;

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public UiSettingController(
    UiSettingService uiSettingService,
    OperationLogService operationLogService,
    ObjectStorageService storageService,
    Optional<EmailSenderService> emailSenderService,
    VerificationSettingService verificationSettingService,
    SecuritySettingService securitySettingService,
    AuthContext authContext,
    AccessControlService accessControlService,
    SecurityRateLimitService rateLimitService
  ) {
    this.uiSettingService = uiSettingService;
    this.operationLogService = operationLogService;
    this.storageService = storageService;
    this.emailSenderService = emailSenderService;
    this.verificationSettingService = verificationSettingService;
    this.securitySettingService = securitySettingService;
    this.authContext = authContext;
    this.accessControlService = accessControlService;
    this.rateLimitService = rateLimitService;
  }

  @GetMapping("/public")
  public ApiResponse<UiSettingResponse> getPublic() {
    UiSetting setting = uiSettingService.getOrCreate();
    VerificationSetting verificationSetting = verificationSettingService.getDecryptedCopy();
    SecuritySetting securitySetting = securitySettingService.getOrCreate();
    UiSettingResponse response = new UiSettingResponse();
    response.setFooterCompany(setting.getFooterCompany());
    response.setFooterIcp(setting.getFooterIcp());
    response.setWebsiteName(setting.getWebsiteName());
    response.setCopyrightStartYear(setting.getCopyrightStartYear());
    response.setLogoExpandedUrl(setting.getLogoExpandedUrl());
    response.setLogoCollapsedUrl(setting.getLogoCollapsedUrl());
    response.setLoginBgUrl(setting.getLoginBgUrl());
    response.setFaviconUrl(setting.getFaviconUrl());
    response.setMaintenanceEnabled(setting.getMaintenanceEnabled());
    response.setMaintenanceMessage(setting.getMaintenanceMessage());
    response.setAutoTheme(setting.getAutoTheme());
    response.setLightStartTime(setting.getLightStartTime());
    response.setDarkStartTime(setting.getDarkStartTime());
    response.setMode(setting.getMode());
    response.setBrandTheme(setting.getBrandTheme());
    response.setHeaderGithubUrl(setting.getHeaderGithubUrl());
    response.setHeaderHelpUrl(setting.getHeaderHelpUrl());
    response.setUserAgreement(setting.getUserAgreement());
    response.setPrivacyAgreement(setting.getPrivacyAgreement());
    response.setSmsEnabled(Boolean.TRUE.equals(verificationSetting.getSmsEnabled()));
    response.setEmailEnabled(Boolean.TRUE.equals(verificationSetting.getEmailEnabled()));
    response.setCaptchaEnabled(securitySetting.getCaptchaEnabled());
    response.setCaptchaType(securitySetting.getCaptchaType());
    response.setDragCaptchaWidth(securitySetting.getDragCaptchaWidth());
    response.setDragCaptchaHeight(securitySetting.getDragCaptchaHeight());
    response.setDragCaptchaThreshold(securitySetting.getDragCaptchaThreshold());
    response.setImageCaptchaLength(securitySetting.getImageCaptchaLength());
    response.setImageCaptchaNoiseLines(securitySetting.getImageCaptchaNoiseLines());
    response.setPasswordMinLength(securitySetting.getPasswordMinLength());
    response.setPasswordRequireUppercase(securitySetting.getPasswordRequireUppercase());
    response.setPasswordRequireLowercase(securitySetting.getPasswordRequireLowercase());
    response.setPasswordRequireSpecial(securitySetting.getPasswordRequireSpecial());
    response.setPasswordAllowSequential(securitySetting.getPasswordAllowSequential());
    return ApiResponse.success(response);
  }

  @GetMapping
  public ApiResponse<UiSettingResponse> get() {
    authContext.requireUserId();
    boolean hasQueryPermission = accessControlService.hasRole("admin")
      || accessControlService.hasPermission("system:SystemPersonalize:query")
      || accessControlService.hasPermission("system:SystemVerification:query")
      || accessControlService.hasPermission("system:SystemSecurity:query");
    if (!hasQueryPermission) {
      throw new AccessDeniedException("权限不足，请联系管理员开通");
    }
    UiSetting setting = uiSettingService.getOrCreate();
    VerificationSetting verificationSetting = verificationSettingService.getDecryptedCopy();
    SecuritySetting securitySetting = securitySettingService.getOrCreate();
    boolean canViewVerificationSensitive = false;
    boolean canViewSecuritySensitive = false;
    if (authContext.isAuthenticated()) {
      // 登录用户可查看更完整的系统配置，未登录用户仅返回基础字段（避免刷新时触发登录失效）。
      boolean isAdmin = accessControlService.hasRole("admin");
      canViewVerificationSensitive = isAdmin
        || accessControlService.hasPermission("system:SystemVerification:query")
        || accessControlService.hasPermission("system:SystemVerification:update");
      canViewSecuritySensitive = isAdmin
        || accessControlService.hasPermission("system:SystemSecurity:query")
        || accessControlService.hasPermission("system:SystemSecurity:update");
    }

    UiSettingResponse response = new UiSettingResponse();
    response.setFooterCompany(setting.getFooterCompany());
    response.setFooterIcp(setting.getFooterIcp());
    response.setWebsiteName(setting.getWebsiteName());
    response.setCopyrightStartYear(setting.getCopyrightStartYear());
    response.setAppVersion(setting.getAppVersion());
    response.setLogoExpandedUrl(setting.getLogoExpandedUrl());
    response.setLogoCollapsedUrl(setting.getLogoCollapsedUrl());
    response.setLoginBgUrl(setting.getLoginBgUrl());
    response.setFaviconUrl(setting.getFaviconUrl());
    response.setQrCodeUrl(setting.getQrCodeUrl());
    response.setAllowMultiDeviceLogin(setting.getAllowMultiDeviceLogin());
    response.setLogRetentionDays(setting.getLogRetentionDays());
    response.setMaintenanceEnabled(setting.getMaintenanceEnabled());
    response.setMaintenanceMessage(setting.getMaintenanceMessage());
    response.setDefaultHome(setting.getDefaultHome());
    response.setAutoTheme(setting.getAutoTheme());
    response.setLightStartTime(setting.getLightStartTime());
    response.setDarkStartTime(setting.getDarkStartTime());
    response.setShowFooter(setting.getShowFooter());
    response.setIsSidebarCompact(setting.getIsSidebarCompact());
    response.setShowBreadcrumb(setting.getShowBreadcrumb());
    response.setMenuAutoCollapsed(setting.getMenuAutoCollapsed());
    response.setMode(setting.getMode());
    response.setLayout(setting.getLayout());
    response.setSplitMenu(setting.getSplitMenu());
    response.setSideMode(setting.getSideMode());
    response.setIsFooterAside(setting.getIsFooterAside());
    response.setIsSidebarFixed(setting.getIsSidebarFixed());
    response.setIsHeaderFixed(setting.getIsHeaderFixed());
    response.setIsUseTabsRouter(setting.getIsUseTabsRouter());
    response.setShowHeader(setting.getShowHeader());
    response.setHeaderGithubUrl(setting.getHeaderGithubUrl());
    response.setHeaderHelpUrl(setting.getHeaderHelpUrl());
    response.setBrandTheme(setting.getBrandTheme());
    response.setUserAgreement(setting.getUserAgreement());
    response.setPrivacyAgreement(setting.getPrivacyAgreement());

    response.setSmsEnabled(Boolean.TRUE.equals(verificationSetting.getSmsEnabled()));
    response.setSmsProvider(verificationSetting.getSmsProvider());
    response.setSmsAliyunEnabled(verificationSetting.getSmsAliyunEnabled());
    response.setSmsAliyunAccessKeyId(verificationSetting.getSmsAliyunAccessKeyId());
    response.setSmsAliyunAccessKeySecret(verificationSetting.getSmsAliyunAccessKeySecret());
    response.setSmsAliyunSignName(verificationSetting.getSmsAliyunSignName());
    response.setSmsAliyunTemplateCode(verificationSetting.getSmsAliyunTemplateCode());
    response.setSmsAliyunRegionId(verificationSetting.getSmsAliyunRegionId());
    response.setSmsAliyunEndpoint(verificationSetting.getSmsAliyunEndpoint());
    response.setSmsTencentEnabled(verificationSetting.getSmsTencentEnabled());
    response.setSmsTencentSecretId(verificationSetting.getSmsTencentSecretId());
    response.setSmsTencentSecretKey(verificationSetting.getSmsTencentSecretKey());
    response.setSmsTencentSignName(verificationSetting.getSmsTencentSignName());
    response.setSmsTencentTemplateId(verificationSetting.getSmsTencentTemplateId());
    response.setSmsTencentRegion(verificationSetting.getSmsTencentRegion());
    response.setSmsTencentEndpoint(verificationSetting.getSmsTencentEndpoint());
    response.setSmsSdkAppId(verificationSetting.getSmsSdkAppId());
    response.setEmailEnabled(Boolean.TRUE.equals(verificationSetting.getEmailEnabled()));
    response.setEmailHost(verificationSetting.getEmailHost());
    response.setEmailPort(verificationSetting.getEmailPort());
    response.setEmailUsername(verificationSetting.getEmailUsername());
    response.setEmailPassword(verificationSetting.getEmailPassword());
    response.setEmailFrom(verificationSetting.getEmailFrom());
    response.setEmailSsl(verificationSetting.getEmailSsl());

    response.setSessionTimeoutMinutes(securitySetting.getSessionTimeoutMinutes());
    response.setTokenTimeoutMinutes(securitySetting.getTokenTimeoutMinutes());
    response.setTokenRefreshGraceMinutes(securitySetting.getTokenRefreshGraceMinutes());
    response.setAllowUrlTokenParam(securitySetting.getAllowUrlTokenParam());
    response.setCaptchaEnabled(securitySetting.getCaptchaEnabled());
    response.setCaptchaType(securitySetting.getCaptchaType());
    response.setDragCaptchaWidth(securitySetting.getDragCaptchaWidth());
    response.setDragCaptchaHeight(securitySetting.getDragCaptchaHeight());
    response.setDragCaptchaThreshold(securitySetting.getDragCaptchaThreshold());
    response.setImageCaptchaLength(securitySetting.getImageCaptchaLength());
    response.setImageCaptchaNoiseLines(securitySetting.getImageCaptchaNoiseLines());
    response.setPasswordMinLength(securitySetting.getPasswordMinLength());
    response.setPasswordRequireUppercase(securitySetting.getPasswordRequireUppercase());
    response.setPasswordRequireLowercase(securitySetting.getPasswordRequireLowercase());
    response.setPasswordRequireSpecial(securitySetting.getPasswordRequireSpecial());
    response.setPasswordAllowSequential(securitySetting.getPasswordAllowSequential());

    if (!canViewVerificationSensitive) {
      response.setSmsProvider(null);
      response.setSmsAliyunAccessKeyId(null);
      response.setSmsAliyunAccessKeySecret(null);
      response.setSmsAliyunSignName(null);
      response.setSmsAliyunTemplateCode(null);
      response.setSmsAliyunRegionId(null);
      response.setSmsAliyunEndpoint(null);
      response.setSmsTencentSecretId(null);
      response.setSmsTencentSecretKey(null);
      response.setSmsTencentSignName(null);
      response.setSmsTencentTemplateId(null);
      response.setSmsTencentRegion(null);
      response.setSmsTencentEndpoint(null);
      response.setSmsSdkAppId(null);
      response.setEmailHost(null);
      response.setEmailPort(null);
      response.setEmailUsername(null);
      response.setEmailPassword(null);
      response.setEmailFrom(null);
      response.setEmailSsl(null);
    }

    if (!canViewSecuritySensitive) {
      response.setSessionTimeoutMinutes(null);
      response.setTokenTimeoutMinutes(null);
      response.setTokenRefreshGraceMinutes(null);
      response.setCaptchaEnabled(null);
      response.setCaptchaType(null);
      response.setDragCaptchaWidth(null);
      response.setDragCaptchaHeight(null);
      response.setDragCaptchaThreshold(null);
      response.setImageCaptchaLength(null);
      response.setImageCaptchaNoiseLines(null);
      response.setPasswordMinLength(null);
      response.setPasswordRequireUppercase(null);
      response.setPasswordRequireLowercase(null);
      response.setPasswordRequireSpecial(null);
      response.setPasswordAllowSequential(null);
    }

    return ApiResponse.success(response);
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<UiSettingResponse> save(@RequestBody @Valid UiSettingRequest req) {
    PermissionUtil.checkAny("system:SystemVerification:update", "system:SystemPersonalize:update", "system:SystemSecurity:update");
    uiSettingService.save(req);
    verificationSettingService.applyRequest(req.toVerificationProviderSettingRequest());
    securitySettingService.applyRequest(req.toSessionPolicySettingRequest(), req.toSecurityPolicySettingRequest());
    operationLogService.log("UPDATE", "系统设置", "更新系统个性化设置");
    return get();
  }

  @PostMapping("/email/test")
  @RepeatSubmit
  public ApiResponse<Boolean> testEmail(@RequestBody @Valid EmailSendRequest req) {
    authContext.requireUserId();
    PermissionUtil.checkAny("system:SystemVerification:update", "system:SystemPersonalize:update", "system:SystemSecurity:update");
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    if (setting == null || !Boolean.TRUE.equals(setting.getEmailEnabled())) {
      throw badRequest("邮箱验证已禁用");
    }
    EmailSenderService sender = emailSenderService.orElseThrow(() -> badRequest("邮箱模块未启用或未安装"));
    sender.sendTest(setting, req.getEmail());
    operationLogService.log("TEST", "系统设置", "发送测试邮件");
    return ApiResponse.success(true);
  }

  @PostMapping("/upload")
  @RepeatSubmit
  public ApiResponse<Map<String, String>> upload(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "page", required = false) String page
  ) throws IOException {
    PermissionUtil.checkAdmin();
    long userId = authContext.requireUserId();
    rateLimitService.checkUploadRequestQuota(userId);
    if (file == null || file.isEmpty()) {
      return ApiResponse.failure(ErrorCodes.BAD_REQUEST, "上传文件不能为空");
    }

    SecurityRateLimitService.UploadQuotaLease lease = null;
    try {
      lease = rateLimitService.acquireUploadQuota(userId, file.getSize());
      String url = storageService.upload(file, "system", page);
      return ApiResponse.success(Map.of("url", url));
    } catch (BusinessException e) {
      if (lease != null) {
        rateLimitService.releaseUploadQuota(lease);
      }
      String msg = e.getMessage() == null ? "上传请求被拒绝" : e.getMessage();
      int code = (msg.contains("频繁") || msg.contains("上限")) ? ErrorCodes.TOO_MANY_REQUESTS : ErrorCodes.BAD_REQUEST;
      return ApiResponse.failure(code, msg);
    } catch (Exception e) {
      if (lease != null) {
        rateLimitService.releaseUploadQuota(lease);
      }
      log.error("系统设置文件上传失败", e);
      return ApiResponse.failure(ErrorCodes.INTERNAL_SERVER_ERROR, "系统文件保存失败，请稍后重试");
    }
  }
}
