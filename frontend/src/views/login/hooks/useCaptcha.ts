import { MessagePlugin } from 'tdesign-vue-next';
import { nextTick, onBeforeUnmount, reactive } from 'vue';

import { request } from '@/utils/request';

import { CAPTCHA_REFRESH_FALLBACK_MS, CAPTCHA_REFRESH_LEEWAY_MS } from '../constants/loginOptions';
import type { CaptchaResponse, CaptchaState, CaptchaType } from '../types';

export function useCaptcha(errorMessage: string) {
  const state = reactive<CaptchaState>({
    captchaId: '',
    captchaImage: '',
    captchaEnabled: true,
    captchaType: 'image',
    dragWidth: 310,
    dragHeight: 155,
    dragRefreshKey: 0,
    showDragCaptchaDialog: false,
    pendingDragSubmit: false,
  });

  let captchaRefreshTimer: number | null = null;

  const clearCaptchaRefreshTimer = () => {
    if (captchaRefreshTimer) {
      window.clearTimeout(captchaRefreshTimer);
      captchaRefreshTimer = null;
    }
  };

  const scheduleCaptchaRefresh = (expiresInSeconds?: number) => {
    clearCaptchaRefreshTimer();
    const seconds = typeof expiresInSeconds === 'number' && Number.isFinite(expiresInSeconds) ? expiresInSeconds : null;
    const ttl = (seconds != null ? seconds * 1000 : CAPTCHA_REFRESH_FALLBACK_MS) - CAPTCHA_REFRESH_LEEWAY_MS;
    captchaRefreshTimer = window.setTimeout(() => {
      captchaRefreshTimer = null;
      void loadCaptcha({ silent: true });
    }, Math.max(ttl, 10 * 1000));
  };

  const bumpDragCaptcha = () => {
    state.dragRefreshKey += 1;
  };

  const handleDragRefresh = () => {
    state.captchaId = '';
    state.captchaImage = '';
  };

  const handleDragSuccess = async (
    payload: { captchaVerification: string; token: string },
    onResolved?: () => void,
  ) => {
    state.showDragCaptchaDialog = false;
    state.captchaId = payload.token;
    if (onResolved) {
      await nextTick();
      onResolved();
    }
  };

  const openDragCaptcha = () => {
    if (state.captchaType !== 'drag') return;
    bumpDragCaptcha();
    state.showDragCaptchaDialog = true;
  };

  const loadCaptcha = async (opts?: { silent?: boolean }) => {
    try {
      const res = await request.get<CaptchaResponse>({ url: '/auth/captcha' }, { isTransformResponse: true, withToken: false });
      state.captchaEnabled = res.enabled !== false;
      if (!state.captchaEnabled) {
        handleDragRefresh();
        return;
      }
      state.captchaType = (res.type || 'image') as CaptchaType;
      state.dragWidth = res.width || 310;
      state.dragHeight = res.height || 155;
      if (state.captchaType === 'image') {
        state.captchaId = res.id;
        state.captchaImage = res.image;
      } else {
        handleDragRefresh();
        bumpDragCaptcha();
      }
      scheduleCaptchaRefresh(res.expiresIn);
    } catch (err: any) {
      scheduleCaptchaRefresh();
      if (!opts?.silent) {
        MessagePlugin.error(String(err?.message || errorMessage));
      }
    }
  };

  onBeforeUnmount(clearCaptchaRefreshTimer);

  return {
    captcha: state,
    clearCaptchaRefreshTimer,
    handleDragRefresh,
    handleDragSuccess,
    loadCaptcha,
    openDragCaptcha,
  };
}
