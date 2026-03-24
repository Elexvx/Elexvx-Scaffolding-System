package elexvx.admin.service;

import elexvx.admin.dto.UiSettingRequest;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.service.setting.UiBrandSettingDomainService;
import elexvx.admin.service.setting.UiFooterSettingDomainService;
import elexvx.admin.service.setting.UiLayoutSettingDomainService;
import elexvx.admin.service.setting.UiLegalSettingDomainService;
import elexvx.admin.service.setting.UiLoginSettingDomainService;
import elexvx.admin.service.setting.UiSystemSettingDomainService;
import elexvx.admin.service.setting.UiThemeSettingDomainService;
import org.springframework.stereotype.Service;

@Service
public class UiSettingService {
  private final UiBrandSettingDomainService brandService;
  private final UiLayoutSettingDomainService layoutService;
  private final UiThemeSettingDomainService themeService;
  private final UiFooterSettingDomainService footerService;
  private final UiLoginSettingDomainService loginService;
  private final UiLegalSettingDomainService legalService;
  private final UiSystemSettingDomainService systemService;

  public UiSettingService(
    UiBrandSettingDomainService brandService,
    UiLayoutSettingDomainService layoutService,
    UiThemeSettingDomainService themeService,
    UiFooterSettingDomainService footerService,
    UiLoginSettingDomainService loginService,
    UiLegalSettingDomainService legalService,
    UiSystemSettingDomainService systemService
  ) {
    this.brandService = brandService;
    this.layoutService = layoutService;
    this.themeService = themeService;
    this.footerService = footerService;
    this.loginService = loginService;
    this.legalService = legalService;
    this.systemService = systemService;
  }

  public UiSetting getOrCreate() {
    UiSetting out = new UiSetting();
    brandService.fill(out);
    layoutService.fill(out);
    themeService.fill(out);
    footerService.fill(out);
    loginService.fill(out);
    legalService.fill(out);
    systemService.fill(out);
    return out;
  }

  public UiSetting save(UiSettingRequest req) {
    brandService.apply(req.toBrandSettingRequest());
    layoutService.apply(req.toLayoutSettingRequest());
    themeService.apply(req.toThemeSettingRequest());
    footerService.apply(req.toFooterSettingRequest());
    loginService.apply(req.toLoginSettingRequest());
    legalService.apply(req.toLegalSettingRequest());
    systemService.apply(req.toSystemSettingRequest());
    return getOrCreate();
  }

  public boolean isMultiDeviceLoginAllowed() {
    return loginService.isMultiDeviceLoginAllowed();
  }

  public Integer getLogRetentionDays() {
    return systemService.getLogRetentionDays();
  }
}
