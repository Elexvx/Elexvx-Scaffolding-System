import type { TColorSeries } from '@/config/color';
import { LIGHT_CHART_COLORS } from '@/config/color';
import OFFLINE_UI_CONFIG from '@/config/offline';
import STYLE_CONFIG from '@/config/style';

export const createSettingInitialState = () => ({
  ...STYLE_CONFIG,
  ...OFFLINE_UI_CONFIG,
  showSettingPanel: false,
  colorList: {} as TColorSeries,
  chartColors: LIGHT_CHART_COLORS,
  uiRefreshSeq: 0,
  footerCompany: '宏翔商道',
  footerIcp: '',
  userAgreement: '',
  privacyAgreement: '',
  allowMultiDeviceLogin: true,
  logRetentionDays: 90,
  defaultHome: '/user/index',
  loginBgUrl: OFFLINE_UI_CONFIG.loginBgUrl,
  qrCodeUrl: '',
  headerGithubUrl: '',
  headerHelpUrl: '',
  maintenanceEnabled: false,
  maintenanceMessage: '',
  smsEnabled: false,
  emailEnabled: false,
  autoTheme: false,
  lightStartTime: '06:00',
  darkStartTime: '18:00',
  autoThemeTimer: null as any,
  passwordMinLength: 6,
  passwordRequireUppercase: false,
  passwordRequireLowercase: false,
  passwordRequireSpecial: false,
  passwordAllowSequential: true,
  watermark: {
    enabled: false,
    type: 'text',
    content: '',
    imageUrl: '',
    opacity: 0.12,
    size: 120,
    gapX: 200,
    gapY: 200,
    rotate: 20,
  },
});

export type SettingState = ReturnType<typeof createSettingInitialState>;
export type SettingStateKey = keyof SettingState;
