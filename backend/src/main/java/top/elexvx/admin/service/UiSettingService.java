package top.elexvx.admin.service;

import top.elexvx.admin.dto.UiSettingRequest;
import top.elexvx.admin.entity.UiBrandSetting;
import top.elexvx.admin.entity.UiFooterSetting;
import top.elexvx.admin.entity.UiLayoutSetting;
import top.elexvx.admin.entity.UiLegalSetting;
import top.elexvx.admin.entity.UiLoginSetting;
import top.elexvx.admin.entity.UiSetting;
import top.elexvx.admin.entity.UiSystemSetting;
import top.elexvx.admin.entity.UiThemeSetting;
import top.elexvx.admin.mapper.UiBrandSettingMapper;
import top.elexvx.admin.mapper.UiFooterSettingMapper;
import top.elexvx.admin.mapper.UiLayoutSettingMapper;
import top.elexvx.admin.mapper.UiLegalSettingMapper;
import top.elexvx.admin.mapper.UiLoginSettingMapper;
import top.elexvx.admin.mapper.UiSystemSettingMapper;
import top.elexvx.admin.mapper.UiThemeSettingMapper;
import org.springframework.stereotype.Service;

@Service
public class UiSettingService {
  private final UiBrandSettingMapper brandMapper;
  private final UiLayoutSettingMapper layoutMapper;
  private final UiThemeSettingMapper themeMapper;
  private final UiFooterSettingMapper footerMapper;
  private final UiLoginSettingMapper loginMapper;
  private final UiLegalSettingMapper legalMapper;
  private final UiSystemSettingMapper systemMapper;

  public UiSettingService(
    UiBrandSettingMapper brandMapper,
    UiLayoutSettingMapper layoutMapper,
    UiThemeSettingMapper themeMapper,
    UiFooterSettingMapper footerMapper,
    UiLoginSettingMapper loginMapper,
    UiLegalSettingMapper legalMapper,
    UiSystemSettingMapper systemMapper
  ) {
    this.brandMapper = brandMapper;
    this.layoutMapper = layoutMapper;
    this.themeMapper = themeMapper;
    this.footerMapper = footerMapper;
    this.loginMapper = loginMapper;
    this.legalMapper = legalMapper;
    this.systemMapper = systemMapper;
  }

  public UiSetting getOrCreate() {
    UiSetting out = new UiSetting();
    UiBrandSetting brand = brandMapper.selectTop();
    if (brand != null) {
      out.setWebsiteName(brand.getWebsiteName());
      out.setAppVersion(brand.getAppVersion());
      out.setLogoExpandedUrl(brand.getLogoExpandedUrl());
      out.setLogoCollapsedUrl(brand.getLogoCollapsedUrl());
      out.setFaviconUrl(brand.getFaviconUrl());
      out.setQrCodeUrl(brand.getQrCodeUrl());
    }

    UiLayoutSetting layout = layoutMapper.selectTop();
    if (layout != null) {
      out.setDefaultHome(layout.getDefaultHome());
      out.setShowFooter(layout.getShowFooter());
      out.setIsSidebarCompact(layout.getIsSidebarCompact());
      out.setShowBreadcrumb(layout.getShowBreadcrumb());
      out.setMenuAutoCollapsed(layout.getMenuAutoCollapsed());
      out.setLayout(layout.getLayout());
      out.setSplitMenu(layout.getSplitMenu());
      out.setSideMode(layout.getSideMode());
      out.setIsFooterAside(layout.getIsFooterAside());
      out.setIsSidebarFixed(layout.getIsSidebarFixed());
      out.setIsHeaderFixed(layout.getIsHeaderFixed());
      out.setIsUseTabsRouter(layout.getIsUseTabsRouter());
      out.setShowHeader(layout.getShowHeader());
      out.setHeaderGithubUrl(layout.getHeaderGithubUrl());
      out.setHeaderHelpUrl(layout.getHeaderHelpUrl());
    }

    UiThemeSetting theme = themeMapper.selectTop();
    if (theme != null) {
      out.setAutoTheme(theme.getAutoTheme());
      out.setLightStartTime(theme.getLightStartTime());
      out.setDarkStartTime(theme.getDarkStartTime());
      out.setMode(theme.getMode());
      out.setBrandTheme(theme.getBrandTheme());
    }

    UiFooterSetting footer = footerMapper.selectTop();
    if (footer != null) {
      out.setFooterCompany(footer.getFooterCompany());
      out.setFooterIcp(footer.getFooterIcp());
      out.setCopyrightStartYear(footer.getCopyrightStartYear());
    }

    UiLoginSetting login = loginMapper.selectTop();
    if (login != null) {
      out.setLoginBgUrl(login.getLoginBgUrl());
      out.setAllowMultiDeviceLogin(login.getAllowMultiDeviceLogin());
    }

    UiLegalSetting legal = legalMapper.selectTop();
    if (legal != null) {
      out.setUserAgreement(legal.getUserAgreement());
      out.setPrivacyAgreement(legal.getPrivacyAgreement());
    }

    UiSystemSetting system = systemMapper.selectTop();
    if (system != null) {
      out.setLogRetentionDays(system.getLogRetentionDays());
      out.setMaintenanceEnabled(system.getMaintenanceEnabled());
      out.setMaintenanceMessage(system.getMaintenanceMessage());
    }

    return out;
  }

  public UiSetting save(UiSettingRequest req) {
    applyBrand(req);
    applyLayout(req);
    applyTheme(req);
    applyFooter(req);
    applyLogin(req);
    applyLegal(req);
    applySystem(req);
    return getOrCreate();
  }

  public boolean isMultiDeviceLoginAllowed() {
    UiLoginSetting setting = loginMapper.selectTop();
    return setting != null && Boolean.TRUE.equals(setting.getAllowMultiDeviceLogin());
  }

  public Integer getLogRetentionDays() {
    UiSystemSetting setting = systemMapper.selectTop();
    return setting == null ? null : setting.getLogRetentionDays();
  }

  private void applyBrand(UiSettingRequest req) {
    UiBrandSetting s = brandMapper.selectTop();
    if (s == null) s = new UiBrandSetting();
    boolean changed = false;
    if (req.getWebsiteName() != null) { s.setWebsiteName(req.getWebsiteName()); changed = true; }
    if (req.getAppVersion() != null) { s.setAppVersion(req.getAppVersion()); changed = true; }
    if (req.getLogoExpandedUrl() != null) { s.setLogoExpandedUrl(req.getLogoExpandedUrl()); changed = true; }
    if (req.getLogoCollapsedUrl() != null) { s.setLogoCollapsedUrl(req.getLogoCollapsedUrl()); changed = true; }
    if (req.getFaviconUrl() != null) { s.setFaviconUrl(req.getFaviconUrl()); changed = true; }
    if (req.getQrCodeUrl() != null) { s.setQrCodeUrl(req.getQrCodeUrl()); changed = true; }
    if (changed) upsertBrand(s);
  }

  private void applyLayout(UiSettingRequest req) {
    UiLayoutSetting s = layoutMapper.selectTop();
    if (s == null) s = new UiLayoutSetting();
    boolean changed = false;
    if (req.getDefaultHome() != null) { s.setDefaultHome(req.getDefaultHome()); changed = true; }
    if (req.getShowFooter() != null) { s.setShowFooter(req.getShowFooter()); changed = true; }
    if (req.getIsSidebarCompact() != null) { s.setIsSidebarCompact(req.getIsSidebarCompact()); changed = true; }
    if (req.getShowBreadcrumb() != null) { s.setShowBreadcrumb(req.getShowBreadcrumb()); changed = true; }
    if (req.getMenuAutoCollapsed() != null) { s.setMenuAutoCollapsed(req.getMenuAutoCollapsed()); changed = true; }
    if (req.getLayout() != null) { s.setLayout(req.getLayout()); changed = true; }
    if (req.getSplitMenu() != null) { s.setSplitMenu(req.getSplitMenu()); changed = true; }
    if (req.getSideMode() != null) { s.setSideMode(req.getSideMode()); changed = true; }
    if (req.getIsFooterAside() != null) { s.setIsFooterAside(req.getIsFooterAside()); changed = true; }
    if (req.getIsSidebarFixed() != null) { s.setIsSidebarFixed(req.getIsSidebarFixed()); changed = true; }
    if (req.getIsHeaderFixed() != null) { s.setIsHeaderFixed(req.getIsHeaderFixed()); changed = true; }
    if (req.getIsUseTabsRouter() != null) { s.setIsUseTabsRouter(req.getIsUseTabsRouter()); changed = true; }
    if (req.getShowHeader() != null) { s.setShowHeader(req.getShowHeader()); changed = true; }
    if (req.getHeaderGithubUrl() != null) { s.setHeaderGithubUrl(req.getHeaderGithubUrl()); changed = true; }
    if (req.getHeaderHelpUrl() != null) { s.setHeaderHelpUrl(req.getHeaderHelpUrl()); changed = true; }
    if (changed) upsertLayout(s);
  }

  private void applyTheme(UiSettingRequest req) {
    UiThemeSetting s = themeMapper.selectTop();
    if (s == null) s = new UiThemeSetting();
    boolean changed = false;
    if (req.getAutoTheme() != null) { s.setAutoTheme(req.getAutoTheme()); changed = true; }
    if (req.getLightStartTime() != null) { s.setLightStartTime(req.getLightStartTime()); changed = true; }
    if (req.getDarkStartTime() != null) { s.setDarkStartTime(req.getDarkStartTime()); changed = true; }
    if (req.getMode() != null) { s.setMode(req.getMode()); changed = true; }
    if (req.getBrandTheme() != null) { s.setBrandTheme(req.getBrandTheme()); changed = true; }
    if (changed) upsertTheme(s);
  }

  private void applyFooter(UiSettingRequest req) {
    UiFooterSetting s = footerMapper.selectTop();
    if (s == null) s = new UiFooterSetting();
    boolean changed = false;
    if (req.getFooterCompany() != null) { s.setFooterCompany(req.getFooterCompany()); changed = true; }
    if (req.getFooterIcp() != null) { s.setFooterIcp(req.getFooterIcp()); changed = true; }
    if (req.getCopyrightStartYear() != null) { s.setCopyrightStartYear(req.getCopyrightStartYear()); changed = true; }
    if (changed) upsertFooter(s);
  }

  private void applyLogin(UiSettingRequest req) {
    UiLoginSetting s = loginMapper.selectTop();
    if (s == null) s = new UiLoginSetting();
    boolean changed = false;
    if (req.getLoginBgUrl() != null) { s.setLoginBgUrl(req.getLoginBgUrl()); changed = true; }
    if (req.getAllowMultiDeviceLogin() != null) { s.setAllowMultiDeviceLogin(req.getAllowMultiDeviceLogin()); changed = true; }
    if (changed) upsertLogin(s);
  }

  private void applyLegal(UiSettingRequest req) {
    UiLegalSetting s = legalMapper.selectTop();
    if (s == null) s = new UiLegalSetting();
    boolean changed = false;
    if (req.getUserAgreement() != null) { s.setUserAgreement(req.getUserAgreement()); changed = true; }
    if (req.getPrivacyAgreement() != null) { s.setPrivacyAgreement(req.getPrivacyAgreement()); changed = true; }
    if (changed) upsertLegal(s);
  }

  private void applySystem(UiSettingRequest req) {
    UiSystemSetting s = systemMapper.selectTop();
    if (s == null) s = new UiSystemSetting();
    boolean changed = false;
    if (req.getLogRetentionDays() != null) { s.setLogRetentionDays(req.getLogRetentionDays()); changed = true; }
    if (req.getMaintenanceEnabled() != null) { s.setMaintenanceEnabled(req.getMaintenanceEnabled()); changed = true; }
    if (req.getMaintenanceMessage() != null) { s.setMaintenanceMessage(req.getMaintenanceMessage()); changed = true; }
    if (changed) upsertSystem(s);
  }

  private void upsertBrand(UiBrandSetting s) {
    if (s.getId() == null) brandMapper.insert(s);
    else brandMapper.update(s);
  }

  private void upsertLayout(UiLayoutSetting s) {
    if (s.getId() == null) layoutMapper.insert(s);
    else layoutMapper.update(s);
  }

  private void upsertTheme(UiThemeSetting s) {
    if (s.getId() == null) themeMapper.insert(s);
    else themeMapper.update(s);
  }

  private void upsertFooter(UiFooterSetting s) {
    if (s.getId() == null) footerMapper.insert(s);
    else footerMapper.update(s);
  }

  private void upsertLogin(UiLoginSetting s) {
    if (s.getId() == null) loginMapper.insert(s);
    else loginMapper.update(s);
  }

  private void upsertLegal(UiLegalSetting s) {
    if (s.getId() == null) legalMapper.insert(s);
    else legalMapper.update(s);
  }

  private void upsertSystem(UiSystemSetting s) {
    if (s.getId() == null) systemMapper.insert(s);
    else systemMapper.update(s);
  }
}
