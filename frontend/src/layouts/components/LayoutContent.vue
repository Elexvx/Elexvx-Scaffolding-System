<template>
  <t-layout :class="layoutCls">
    <div v-if="settingStore.isUseTabsRouter" :class="`${prefix}-layout-tabs`">
      <t-tabs
        drag-sort
        theme="card"
        :class="`${prefix}-layout-tabs-nav`"
        :value="currentTabValue"
        @change="(value) => handleChangeCurrentTab(value as string)"
        @remove="handleRemove"
        @drag-sort="handleDragend"
      >
        <t-tab-panel
          v-for="(routeItem, index) in tabRouters"
          :key="getTabValue(routeItem)"
          :value="getTabValue(routeItem)"
          :removable="!routeItem.isHome"
          :draggable="!routeItem.isHome"
        >
          <template #label>
            <t-dropdown
              trigger="context-menu"
              :min-column-width="128"
              :popup-props="{
                overlayClassName: 'route-tabs-dropdown',
                onVisibleChange: (visible: boolean, ctx: PopupVisibleChangeContext) =>
                  handleTabMenuClick(visible, ctx, routeItem.path),
              }"
              :visible="activeTabPath === getTabValue(routeItem)"
            >
              <template v-if="!routeItem.isHome">
                {{ renderTitle(routeItem.title) }}
              </template>
              <t-icon v-else name="home" />
              <template #dropdown>
                <t-dropdown-menu>
                  <t-dropdown-item @click="() => handleRefresh(routeItem, index)">
                    <t-icon name="refresh" />
                    {{ t('layout.tagTabs.refresh') }}
                  </t-dropdown-item>
                  <t-dropdown-item v-if="index > 1" @click="() => handleCloseAhead(routeItem.path, index)">
                    <t-icon name="arrow-left" />
                    {{ t('layout.tagTabs.closeLeft') }}
                  </t-dropdown-item>
                  <t-dropdown-item
                    v-if="index < tabRouters.length - 1"
                    @click="() => handleCloseBehind(routeItem.path, index)"
                  >
                    <t-icon name="arrow-right" />
                    {{ t('layout.tagTabs.closeRight') }}
                  </t-dropdown-item>
                  <t-dropdown-item v-if="tabRouters.length > 2" @click="() => handleCloseOther(routeItem.path, index)">
                    <t-icon name="close-circle" />
                    {{ t('layout.tagTabs.closeOther') }}
                  </t-dropdown-item>
                </t-dropdown-menu>
              </template>
            </t-dropdown>
          </template>
        </t-tab-panel>
      </t-tabs>
    </div>
    <t-content :class="`${prefix}-content-layout`">
      <l-breadcrumb v-if="settingStore.showBreadcrumb" />
      <l-content />
    </t-content>
    <t-footer v-if="settingStore.showFooter" :class="`${prefix}-footer-layout`">
      <l-footer />
    </t-footer>
  </t-layout>
</template>
<script setup lang="ts">
import type { PopupVisibleChangeContext } from 'tdesign-vue-next';
import { computed, nextTick, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { prefix } from '@/config/global';
import { t } from '@/locales';
import { useLocale } from '@/locales/useLocale';
import { useSettingStore, useTabsRouterStore } from '@/store';
import { normalizeTabPath } from '@/store/modules/tabs-router';
import type { TRouterInfo, TTabRemoveOptions } from '@/types/interface';
import { resolveRouteTitle } from '@/utils/routeTitle';

import LBreadcrumb from './Breadcrumb.vue';
import LContent from './Content.vue';
import LFooter from './Footer.vue';

const route = useRoute();
const router = useRouter();

const settingStore = useSettingStore();
const tabsRouterStore = useTabsRouterStore();
const tabRouters = computed(() => tabsRouterStore.tabRouters);
const activeTabPath = ref<string | null>(null);
const routeChanging = ref(false);
const suppressTabChange = ref(false);
const tabNavigating = ref(false);

const { locale } = useLocale();

const layoutCls = computed(() => [
  `${prefix}-layout`,
  { [`${prefix}-layout-with-fixed-header`]: settingStore.isHeaderFixed && settingStore.showHeader },
]);

const normalizeTabValue = (path: string) => normalizeTabPath(path);
const getTabValue = (tab: TRouterInfo) => normalizeTabValue(tab.path);

// 缓存上次有效的 tab 值，防止同步期间 computed 返回不稳定的值
const lastValidTabValue = ref('');

const resolvedTabValue = computed(() => {
  const values = tabRouters.value.map((t) => getTabValue(t));
  const current = normalizeTabValue(router.currentRoute.value.fullPath);
  if (values.includes(current)) {
    return current;
  }
  if (lastValidTabValue.value && values.includes(lastValidTabValue.value)) {
    return lastValidTabValue.value;
  }
  return values[0] ?? normalizeTabValue(route.path);
});

watch(
  [resolvedTabValue, () => tabsRouterStore.isSyncing, () => tabsRouterStore.isLocked],
  ([value, syncing, locked]) => {
    if (syncing || locked) return;
    if (value && value !== lastValidTabValue.value) {
      lastValidTabValue.value = value;
    }
  },
  { immediate: true },
);

watch(
  () => router.currentRoute.value.fullPath,
  (next, prev) => {
    if (next === prev) return;
    routeChanging.value = true;
    nextTick(() => {
      routeChanging.value = false;
    });
  },
);

const currentTabValue = computed(() => {
  // 在同步期间保持稳定值，避免触发递归更新
  if ((tabsRouterStore.isSyncing || tabsRouterStore.isLocked) && lastValidTabValue.value) {
    return lastValidTabValue.value;
  }
  return resolvedTabValue.value;
});

watch(
  currentTabValue,
  () => {
    suppressTabChange.value = true;
    nextTick(() => {
      suppressTabChange.value = false;
    });
  },
  { flush: 'sync' },
);

const handleChangeCurrentTab = async (rawValue: string) => {
  // 防止在权限同步期间的递归更新
  if (tabsRouterStore.isSyncing || tabsRouterStore.isLocked) {
    console.warn('Tab sync in progress, ignoring tab change');
    return;
  }
  if (tabNavigating.value) return;
  if (suppressTabChange.value) return;
  if (routeChanging.value) return;

  const path = normalizeTabValue(rawValue);
  if (import.meta.env.DEV) {
    console.debug('[tabs] change', {
      rawValue,
      path,
      currentTabValue: currentTabValue.value,
      currentRoute: router.currentRoute.value.fullPath,
    });
  }
  if (path === normalizeTabValue(currentTabValue.value)) return;
  if (path === normalizeTabValue(router.currentRoute.value.fullPath)) return;
  const tab = tabsRouterStore.tabRouters.find((i) => getTabValue(i) === path);

  // 确保有效的 tab 存在
  if (!tab) return;

  const target = router.resolve({ path, query: tab?.query });
  if (target.fullPath === router.currentRoute.value.fullPath) return;

  try {
    if (import.meta.env.DEV) {
      console.debug('[tabs] navigate', { path, query: tab?.query });
    }
    tabNavigating.value = true;
    await router.push({ path, query: tab?.query });
  } catch (error) {
    // 路由导航被拦截或其他异常，继续执行
    console.warn('Navigation error:', error);
  } finally {
    await nextTick();
    tabNavigating.value = false;
  }
};

const handleRemove = (options: TTabRemoveOptions) => {
  const { tabRouters } = tabsRouterStore;
  const nextRouter = tabRouters[options.index + 1] || tabRouters[options.index - 1];

  tabsRouterStore.subtractCurrentTabRouter({ path: options.value as string, routeIdx: options.index });
  if (normalizeTabValue(options.value as string) === normalizeTabValue(route.path)) {
    router.push({ path: getTabValue(nextRouter), query: nextRouter.query });
  }
};

const renderTitle = (title?: string | Record<string, string>) => {
  return resolveRouteTitle(title, locale.value, '');
};
const handleRefresh = (route: TRouterInfo, _routeIdx: number) => {
  const tabPath = getTabValue(route);
  tabsRouterStore.beginTabRefresh(tabPath);
  nextTick(() => {
    tabsRouterStore.endTabRefresh(tabPath);
    router.replace({ path: tabPath, query: route.query });
  });
  activeTabPath.value = null;
};
const handleCloseAhead = (path: string, routeIdx: number) => {
  tabsRouterStore.subtractTabRouterAhead({ path, routeIdx });

  handleOperationEffect('ahead', routeIdx);
};
const handleCloseBehind = (path: string, routeIdx: number) => {
  tabsRouterStore.subtractTabRouterBehind({ path, routeIdx });

  handleOperationEffect('behind', routeIdx);
};
const handleCloseOther = (path: string, routeIdx: number) => {
  tabsRouterStore.subtractTabRouterOther({ path, routeIdx });

  handleOperationEffect('other', routeIdx);
};

// 处理非当前路由操作的副作用
const handleOperationEffect = (type: 'other' | 'ahead' | 'behind', routeIndex: number) => {
  const currentPath = router.currentRoute.value.path;
  const { tabRouters } = tabsRouterStore;

  const currentIdx = tabRouters.findIndex((i) => getTabValue(i) === currentPath);
  // 存在三种情况需要刷新当前路由
  // 点击非当前路由的关闭其他、点击非当前路由的关闭左侧且当前路由小于触发路由、点击非当前路由的关闭右侧且当前路由大于触发路由
  const needRefreshRouter =
    (type === 'other' && currentIdx !== routeIndex) ||
    (type === 'ahead' && currentIdx < routeIndex) ||
    (type === 'behind' && currentIdx === -1);
  if (needRefreshRouter) {
    const nextRouteIdx = type === 'behind' ? tabRouters.length - 1 : 1;
    const nextRouter = tabRouters[nextRouteIdx];
    router.push({ path: getTabValue(nextRouter), query: nextRouter.query });
  }

  activeTabPath.value = null;
};
const handleTabMenuClick = (visible: boolean, ctx: PopupVisibleChangeContext, path: string) => {
  if (ctx.trigger === 'document') activeTabPath.value = null;
  if (visible) activeTabPath.value = normalizeTabValue(path);
};

const handleDragend = (options: { currentIndex: number; targetIndex: number }) => {
  tabsRouterStore.reorderTabRouters(options.currentIndex, options.targetIndex);
};
</script>
