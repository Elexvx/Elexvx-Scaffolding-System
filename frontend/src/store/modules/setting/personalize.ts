import OFFLINE_UI_CONFIG from '@/config/offline';

import { resolveAssetUrl } from './loginPage';
import type { SettingState } from './types';

export const isEmptyUiSetting = (s: Record<string, any>) => {
  const hasValue = (value: unknown) => {
    if (value === null || value === undefined) return false;
    if (typeof value === 'string') return value.trim().length > 0;
    return true;
  };
  return ![
    s.websiteName,
    s.logoExpandedUrl,
    s.logoCollapsedUrl,
    s.loginBgUrl,
    s.faviconUrl,
    s.footerCompany,
    s.footerIcp,
    s.copyrightStartYear,
    s.appVersion,
    s.defaultHome,
    s.qrCodeUrl,
    s.headerGithubUrl,
    s.headerHelpUrl,
  ].some(hasValue);
};

export const mapUiSettingToStatePayload = (s: Record<string, any>, currentAutoTheme: boolean): Partial<SettingState> => {
  const payload: Partial<SettingState> = {};
  if (s.footerCompany !== null && s.footerCompany !== undefined) payload.footerCompany = s.footerCompany;
  if (s.footerIcp !== null && s.footerIcp !== undefined) payload.footerIcp = s.footerIcp;
  if (s.websiteName !== null && s.websiteName !== undefined) payload.websiteName = s.websiteName;
  if (s.copyrightStartYear !== null && s.copyrightStartYear !== undefined) payload.copyrightStartYear = s.copyrightStartYear;
  if (s.appVersion !== null && s.appVersion !== undefined) payload.appVersion = s.appVersion;
  payload.logoExpandedUrl = resolveAssetUrl(s.logoExpandedUrl, OFFLINE_UI_CONFIG.logoExpandedUrl);
  payload.logoCollapsedUrl = resolveAssetUrl(s.logoCollapsedUrl, OFFLINE_UI_CONFIG.logoCollapsedUrl);
  if (s.allowMultiDeviceLogin !== null && s.allowMultiDeviceLogin !== undefined) payload.allowMultiDeviceLogin = !!s.allowMultiDeviceLogin;
  if (s.logRetentionDays !== null && s.logRetentionDays !== undefined) payload.logRetentionDays = s.logRetentionDays;
  if (s.defaultHome !== null && s.defaultHome !== undefined) payload.defaultHome = s.defaultHome;
  payload.loginBgUrl = resolveAssetUrl(s.loginBgUrl, OFFLINE_UI_CONFIG.loginBgUrl);
  if (s.qrCodeUrl !== null && s.qrCodeUrl !== undefined) payload.qrCodeUrl = s.qrCodeUrl;
  if (s.headerGithubUrl !== null && s.headerGithubUrl !== undefined) payload.headerGithubUrl = String(s.headerGithubUrl || '');
  if (s.headerHelpUrl !== null && s.headerHelpUrl !== undefined) payload.headerHelpUrl = String(s.headerHelpUrl || '');
  if (s.maintenanceEnabled !== null && s.maintenanceEnabled !== undefined) payload.maintenanceEnabled = !!s.maintenanceEnabled;
  if (s.maintenanceMessage !== null && s.maintenanceMessage !== undefined) payload.maintenanceMessage = s.maintenanceMessage;
  if (s.smsEnabled !== null && s.smsEnabled !== undefined) payload.smsEnabled = !!s.smsEnabled;
  if (s.emailEnabled !== null && s.emailEnabled !== undefined) payload.emailEnabled = !!s.emailEnabled;
  payload.faviconUrl = resolveAssetUrl(s.faviconUrl, OFFLINE_UI_CONFIG.faviconUrl);
  const nextAutoTheme = s.autoTheme !== null && s.autoTheme !== undefined ? !!s.autoTheme : currentAutoTheme;
  if (s.autoTheme !== null && s.autoTheme !== undefined) payload.autoTheme = nextAutoTheme;
  if (s.lightStartTime !== null && s.lightStartTime !== undefined) payload.lightStartTime = s.lightStartTime;
  if (s.darkStartTime !== null && s.darkStartTime !== undefined) payload.darkStartTime = s.darkStartTime;
  if (s.userAgreement !== null && s.userAgreement !== undefined) payload.userAgreement = s.userAgreement;
  if (s.privacyAgreement !== null && s.privacyAgreement !== undefined) payload.privacyAgreement = s.privacyAgreement;
  if (s.passwordMinLength !== null && s.passwordMinLength !== undefined) payload.passwordMinLength = s.passwordMinLength;
  if (s.passwordRequireUppercase !== null && s.passwordRequireUppercase !== undefined) payload.passwordRequireUppercase = !!s.passwordRequireUppercase;
  if (s.passwordRequireLowercase !== null && s.passwordRequireLowercase !== undefined) payload.passwordRequireLowercase = !!s.passwordRequireLowercase;
  if (s.passwordRequireSpecial !== null && s.passwordRequireSpecial !== undefined) payload.passwordRequireSpecial = !!s.passwordRequireSpecial;
  if (s.passwordAllowSequential !== null && s.passwordAllowSequential !== undefined) payload.passwordAllowSequential = !!s.passwordAllowSequential;
  if (s.showFooter !== null && s.showFooter !== undefined) payload.showFooter = !!s.showFooter;
  if (s.isSidebarCompact !== null && s.isSidebarCompact !== undefined) payload.isSidebarCompact = !!s.isSidebarCompact;
  if (s.showBreadcrumb !== null && s.showBreadcrumb !== undefined) payload.showBreadcrumb = !!s.showBreadcrumb;
  if (s.menuAutoCollapsed !== null && s.menuAutoCollapsed !== undefined) payload.menuAutoCollapsed = !!s.menuAutoCollapsed;
  if (!nextAutoTheme && s.mode !== null && s.mode !== undefined) payload.mode = s.mode;
  if (s.layout !== null && s.layout !== undefined) payload.layout = s.layout;
  if (s.splitMenu !== null && s.splitMenu !== undefined) payload.splitMenu = !!s.splitMenu;
  if (!nextAutoTheme && s.sideMode !== null && s.sideMode !== undefined) payload.sideMode = s.sideMode;
  if (s.isFooterAside !== null && s.isFooterAside !== undefined) payload.isFooterAside = !!s.isFooterAside;
  if (s.isSidebarFixed !== null && s.isSidebarFixed !== undefined) payload.isSidebarFixed = !!s.isSidebarFixed;
  if (s.isHeaderFixed !== null && s.isHeaderFixed !== undefined) payload.isHeaderFixed = !!s.isHeaderFixed;
  if (s.isUseTabsRouter !== null && s.isUseTabsRouter !== undefined) payload.isUseTabsRouter = !!s.isUseTabsRouter;
  if (s.showHeader !== null && s.showHeader !== undefined) payload.showHeader = !!s.showHeader;
  if (s.brandTheme !== null && s.brandTheme !== undefined) payload.brandTheme = s.brandTheme;
  return payload;
};
