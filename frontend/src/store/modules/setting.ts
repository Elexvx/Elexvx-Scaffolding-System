import keys from 'lodash/keys';
import { defineStore } from 'pinia';
import { Color } from 'tvision-color';

import type { TColorSeries } from '@/config/color';
import { DARK_CHART_COLORS, LIGHT_CHART_COLORS } from '@/config/color';
import OFFLINE_UI_CONFIG from '@/config/offline';
import STYLE_CONFIG from '@/config/style';
import { store } from '@/store';
import type { ModeType } from '@/types/interface';
import { generateColorMap, insertThemeStylesheet } from '@/utils/color';

/**
 * UI 配置与站点设置 Store（主题、布局、站点信息、登录方式开关、水印等）。
 *
 * 数据来源与兜底：
 * - STYLE_CONFIG：前端默认 UI 配置（布局/主题/是否展示页脚等）
 * - OFFLINE_UI_CONFIG：离线兜底（后端 UI 设置不可用时仍可展示站点名称/Logo/favicon/登录背景）
 *
 * 说明：该 store 既服务于“界面即时状态”（如 showSettingPanel），也承载后端可配置的系统 UI 设置。
 */
const state: Record<string, any> = {
  ...STYLE_CONFIG,
  // 离线模式默认值：当后端 UI 设置不可用时，保证站点标题/Logo 等仍可显示。
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
};

export type TState = typeof state;
export type TStateKey = keyof typeof state;

export const useSettingStore = defineStore('setting', {
  state: () => state,
  getters: {
    showSidebar: (state) => state.layout !== 'top',
    showSidebarLogo: (state) => state.layout === 'side',
    showHeaderLogo: (state) => state.layout !== 'side',
    displayMode: (state): ModeType => {
      if (state.mode === 'auto') {
        const media = window.matchMedia('(prefers-color-scheme:dark)');
        if (media.matches) {
          return 'dark';
        }
        return 'light';
      }
      return state.mode as ModeType;
    },
    displaySideMode: (state): ModeType => {
      if (state.mode === 'auto') {
        const media = window.matchMedia('(prefers-color-scheme:dark)');
        return media.matches ? 'dark' : 'light';
      }
      return state.mode as ModeType;
    },
  },
  actions: {
    ensureOfflineAssetFallbacks() {
      const resolveAssetUrl = (value: unknown, fallback: string) => {
        if (typeof value === 'string' && value.trim()) return value;
        return fallback;
      };

      this.updateConfig({
        logoExpandedUrl: resolveAssetUrl(this.logoExpandedUrl, OFFLINE_UI_CONFIG.logoExpandedUrl),
        logoCollapsedUrl: resolveAssetUrl(this.logoCollapsedUrl, OFFLINE_UI_CONFIG.logoCollapsedUrl),
        loginBgUrl: resolveAssetUrl(this.loginBgUrl, OFFLINE_UI_CONFIG.loginBgUrl),
        faviconUrl: resolveAssetUrl(this.faviconUrl, OFFLINE_UI_CONFIG.faviconUrl),
      });
    },
    applyMockUiSetting() {
      const footerCompany =
        typeof this.footerCompany === 'string' && this.footerCompany.trim() ? this.footerCompany : '宏翔商道';
      const footerIcp = typeof this.footerIcp === 'string' ? this.footerIcp : '';
      this.updateConfig({
        websiteName: OFFLINE_UI_CONFIG.websiteName,
        copyrightStartYear: OFFLINE_UI_CONFIG.copyrightStartYear,
        logoExpandedUrl: OFFLINE_UI_CONFIG.logoExpandedUrl,
        logoCollapsedUrl: OFFLINE_UI_CONFIG.logoCollapsedUrl,
        faviconUrl: OFFLINE_UI_CONFIG.faviconUrl,
        loginBgUrl: OFFLINE_UI_CONFIG.loginBgUrl,
        footerCompany,
        footerIcp,
      });
    },
    async loadUiSetting() {
      const { request } = await import('@/utils/request');
      const { useUserStore } = await import('@/store');
      const userStore = useUserStore();
      try {
        const fetchUiSetting = async (endpoint: string, withToken: boolean) =>
          request.get<any>({ url: endpoint }, { withToken });

        const endpoint = userStore.token ? '/system/ui' : '/system/ui/public';
        let s: any;
        try {
          s = await fetchUiSetting(endpoint, !!userStore.token);
        } catch (error: any) {
          const msg = String(error?.message || '');
          // Logged-in users without system-setting permission should transparently fall back to public settings.
          if (userStore.token && (msg.includes('[403]') || msg.includes('[401]'))) {
            s = await fetchUiSetting('/system/ui/public', false);
          } else {
            throw error;
          }
        }
        if (!s) {
          this.ensureOfflineAssetFallbacks();
          return;
        }
        const hasValue = (value: unknown) => {
          if (value === null || value === undefined) return false;
          if (typeof value === 'string') return value.trim().length > 0;
          return true;
        };
        const isEmptySetting = ![
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
        if (isEmptySetting) {
          this.applyMockUiSetting();
          return;
        }

        const payload: Partial<TState> = {};
        if (s.footerCompany !== null && s.footerCompany !== undefined) payload.footerCompany = s.footerCompany;
        if (s.footerIcp !== null && s.footerIcp !== undefined) payload.footerIcp = s.footerIcp;
        if (s.websiteName !== null && s.websiteName !== undefined) payload.websiteName = s.websiteName;
        if (s.copyrightStartYear !== null && s.copyrightStartYear !== undefined)
          payload.copyrightStartYear = s.copyrightStartYear;
        if (s.appVersion !== null && s.appVersion !== undefined) payload.appVersion = s.appVersion;
        const resolveAssetUrl = (value: unknown, fallback: string) => {
          if (typeof value === 'string' && value.trim()) return value;
          return fallback;
        };

        payload.logoExpandedUrl = resolveAssetUrl(s.logoExpandedUrl, OFFLINE_UI_CONFIG.logoExpandedUrl);
        payload.logoCollapsedUrl = resolveAssetUrl(s.logoCollapsedUrl, OFFLINE_UI_CONFIG.logoCollapsedUrl);
        if (s.allowMultiDeviceLogin !== null && s.allowMultiDeviceLogin !== undefined)
          payload.allowMultiDeviceLogin = !!s.allowMultiDeviceLogin;
        if (s.logRetentionDays !== null && s.logRetentionDays !== undefined)
          payload.logRetentionDays = s.logRetentionDays;
        if (s.defaultHome !== null && s.defaultHome !== undefined) {
          payload.defaultHome = s.defaultHome;
          const { useTabsRouterStore } = await import('./tabs-router');
          const tabsRouterStore = useTabsRouterStore();
          tabsRouterStore.updateHomeTab(s.defaultHome);
        }
        payload.loginBgUrl = resolveAssetUrl(s.loginBgUrl, OFFLINE_UI_CONFIG.loginBgUrl);
        if (s.qrCodeUrl !== null && s.qrCodeUrl !== undefined) payload.qrCodeUrl = s.qrCodeUrl;
        if (s.headerGithubUrl !== null && s.headerGithubUrl !== undefined)
          payload.headerGithubUrl = String(s.headerGithubUrl || '');
        if (s.headerHelpUrl !== null && s.headerHelpUrl !== undefined)
          payload.headerHelpUrl = String(s.headerHelpUrl || '');
        if (s.maintenanceEnabled !== null && s.maintenanceEnabled !== undefined)
          payload.maintenanceEnabled = !!s.maintenanceEnabled;
        if (s.maintenanceMessage !== null && s.maintenanceMessage !== undefined)
          payload.maintenanceMessage = s.maintenanceMessage;
        if (s.smsEnabled !== null && s.smsEnabled !== undefined) payload.smsEnabled = !!s.smsEnabled;
        if (s.emailEnabled !== null && s.emailEnabled !== undefined) payload.emailEnabled = !!s.emailEnabled;
        payload.faviconUrl = resolveAssetUrl(s.faviconUrl, OFFLINE_UI_CONFIG.faviconUrl);
        const nextAutoTheme =
          s.autoTheme !== null && s.autoTheme !== undefined ? !!s.autoTheme : (this.autoTheme as boolean);
        if (s.autoTheme !== null && s.autoTheme !== undefined) payload.autoTheme = nextAutoTheme;
        if (s.lightStartTime !== null && s.lightStartTime !== undefined) payload.lightStartTime = s.lightStartTime;
        if (s.darkStartTime !== null && s.darkStartTime !== undefined) payload.darkStartTime = s.darkStartTime;
        if (s.userAgreement !== null && s.userAgreement !== undefined) payload.userAgreement = s.userAgreement;
        if (s.privacyAgreement !== null && s.privacyAgreement !== undefined)
          payload.privacyAgreement = s.privacyAgreement;
        if (s.passwordMinLength !== null && s.passwordMinLength !== undefined)
          payload.passwordMinLength = s.passwordMinLength;
        if (s.passwordRequireUppercase !== null && s.passwordRequireUppercase !== undefined)
          payload.passwordRequireUppercase = !!s.passwordRequireUppercase;
        if (s.passwordRequireLowercase !== null && s.passwordRequireLowercase !== undefined)
          payload.passwordRequireLowercase = !!s.passwordRequireLowercase;
        if (s.passwordRequireSpecial !== null && s.passwordRequireSpecial !== undefined)
          payload.passwordRequireSpecial = !!s.passwordRequireSpecial;
        if (s.passwordAllowSequential !== null && s.passwordAllowSequential !== undefined)
          payload.passwordAllowSequential = !!s.passwordAllowSequential;

        if (s.showFooter !== null && s.showFooter !== undefined) payload.showFooter = !!s.showFooter;
        if (s.isSidebarCompact !== null && s.isSidebarCompact !== undefined)
          payload.isSidebarCompact = !!s.isSidebarCompact;
        if (s.showBreadcrumb !== null && s.showBreadcrumb !== undefined) payload.showBreadcrumb = !!s.showBreadcrumb;
        if (s.menuAutoCollapsed !== null && s.menuAutoCollapsed !== undefined)
          payload.menuAutoCollapsed = !!s.menuAutoCollapsed;
        if (!nextAutoTheme && s.mode !== null && s.mode !== undefined) payload.mode = s.mode;
        if (s.layout !== null && s.layout !== undefined) payload.layout = s.layout;
        if (s.splitMenu !== null && s.splitMenu !== undefined) payload.splitMenu = !!s.splitMenu;
        if (!nextAutoTheme && s.sideMode !== null && s.sideMode !== undefined) payload.sideMode = s.sideMode;
        if (s.isFooterAside !== null && s.isFooterAside !== undefined) payload.isFooterAside = !!s.isFooterAside;
        if (s.isSidebarFixed !== null && s.isSidebarFixed !== undefined) payload.isSidebarFixed = !!s.isSidebarFixed;
        if (s.isHeaderFixed !== null && s.isHeaderFixed !== undefined) payload.isHeaderFixed = !!s.isHeaderFixed;
        if (s.isUseTabsRouter !== null && s.isUseTabsRouter !== undefined)
          payload.isUseTabsRouter = !!s.isUseTabsRouter;
        if (s.showHeader !== null && s.showHeader !== undefined) payload.showHeader = !!s.showHeader;
        if (s.brandTheme !== null && s.brandTheme !== undefined) payload.brandTheme = s.brandTheme;

        if (Object.keys(payload).length > 0) {
          this.updateConfig(payload);
        }
      } catch (e) {
        console.error('Failed to load UI settings:', e);
        this.ensureOfflineAssetFallbacks();
      }
    },
    async loadWatermarkSetting() {
      const { request } = await import('@/utils/request');
      const s = await request.get<any>({ url: '/system/watermark' });
      if (s) {
        this.watermark = {
          enabled: !!s.enabled,
          type: s.type || 'text',
          content: s.content || '',
          imageUrl: s.imageUrl || '',
          opacity: s.opacity ?? 0.12,
          size: s.size ?? 120,
          gapX: s.gapX ?? 200,
          gapY: s.gapY ?? 200,
          rotate: s.rotate ?? 20,
        };
      }
    },
    async saveUiSetting(data: Partial<TState>) {
      const { request } = await import('@/utils/request');
      // 过滤掉非 UI 配置字段
      const payload: any = {};
      const allowedKeys = [
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
      ];

      allowedKeys.forEach((key) => {
        if (data[key as keyof TState] !== undefined) {
          payload[key] = data[key as keyof TState];
        }
      });

      if (Object.keys(payload).length > 0) {
        await request.post({ url: '/system/ui', data: payload });
      }
    },
    initAutoTheme() {
      if (this.autoThemeTimer) {
        clearInterval(this.autoThemeTimer);
        this.autoThemeTimer = null;
      }
      if (!this.autoTheme) return;

      this.checkAutoTheme();
      this.autoThemeTimer = setInterval(() => {
        this.checkAutoTheme();
      }, 60000); // 每分钟检查一次
    },
    checkAutoTheme() {
      if (!this.autoTheme) {
        if (this.autoThemeTimer) {
          clearInterval(this.autoThemeTimer);
          this.autoThemeTimer = null;
        }
        return;
      }

      const now = new Date();
      const currentMinutes = now.getHours() * 60 + now.getMinutes();

      const parseTime = (timeStr: string) => {
        const [hours, minutes] = timeStr.split(':').map(Number);
        return hours * 60 + minutes;
      };

      const lightStart = parseTime(this.lightStartTime || '06:00');
      const darkStart = parseTime(this.darkStartTime || '18:00');

      let targetMode: ModeType = 'light';

      if (lightStart < darkStart) {
        // 正常情况，例如 06:00 - 18:00 是白天
        if (currentMinutes >= lightStart && currentMinutes < darkStart) {
          targetMode = 'light';
        } else {
          targetMode = 'dark';
        }
      } else {
        // 跨天情况，例如 18:00 - 06:00 是白天（虽然不常见但逻辑要严谨）
        if (currentMinutes >= lightStart || currentMinutes < darkStart) {
          targetMode = 'light';
        } else {
          targetMode = 'dark';
        }
      }

      // 检查当前 DOM 状态，确保与 store 状态一致
      const currentAttr = document.documentElement.getAttribute('theme-mode');
      const isDomDark = currentAttr === 'dark';
      const isTargetDark = targetMode === 'dark';

      if (this.mode !== targetMode || isDomDark !== isTargetDark) {
        this.updateConfig({ mode: targetMode });
      }
    },
    async changeMode(mode: ModeType | 'auto') {
      let theme = mode;

      if (mode === 'auto') {
        theme = this.getMediaColor();
      }
      const isDarkMode = theme === 'dark';

      if (isDarkMode) {
        document.documentElement.setAttribute('theme-mode', 'dark');
      } else {
        document.documentElement.removeAttribute('theme-mode');
      }

      this.chartColors = isDarkMode ? DARK_CHART_COLORS : LIGHT_CHART_COLORS;
    },
    async changeSideMode(mode: ModeType) {
      const isDarkMode = mode === 'dark';

      if (isDarkMode) {
        document.documentElement.setAttribute('side-mode', 'dark');
      } else {
        document.documentElement.removeAttribute('side-mode');
      }
    },
    getMediaColor() {
      const media = window.matchMedia('(prefers-color-scheme:dark)');

      if (media.matches) {
        return 'dark';
      }
      return 'light';
    },
    changeBrandTheme(brandTheme: string) {
      const mode = this.displayMode;
      // 以主题色加显示模式作为键
      const colorKey = `${brandTheme}[${mode}]`;
      let colorMap = this.colorList[colorKey];
      // 如果不存在色阶，就需要计算
      if (colorMap === undefined) {
        const [{ colors: newPalette, primary: brandColorIndex }] = Color.getColorGradations({
          colors: [brandTheme],
          step: 10,
          remainInput: false, // 是否保留输入 不保留会矫正不合适的主题色
        });
        colorMap = generateColorMap(brandTheme, newPalette, mode, brandColorIndex);
        this.colorList[colorKey] = colorMap;
      }
      // TODO 需要解决不停切换时有反复插入 style 的问题
      insertThemeStylesheet(brandTheme, colorMap, mode);
      document.documentElement.setAttribute('theme-color', brandTheme);
    },
    updateConfig(payload: Partial<TState>) {
      const merged: Partial<TState> = { ...payload };
      const hasMode = merged.mode !== undefined;
      const hasSideMode = merged.sideMode !== undefined;
      const modeChanged = hasMode && merged.mode !== this.mode;
      const sideModeChanged = hasSideMode && merged.sideMode !== this.sideMode;
      const deriveSideMode = (mode: ModeType | 'auto' | undefined) =>
        mode === 'auto' ? this.getMediaColor() : (mode as ModeType);

      if (modeChanged && !sideModeChanged) {
        merged.sideMode = deriveSideMode(merged.mode);
      } else if (sideModeChanged && !modeChanged) {
        merged.mode = merged.sideMode as ModeType;
      } else if (modeChanged && sideModeChanged) {
        merged.sideMode = deriveSideMode(merged.mode);
      } else if (hasMode && !hasSideMode) {
        merged.sideMode = deriveSideMode(merged.mode);
      } else if (hasSideMode && !hasMode) {
        merged.mode = merged.sideMode as ModeType;
      }

      for (const key in merged) {
        if (merged[key as TStateKey] !== undefined) {
          this[key as TStateKey] = merged[key as TStateKey];
        }
        if (key === 'mode') {
          this.changeMode(merged[key] as ModeType);
        }
        if (key === 'sideMode') {
          this.changeSideMode(merged[key] as ModeType);
        }
        if (key === 'brandTheme') {
          this.changeBrandTheme(merged[key]);
        }
        if (key === 'autoTheme' || key === 'lightStartTime' || key === 'darkStartTime') {
          this.initAutoTheme();
        }
      }
    },
  },
  persist: {
    paths: [...keys(STYLE_CONFIG), 'colorList', 'chartColors', 'uiRefreshSeq'],
  },
});

export function getSettingStore() {
  return useSettingStore(store);
}
