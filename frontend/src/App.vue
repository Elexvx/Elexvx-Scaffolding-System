<template>
  <t-config-provider :global-config="globalConfig">
    <watermark-overlay />
    <session-kickout-listener />
    <route-loading :visible="isRouteLoading" />
    <router-view :key="locale" :class="[mode]" />
  </t-config-provider>
</template>
<script setup lang="ts">
/**
 * 根组件：
 * - 注入 TDesign 全局配置（含 table 空状态统一渲染）
 * - 承载全局覆盖层：水印、会话踢下线监听、路由加载中遮罩
 * - 监听 storage/pageshow 等事件，保证“多标签页登录态”一致，并在必要时刷新页面
 * - 负责动态更新 document.title 与 favicon
 */
import { computed, h, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import emptyImage from '@/assets/assets-empty.svg?url';
import RouteLoading from '@/components/RouteLoading.vue';
import SessionKickoutListener from '@/components/SessionKickoutListener.vue';
import WatermarkOverlay from '@/components/WatermarkOverlay.vue';
import { t } from '@/locales';
import { useLocale } from '@/locales/useLocale';
import { useAppStore, useSettingStore, useUserStore } from '@/store';
import { resolveRouteTitle } from '@/utils/routeTitle';
import { getTokenStorageKey } from '@/utils/secureToken';
import { isLocalTokenExpired } from '@/utils/tokenExpire';

const store = useSettingStore();
const userStore = useUserStore();
const appStore = useAppStore();
const route = useRoute();
const router = useRouter();
let uiSyncTimer: number | null = null;
let authSyncing = false;
const authPages = new Set(['/login', '/register', '/forgot']);
const isAuthPage = (path: string) => authPages.has(path);

const mode = computed(() => {
  return store.displayMode;
});

const isRouteLoading = computed(() => appStore.routeLoading || !appStore.backendReady);

const { getComponentsLocale, locale } = useLocale();
const globalConfig = computed(() => {
  const baseConfig = getComponentsLocale.value as any;
  const emptyText = baseConfig?.table?.empty || '暂无数据';
  return {
    ...baseConfig,
    table: {
      ...(baseConfig?.table || {}),
      empty: () =>
        h('div', { class: 'tdesign-table-empty' }, [
          h('img', { src: emptyImage, alt: emptyText }),
          h('div', { class: 'tdesign-table-empty-text' }, emptyText),
        ]),
    },
  };
});

const loadSystemSettings = async () => {
  if (!userStore.token) return;
  await store.loadUiSetting();
  await store.loadWatermarkSetting();
};

const resolveLoginRedirect = (redirect?: string) => {
  const fallbackHome = store.defaultHome || '/user/index';
  if (!redirect) return fallbackHome;
  try {
    const decoded = decodeURIComponent(redirect);
    if ((decoded === '/' || decoded === '/user/index') && fallbackHome !== '/user/index') {
      return fallbackHome;
    }
    return decoded;
  } catch {
    return fallbackHome;
  }
};

const redirectFromAuthPage = (redirect?: string, forceReload = false) => {
  const redirectUrl = resolveLoginRedirect(redirect);
  if (forceReload) {
    window.location.replace(redirectUrl);
    return;
  }
  void router.replace(redirectUrl);
};

const handleStorageChange = async (event: StorageEvent) => {
  if (event.storageArea !== window.localStorage) return;
  const tokenKey = getTokenStorageKey();
  if (
    event.key !== 'elexvx.user' &&
    event.key !== 'user' &&
    event.key !== tokenKey &&
    event.key !== 'tdesign.auth.token'
  ) {
    return;
  }
  if (authSyncing) return;
  authSyncing = true;
  const prevToken = userStore.token;
  try {
    if (event.key === 'elexvx.user' || event.key === 'user') {
      userStore.$hydrate({ runHooks: true });
    }
    if (event.key === tokenKey || event.key === 'tdesign.auth.token') {
      await userStore.restoreTokenFromStorage();
    }
    const nextToken = userStore.token;
    if (prevToken === nextToken) return;

    if (nextToken && isAuthPage(route.path)) {
      if (isLocalTokenExpired()) return;
      const redirectValue = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect;
      redirectFromAuthPage(redirectValue as string | undefined, true);
    } else if (nextToken && !prevToken && !isAuthPage(route.path)) {
      if (isLocalTokenExpired()) return;
      window.location.reload();
    } else if (!nextToken && prevToken && !isAuthPage(route.path)) {
      router.replace({ path: '/login', query: { redirect: route.fullPath } });
    }
  } finally {
    authSyncing = false;
  }
};

const syncAuthFromCache = async (forceReload = false) => {
  if (authSyncing) return;
  authSyncing = true;
  if (typeof (userStore as any).$hydrate === 'function') {
    (userStore as any).$hydrate({ runHooks: true });
  }
  try {
    await userStore.restoreTokenFromStorage();
    if (userStore.token && isAuthPage(route.path)) {
      if (isLocalTokenExpired()) return;
      const redirectValue = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect;
      redirectFromAuthPage(redirectValue as string | undefined, forceReload);
    }
  } finally {
    authSyncing = false;
  }
};

const handlePageShow = (event: PageTransitionEvent) => {
  syncAuthFromCache(event.persisted);
};

const handleFormEnterSubmit = (event: KeyboardEvent) => {
  if (event.key !== 'Enter') return;
  const target = event.target as HTMLElement | null;
  if (!target) return;
  if (target.isContentEditable) return;
  const tagName = target.tagName?.toUpperCase?.() || '';
  if (tagName === 'TEXTAREA') return;
  if (!target.closest('form')) return;
  event.preventDefault();
};

const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    syncAuthFromCache();
  }
};

onMounted(async () => {
  await loadSystemSettings();
  store.initAutoTheme();
  window.addEventListener('storage', handleStorageChange);
  window.addEventListener('pageshow', handlePageShow);
  document.addEventListener('keydown', handleFormEnterSubmit);
  document.addEventListener('visibilitychange', handleVisibilityChange);
  const probe = async () => {
    try {
      const { request } = await import('@/utils/request');
      await request.get({ url: '/system/ui/public' }, { withToken: false, retry: { count: 0, delay: 0 } });
      appStore.setBackendReady(true);
      try {
        if (userStore.token || !isAuthPage(route.path)) {
          await store.loadUiSetting();
        }
      } catch {}

      const waitedKey = 'backend_waited_for_ready';
      const reloadedKey = 'backend_ready_reloaded_once';
      const waited = sessionStorage.getItem(waitedKey) === '1';
      const reloaded = sessionStorage.getItem(reloadedKey) === '1';
      const shouldReload = waited && !reloaded && isAuthPage(route.path) && !userStore.token;
      if (shouldReload) {
        sessionStorage.setItem(reloadedKey, '1');
        window.location.reload();
      }
    } catch {
      sessionStorage.setItem('backend_waited_for_ready', '1');
      setTimeout(probe, 1500);
    }
  };
  appStore.setBackendReady(false);
  probe();
  uiSyncTimer = window.setInterval(async () => {
    if (!userStore.token) return;
    try {
      await store.loadUiSetting();
      await store.loadWatermarkSetting();
    } catch (e) {
      console.warn('Failed to refresh system settings:', e);
    }
  }, 60000);
});

watch(
  () => userStore.token,
  async (token, prev) => {
    if (token && token !== prev) {
      await loadSystemSettings();
    }
  },
);

const updateTitle = () => {
  const pageTitle = route.meta?.title;
  const siteName = store.websiteName || 'Elexvx 脚手架系统';

  let titleContent = '';
  if (pageTitle) {
    if (typeof pageTitle === 'string') {
      titleContent = t(pageTitle);
    } else if (typeof pageTitle === 'object') {
      titleContent = resolveRouteTitle(pageTitle as Record<string, string>, locale.value, '');
    }
  }

  if (titleContent) {
    document.title = `${titleContent} - ${siteName}`;
  } else {
    document.title = siteName;
  }
};

const updateFavicon = () => {
  const favicon = store.faviconUrl;
  if (!favicon) return;
  let link = document.querySelector("link[rel*='icon']") as HTMLLinkElement;
  if (!link) {
    link = document.createElement('link');
    link.type = 'image/x-icon';
    link.rel = 'shortcut icon';
    document.getElementsByTagName('head')[0].appendChild(link);
  }
  link.href = favicon;
};

watch(
  () => [route.path, store.websiteName, store.faviconUrl, locale.value],
  () => {
    updateTitle();
    updateFavicon();
  },
  { immediate: true },
);

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener('pageshow', handlePageShow);
  document.removeEventListener('keydown', handleFormEnterSubmit);
  document.removeEventListener('visibilitychange', handleVisibilityChange);
  if (uiSyncTimer) {
    clearInterval(uiSyncTimer);
    uiSyncTimer = null;
  }
});
</script>
<style lang="less" scoped>
#nprogress .bar {
  background: var(--td-brand-color) !important;
}
</style>
