<template>
  <div :class="`${prefix}-wrapper`">
    <template v-if="setting.layout.value === 'side'">
      <t-layout key="side" :class="mainLayoutCls">
        <t-aside :width="settingStore.isSidebarCompact ? 'var(--td-starter-side-compact-width)' : 'var(--td-starter-side-width)'"><layout-side-nav /></t-aside>
        <t-layout :class="`${prefix}-main-layout`">
          <t-header v-if="settingStore.showHeader" :class="headerCls"><layout-header /></t-header>
          <t-content :class="`${prefix}-main-content`"><layout-content /></t-content>
        </t-layout>
      </t-layout>
    </template>

    <template v-else>
      <t-layout key="no-side">
        <t-header v-if="settingStore.showHeader" :class="headerCls"><layout-header /> </t-header>
        <t-layout :class="[mainLayoutCls, `${prefix}-main-layout`]">
          <layout-side-nav />
          <t-content :class="`${prefix}-main-content`"><layout-content /></t-content>
        </t-layout>
      </t-layout>
    </template>
  </div>
</template>
<script setup lang="ts">
import '@/style/layout.less';

import { storeToRefs } from 'pinia';
import { computed, watch } from 'vue';
import { useRoute } from 'vue-router';

import { prefix } from '@/config/global';
import { useSettingStore, useTabsRouterStore } from '@/store';

import LayoutContent from './components/LayoutContent.vue';
import LayoutHeader from './components/LayoutHeader.vue';
import LayoutSideNav from './components/LayoutSideNav.vue';

const route = useRoute();
const settingStore = useSettingStore();
const tabsRouterStore = useTabsRouterStore();
const setting = storeToRefs(settingStore);

const mainLayoutCls = computed(() => [
  {
    't-layout--with-sider': settingStore.showSidebar,
  },
]);

const headerCls = computed(() => ({
  [`${prefix}-layout-header-fixed`]: settingStore.isHeaderFixed,
}));

const appendNewRoute = () => {
  if (tabsRouterStore.isSyncing || tabsRouterStore.isLocked) return;
  const { path, query, meta, name } = route;
  if (
    [
      'Result403',
      'Result404',
      'Result500',
      'ResultNetworkError',
      'ResultBrowserIncompatible',
      'ResultMaintenance',
    ].includes(name as string)
  ) {
    return;
  }
  const title = meta?.title ?? (typeof name === 'string' ? name : path);
  tabsRouterStore.appendTabRouterList({ path, query, title, name, isAlive: true, meta });
};

tabsRouterStore.normalizeTabRouterList();
appendNewRoute();

watch(
  () => route.fullPath,
  (newFullPath, oldFullPath) => {
    appendNewRoute();

    const newPath = String(newFullPath || '').split(/[?#]/)[0];
    const oldPath = String(oldFullPath || '').split(/[?#]/)[0];
    if (newPath === oldPath) return;

    const layoutEl = document.querySelector(`.${prefix}-layout`) as HTMLElement | null;
    layoutEl?.scrollTo({ top: 0, behavior: 'smooth' });
  },
);
</script>
<style lang="less" scoped></style>
