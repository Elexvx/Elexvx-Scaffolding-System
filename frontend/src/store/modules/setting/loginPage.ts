import OFFLINE_UI_CONFIG from '@/config/offline';

type LoginPageStoreLike = {
  footerCompany: string;
  footerIcp: string;
  logoExpandedUrl: string;
  logoCollapsedUrl: string;
  loginBgUrl: string;
  faviconUrl: string;
  updateConfig: (payload: Record<string, any>) => void;
};

export const resolveAssetUrl = (value: unknown, fallback: string) => {
  if (typeof value === 'string' && value.trim()) return value;
  return fallback;
};

export const ensureOfflineAssetFallbacks = (store: LoginPageStoreLike) => {
  store.updateConfig({
    logoExpandedUrl: resolveAssetUrl(store.logoExpandedUrl, OFFLINE_UI_CONFIG.logoExpandedUrl),
    logoCollapsedUrl: resolveAssetUrl(store.logoCollapsedUrl, OFFLINE_UI_CONFIG.logoCollapsedUrl),
    loginBgUrl: resolveAssetUrl(store.loginBgUrl, OFFLINE_UI_CONFIG.loginBgUrl),
    faviconUrl: resolveAssetUrl(store.faviconUrl, OFFLINE_UI_CONFIG.faviconUrl),
  });
};

export const applyMockUiSetting = (store: LoginPageStoreLike) => {
  const footerCompany = typeof store.footerCompany === 'string' && store.footerCompany.trim() ? store.footerCompany : '宏翔商道';
  const footerIcp = typeof store.footerIcp === 'string' ? store.footerIcp : '';
  store.updateConfig({
    websiteName: OFFLINE_UI_CONFIG.websiteName,
    copyrightStartYear: OFFLINE_UI_CONFIG.copyrightStartYear,
    logoExpandedUrl: OFFLINE_UI_CONFIG.logoExpandedUrl,
    logoCollapsedUrl: OFFLINE_UI_CONFIG.logoCollapsedUrl,
    faviconUrl: OFFLINE_UI_CONFIG.faviconUrl,
    loginBgUrl: OFFLINE_UI_CONFIG.loginBgUrl,
    footerCompany,
    footerIcp,
  });
};
