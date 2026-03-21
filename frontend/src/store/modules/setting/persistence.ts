import keys from 'lodash/keys';

import STYLE_CONFIG from '@/config/style';

export const settingPersistConfig = {
  key: 'elexvx.setting',
  paths: [...keys(STYLE_CONFIG), 'colorList', 'chartColors', 'uiRefreshSeq'],
};

export const UI_ALLOWED_SAVE_KEYS = [
  'showFooter',
  'isSidebarCompact',
  'showBreadcrumb',
  'menuAutoCollapsed',
  'mode',
  'layout',
  'splitMenu',
  'sideMode',
  'isFooterAside',
  'isSidebarFixed',
  'isHeaderFixed',
  'isUseTabsRouter',
  'showHeader',
  'brandTheme',
  'footerCompany',
  'footerIcp',
  'websiteName',
  'copyrightStartYear',
  'appVersion',
  'logoExpandedUrl',
  'logoCollapsedUrl',
  'allowMultiDeviceLogin',
  'logRetentionDays',
  'defaultHome',
  'loginBgUrl',
  'qrCodeUrl',
  'headerGithubUrl',
  'headerHelpUrl',
  'maintenanceEnabled',
  'maintenanceMessage',
  'userAgreement',
  'privacyAgreement',
  'autoTheme',
  'lightStartTime',
  'darkStartTime',
] as const;
