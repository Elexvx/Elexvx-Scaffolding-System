import { defineStore } from 'pinia';

import { store } from '@/store';
import type { ModeType } from '@/types/interface';

import { settingPersistConfig, UI_ALLOWED_SAVE_KEYS } from './persistence';
import { applyMockUiSetting, ensureOfflineAssetFallbacks } from './loginPage';
import { normalizeLayoutPayload } from './layout';
import { runSettingMigration } from './migration';
import { isEmptyUiSetting, mapUiSettingToStatePayload } from './personalize';
import { changeBrandTheme, changeMode, changeSideMode, checkAutoTheme, getMediaColor, initAutoTheme } from './theme';
import { createSettingInitialState, type SettingState, type SettingStateKey } from './types';

const initialState = createSettingInitialState();

export type TState = SettingState;
export type TStateKey = SettingStateKey;

export const useSettingStore = defineStore('setting', {
  state: () => ({ ...initialState }),
  getters: {
    isSideLayout: (state) => state.layout === 'side',
    showSidebar: (state) => state.layout === 'side' || state.layout === 'mix',
    showSidebarLogo: (state) => state.layout === 'side',
    showHeaderLogo: (state) => state.layout !== 'side',
    displayMode: (state): ModeType => {
      if (state.mode === 'auto') return getMediaColor();
      return state.mode as ModeType;
    },
    displaySideMode: (state): ModeType => {
      if (state.mode === 'auto') return getMediaColor();
      return state.mode as ModeType;
    },
  },
  actions: {
    ensureOfflineAssetFallbacks() {
      ensureOfflineAssetFallbacks(this);
    },
    applyMockUiSetting() {
      applyMockUiSetting(this);
    },
    async loadUiSetting() {
      const { request } = await import('@/utils/request');
      const { useUserStore } = await import('@/store');
      const userStore = useUserStore();
      try {
        const fetchUiSetting = async (endpoint: string, withToken: boolean) => request.get<any>({ url: endpoint }, { withToken });
        const endpoint = userStore.token ? '/system/ui' : '/system/ui/public';
        let s: any;
        try {
          s = await fetchUiSetting(endpoint, !!userStore.token);
        } catch (error: any) {
          const msg = String(error?.message || '');
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
        if (isEmptyUiSetting(s)) {
          this.applyMockUiSetting();
          return;
        }
        const payload = mapUiSettingToStatePayload(s, this.autoTheme as boolean);
        if (s.defaultHome !== null && s.defaultHome !== undefined) {
          const { useTabsRouterStore } = await import('../tabs-router');
          const tabsRouterStore = useTabsRouterStore();
          tabsRouterStore.updateHomeTab(s.defaultHome);
        }
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
      if (!s) return;
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
    },
    async saveUiSetting(data: Partial<TState>) {
      const { request } = await import('@/utils/request');
      const payload: Record<string, any> = {};
      UI_ALLOWED_SAVE_KEYS.forEach((key) => {
        if (data[key as keyof TState] !== undefined) {
          payload[key] = data[key as keyof TState];
        }
      });
      if (Object.keys(payload).length > 0) {
        await request.post({ url: '/system/ui', data: payload });
      }
    },
    initAutoTheme() {
      initAutoTheme(this as any);
    },
    checkAutoTheme() {
      checkAutoTheme(this as any);
    },
    async changeMode(mode: ModeType | 'auto') {
      this.chartColors = changeMode(mode);
    },
    async changeSideMode(mode: ModeType) {
      changeSideMode(mode);
    },
    getMediaColor() {
      return getMediaColor();
    },
    changeBrandTheme(brandTheme: string) {
      changeBrandTheme(this as any, brandTheme);
    },
    updateConfig(payload: Partial<TState>) {
      const merged = normalizeLayoutPayload(this as any, payload as Record<string, any>) as Partial<TState>;
      for (const key in merged) {
        if (merged[key as TStateKey] !== undefined) {
          (this as any)[key] = merged[key as TStateKey];
        }
        if (key === 'mode') {
          this.changeMode(merged[key] as ModeType);
        }
        if (key === 'sideMode') {
          this.changeSideMode(merged[key] as ModeType);
        }
        if (key === 'brandTheme') {
          this.changeBrandTheme(merged[key] as string);
        }
        if (key === 'autoTheme' || key === 'lightStartTime' || key === 'darkStartTime') {
          this.initAutoTheme();
        }
      }
    },
  },
  persist: settingPersistConfig,
});

export function getSettingStore() {
  return useSettingStore(store);
}

runSettingMigration();
