<template>
  <div :class="sideNavCls">
    <t-menu
      :class="menuCls"
      :theme="theme"
      :value="active"
      :collapsed="collapsed"
      :expanded="expanded"
      :expand-mutex="false"
      @expand="onExpanded"
    >
      <template #logo>
        <span v-if="showLogo" :class="`${prefix}-side-nav-logo-wrapper`" @click="goHome">
          <img v-if="customLogoUrl" :src="customLogoUrl" :class="logoCls" />
          <span v-else :class="logoCls">{{ logoText }}</span>
        </span>
      </template>
      <menu-content :nav-data="menu" />
      <template #operations>
        <span v-if="showVersion" :class="versionCls" :title="versionText">{{ versionText }}</span>
      </template>
    </t-menu>
    <div :class="`${prefix}-side-nav-placeholder${collapsed ? '-hidden' : ''}`"></div>
  </div>
</template>
<script setup lang="ts">
import { union } from 'lodash';
import type { MenuValue } from 'tdesign-vue-next';
import type { PropType } from 'vue';
import { computed, onMounted, onUnmounted, ref, toRefs, watch } from 'vue';
import { useRouter } from 'vue-router';

import { prefix } from '@/config/global';
import { getActive } from '@/router';
import { useSettingStore } from '@/store';
import type { MenuRoute, ModeType } from '@/types/interface';

import pgk from '../../../package.json';
import MenuContent from './MenuContent.vue';

const props = defineProps({
  menu: {
    type: Array as PropType<MenuRoute[]>,
    default: () => [],
  },
  showLogo: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  isFixed: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  layout: {
    type: String as PropType<string>,
    default: '',
  },
  headerHeight: {
    type: String as PropType<string>,
    default: '64px',
  },
  theme: {
    type: String as PropType<ModeType>,
    default: 'light',
  },
  isCompact: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
});
const { menu, showLogo, isFixed, layout, theme, isCompact } = toRefs(props);

const MIN_POINT = 1200 - 1;
const settingStore = useSettingStore();

const collapsed = computed(() => settingStore.isSidebarCompact);
const menuAutoCollapsed = computed(() => settingStore.menuAutoCollapsed);

const active = computed(() => getActive());

const expanded = ref<MenuValue[]>([]);

const getExpanded = () => {
  const path = getActive();
  const parts = path.split('/').slice(1);
  const result = parts.map((_, index) => `/${parts.slice(0, index + 1).join('/')}`);

  expanded.value = union(result, expanded.value);
};

watch(
  () => active.value,
  () => {
    getExpanded();
  },
);

const onExpanded = (value: MenuValue[]) => {
  expanded.value = value;
};

const sideMode = computed(() => theme.value === 'dark');
const sideNavCls = computed(() => {
  return [
    `${prefix}-sidebar-layout`,
    {
      [`${prefix}-sidebar-compact`]: isCompact.value,
    },
  ];
});
const logoCls = computed(() => {
  return [
    `${prefix}-side-nav-logo-${collapsed.value ? 't' : 'tdesign'}-logo`,
    {
      [`${prefix}-side-nav-dark`]: sideMode.value,
    },
  ];
});
const versionCls = computed(() => {
  return [
    `version-container`,
    {
      [`${prefix}-side-nav-dark`]: sideMode.value,
    },
  ];
});
const menuCls = computed(() => {
  return [
    `${prefix}-side-nav`,
    {
      [`${prefix}-side-nav-no-logo`]: !showLogo.value,
      [`${prefix}-side-nav-no-fixed`]: !isFixed.value,
      [`${prefix}-side-nav-mix-fixed`]: layout.value === 'mix' && isFixed.value,
    },
  ];
});

const router = useRouter();

const lastAutoCollapsed = ref<boolean | null>(null);
const autoCollapsed = () => {
  const forceCompact = window.innerWidth <= MIN_POINT;
  if (forceCompact) {
    if (!settingStore.isSidebarCompact) {
      settingStore.updateConfig({ isSidebarCompact: true });
    }
    lastAutoCollapsed.value = true;
    return;
  }

  if (!menuAutoCollapsed.value) {
    lastAutoCollapsed.value = null;
    return;
  }

  const isCompact = false;
  if (lastAutoCollapsed.value === isCompact) return;
  lastAutoCollapsed.value = isCompact;
  settingStore.updateConfig({ isSidebarCompact: isCompact });
};

watch(menuAutoCollapsed, () => {
  autoCollapsed();
});

onMounted(() => {
  getExpanded();
  autoCollapsed();

  window.addEventListener('resize', autoCollapsed);
});

onUnmounted(() => {
  window.removeEventListener('resize', autoCollapsed);
});

const goHome = () => {
  router.push(settingStore.defaultHome || '/user/index');
};

const logoText = computed(() => {
  const name = settingStore.websiteName?.trim() || '管理后台';
  if (!collapsed.value) return name;
  return name.slice(0, 1);
});

const customLogoUrl = computed(() => (collapsed.value ? settingStore.logoCollapsedUrl : settingStore.logoExpandedUrl));
const versionText = computed(() => settingStore.appVersion || pgk.version);
const showVersion = computed(() => !collapsed.value);
</script>
<style lang="less" scoped></style>
