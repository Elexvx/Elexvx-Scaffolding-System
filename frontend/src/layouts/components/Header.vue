<template>
  <div :class="layoutCls">
    <t-head-menu :class="menuCls" :theme="menuTheme" expand-type="popup" :value="active">
      <template #logo>
        <span v-if="showLogo" class="header-logo-container" @click="handleNav('/')">
          <img v-if="logoUrl" :src="logoUrl" class="t-logo" />
          <span v-else class="t-logo-text">{{ logoText }}</span>
        </span>
        <div v-else class="header-operate-left">
          <t-button theme="default" shape="square" variant="text" @click="changeCollapsed">
            <t-icon class="collapsed-icon" name="view-list" />
          </t-button>
        </div>
      </template>
      <template v-if="layout !== 'side'" #default>
        <menu-content class="header-menu" :nav-data="menu" />
      </template>
      <template #operations>
        <div class="operations-container">
          <!-- 全局通知 -->
          <notice />
          <t-tag v-if="user.userInfo.roleSimulated" theme="warning" size="small" style="margin-right: 12px">
            角色模拟
          </t-tag>
          <t-tooltip placement="bottom" content="GitHub">
            <t-button theme="default" shape="square" variant="text" @click="openHeaderLink(headerGithubUrl, 'GitHub')">
              <t-icon name="logo-github" />
            </t-button>
          </t-tooltip>
          <t-tooltip placement="bottom" content="帮助文档">
            <t-button theme="default" shape="square" variant="text" @click="openHeaderLink(headerHelpUrl, '帮助')">
              <t-icon name="help-circle" />
            </t-button>
          </t-tooltip>
          <t-tooltip placement="bottom" :content="t('layout.setting.theme.mode')">
            <t-button theme="default" shape="square" variant="text" @click="toggleThemeMode">
              <t-icon :name="themeIcon" />
            </t-button>
          </t-tooltip>
          <t-dropdown :min-column-width="120" trigger="click">
            <template #dropdown>
              <t-dropdown-item v-if="canSwitchRole" class="operations-dropdown-container-item" @click="openRoleDialog">
                <chevron-down-icon />权限切换
              </t-dropdown-item>
              <t-dropdown-item class="operations-dropdown-container-item" @click="handleNav('/user')">
                <user-circle-icon />{{ t('layout.header.user') }}
              </t-dropdown-item>
              <t-dropdown-item class="operations-dropdown-container-item" @click="handleLogout">
                <poweroff-icon />{{ t('layout.header.signOut') }}
              </t-dropdown-item>
            </template>
            <t-avatar :image="user.userInfo.avatar" class="header-avatar-trigger">
              <template #icon><user-icon /></template>
            </t-avatar>
          </t-dropdown>
        </div>
      </template>
    </t-head-menu>
    <t-dialog
      v-model:visible="roleDialogVisible"
      header="切换权限"
      :confirm-btn="{ content: '应用配置', loading: roleSaving }"
      :cancel-btn="{ content: '取消', theme: 'default' }"
      width="520px"
      @confirm="applyRoleSwitch"
    >
      <t-loading :loading="roleLoading">
        <p class="mb-2">选择要模拟的角色，确认后当前会话的菜单、接口权限将按所选角色重新加载。</p>
        <t-form label-width="100px">
          <t-form-item label="角色列表">
            <t-select
              v-model="selectedRoles"
              :options="roleOptions"
              multiple
              placeholder="选择要模拟的角色"
              :disabled="!canSwitchRole"
            />
          </t-form-item>
        </t-form>
        <t-space align="center" style="margin-top: 24px">
          <t-button variant="outline" :disabled="!canSwitchRole" @click="resetRoleSwitch">恢复管理员权限</t-button>
          <t-button v-if="!roleLoading && !canSwitchRole" variant="outline" disabled>仅 admin 账号可切换</t-button>
        </t-space>
      </t-loading>
    </t-dialog>
  </div>
</template>
<script setup lang="ts">
import { ChevronDownIcon, PoweroffIcon, UserCircleIcon, UserIcon } from 'tdesign-icons-vue-next';
import type { SelectOption } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PropType } from 'vue';
import { computed, ref, toRefs } from 'vue';
import { useRouter } from 'vue-router';

import { prefix } from '@/config/global';
import { t } from '@/locales';
import { getActive } from '@/router';
import { usePermissionStore, useSettingStore, useTabsRouterStore, useUserStore } from '@/store';
import type { MenuRoute, ModeType } from '@/types/interface';
import { request } from '@/utils/request';

import MenuContent from './MenuContent.vue';
import Notice from './Notice.vue';

const props = defineProps({
  theme: {
    type: String,
    default: 'light',
  },
  layout: {
    type: String,
    default: 'top',
  },
  showLogo: {
    type: Boolean,
    default: true,
  },
  menu: {
    type: Array as PropType<MenuRoute[]>,
    default: () => [],
  },
  isFixed: {
    type: Boolean,
    default: false,
  },
  isCompact: {
    type: Boolean,
    default: false,
  },
  maxLevel: {
    type: Number,
    default: 3,
  },
});
const { theme, layout, showLogo, menu, isFixed, isCompact } = toRefs(props);

const router = useRouter();
const settingStore = useSettingStore();
const tabsRouterStore = useTabsRouterStore();
const permissionStore = usePermissionStore();
const user = useUserStore();
const active = computed(() => getActive());

const logoUrl = computed(() => settingStore.logoExpandedUrl);
const logoText = computed(() => settingStore.websiteName?.trim() || '管理后台');
const headerGithubUrl = computed(() => String(settingStore.headerGithubUrl || '').trim());
const headerHelpUrl = computed(() => String(settingStore.headerHelpUrl || '').trim());

const layoutCls = computed(() => [`${prefix}-header-layout`]);

const menuCls = computed(() => {
  return [
    {
      [`${prefix}-header-menu`]: !isFixed.value,
      [`${prefix}-header-menu-fixed`]: isFixed.value,
      [`${prefix}-header-menu-fixed-side`]: layout.value === 'side' && isFixed.value,
      [`${prefix}-header-menu-fixed-side-compact`]: layout.value === 'side' && isFixed.value && isCompact.value,
    },
  ];
});
const menuTheme = computed(() => theme.value as ModeType);

// 切换语言
const changeCollapsed = () => {
  settingStore.updateConfig({
    isSidebarCompact: !settingStore.isSidebarCompact,
  });
};

const handleNav = (url: string) => {
  router.push(url);
};

const handleLogout = async () => {
  await user.logout();
  window.location.href = `/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`;
};

const openHeaderLink = (url: string, label: string) => {
  const target = String(url || '').trim();
  if (!target) {
    MessagePlugin.warning(`请在个性化设置中配置${label}链接`);
    return;
  }
  if (typeof window === 'undefined') return;
  window.open(target, '_blank', 'noreferrer');
};

const roleDialogVisible = ref(false);
const roleLoading = ref(false);
const roleSaving = ref(false);
const selectedRoles = ref<string[]>([]);
const roleOptions = ref<SelectOption[]>([]);
const canSwitchRole = computed(() => (user.userInfo.assignedRoles || []).includes('admin'));

const loadRoleOptions = async () => {
  roleLoading.value = true;
  try {
    const roles = await request.get<Array<string>>({ url: '/auth/assume-role/options' });
    roleOptions.value = (roles || []).map((name) => ({ label: name, value: name }));
  } finally {
    roleLoading.value = false;
  }
};

const openRoleDialog = async () => {
  await loadRoleOptions();
  selectedRoles.value = user.userInfo.roleSimulated ? [...(user.userInfo.roles || [])] : [];
  roleDialogVisible.value = true;
};

const applyRoleSwitch = async () => {
  roleSaving.value = true;
  tabsRouterStore.setTabsLock(true);
  try {
    await request.post({
      url: '/auth/assume-role',
      data: { roles: selectedRoles.value },
    });
    await user.getUserInfo(true);
    await permissionStore.refreshAsyncRoutes(user.userInfo);
    const targetPath = settingStore.defaultHome || '/user/index';
    const resolved = router.resolve(targetPath);
    tabsRouterStore.resetTabRouterList({
      path: resolved.path || targetPath,
      query: resolved.query,
      title: resolved.meta?.title ?? (typeof resolved.name === 'string' ? resolved.name : resolved.path),
      name: resolved.name,
      meta: resolved.meta,
      isAlive: true,
    });
    await router.replace({ path: resolved.path || targetPath, query: resolved.query });
    MessagePlugin.success('已切换当前会话权限');
  } finally {
    roleSaving.value = false;
    roleDialogVisible.value = false;
    tabsRouterStore.setTabsLock(false);
  }
};

const resetRoleSwitch = async () => {
  selectedRoles.value = [];
  await applyRoleSwitch();
};

const themeIcon = computed(() => (settingStore.displayMode === 'light' ? 'moon' : 'sunny'));
const toggleThemeMode = async () => {
  const next = settingStore.displayMode === 'light' ? 'dark' : 'light';
  const payload = settingStore.autoTheme ? { mode: next, autoTheme: false } : { mode: next };

  settingStore.updateConfig(payload);
  if (!user.token) return;
  try {
    await settingStore.saveUiSetting(payload);
  } catch (error) {
    console.error('Failed to persist theme mode:', error);
  }
};
</script>
<style lang="less" scoped>
.@{starter-prefix}-header {
  &-menu-fixed {
    position: fixed;
    top: 0;
    z-index: 1001;
    height: var(--td-starter-header-height);

    :deep(.t-head-menu__inner) {
      height: var(--td-starter-header-height);
      padding-right: var(--td-comp-margin-xl);
    }

    &-side {
      left: var(--td-starter-side-width);
      right: 0;
      z-index: 10;
      width: auto;
      transition: left 0.3s ease;

      &-compact {
        left: var(--td-starter-side-compact-width);
      }
    }
  }

  &-logo-container {
    cursor: pointer;
    display: inline-flex;
  }
}

.header-menu {
  flex: 1 1 auto;
  display: inline-flex;

  :deep(.t-menu__item) {
    min-width: unset;
  }
}

.operations-container {
  display: flex;
  align-items: center;

  .t-popup__reference {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .t-button {
    margin-left: var(--td-comp-margin-l);
  }
}

.mb-2 {
  margin-bottom: 8px;
}

.header-operate-left {
  display: flex;
  align-items: normal;
  line-height: 0;
}

.header-logo-container {
  width: 184px;
  height: 26px;
  display: flex;
  margin-left: 24px;
  color: var(--td-text-color-primary);

  .t-logo {
    width: 100%;
    height: 100%;

    &:hover {
      cursor: pointer;
    }
  }

  .t-logo-text {
    display: inline-flex;
    align-items: center;
    font-size: 18px;
    font-weight: 600;
    width: 100%;
    height: 100%;
  }

  &:hover {
    cursor: pointer;
  }
}

.header-user-account {
  display: inline-flex;
  align-items: center;
  color: var(--td-text-color-primary);
}

.header-avatar-trigger {
  cursor: pointer;
  margin-left: var(--td-comp-margin-l);
}

.upload-item-inner {
  display: flex;
  align-items: center;
  width: 100%;

  .t-icon {
    margin-right: var(--td-comp-margin-s);
  }
}

:deep(.t-head-menu__inner) {
  border-bottom: 1px solid var(--td-component-stroke);
}

.t-menu--light {
  .header-user-account {
    color: var(--td-text-color-primary);
  }
}

.t-menu--dark {
  .t-head-menu__inner {
    border-bottom: 1px solid var(--td-gray-color-10);
  }

  .header-user-account {
    color: rgb(255 255 255 / 55%);
  }
}

.operations-dropdown-container-item {
  width: 100%;
  display: flex;
  align-items: center;

  :deep(.t-dropdown__item-text) {
    display: flex;
    align-items: center;
  }

  .t-icon {
    font-size: var(--td-comp-size-xxxs);
    margin-right: var(--td-comp-margin-s);
  }

  :deep(.t-dropdown__item) {
    width: 100%;
    margin-bottom: 0;
  }

  &:last-child {
    :deep(.t-dropdown__item) {
      margin-bottom: 8px;
    }
  }
}
</style>
<!-- eslint-disable-next-line vue-scoped-css/enforce-style-type -->
<style lang="less">
.operations-dropdown-container-item {
  .t-dropdown__item-text {
    display: flex;
    align-items: center;
  }
}
</style>
