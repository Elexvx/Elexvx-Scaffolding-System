<template>
  <div class="login-wrapper">
    <login-header />

    <div class="login-left-container">
      <div class="title-container">
        <h1 class="title margin-no">{{ t('pages.login.loginTitle') }}</h1>
        <h1 class="title">{{ displayName }}</h1>
      </div>

      <login v-if="type === 'login'" @register="switchType('register')" @forgot="switchType('forgot')" />
      <register v-else-if="type === 'register'" @register-success="switchType('login')" @back="switchType('login')" />
      <forgot-password v-else @back="switchType('login')" @reset-success="switchType('login')" />
      <tdesign-setting />
      <div v-if="copyrightText || icp" class="copyright">
        <div v-if="copyrightText">{{ copyrightText }}</div>
        <div v-if="icp">
          <a href="https://beian.miit.gov.cn/" target="_blank">{{ icp }}</a>
        </div>
      </div>
    </div>

    <div class="login-right-container">
      <div class="login-bg-image" :style="bgStyle"></div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import defaultLoginBg from '@/assets/logo/background.svg?url';
import TdesignSetting from '@/layouts/setting.vue';
import { t } from '@/locales';
import { useSettingStore } from '@/store';
import { migrateSessionStorageKey } from '@/utils/storage/compat';

import ForgotPassword from './components/ForgotPassword.vue';
import LoginHeader from './components/Header.vue';
import Login from './components/Login.vue';
import Register from './components/Register.vue';

defineOptions({
  name: 'LoginIndex',
});
const route = useRoute();
const router = useRouter();
const UNAUTHORIZED_NOTICE_KEY = 'elexvx.auth.invalid.notice';
const LEGACY_UNAUTHORIZED_NOTICE_KEY = 'tdesign.auth.invalid.notice';
const type = computed(() => {
  if (route.path === '/register') return 'register';
  if (route.path === '/forgot') return 'forgot';
  return 'login';
});
const settingStore = useSettingStore();
const maintenanceDialogShown = ref(false);
const displayName = computed(() => settingStore.websiteName?.trim() || '管理后台');
const websiteName = computed(() => settingStore.websiteName?.trim() || '');
const company = computed(() => settingStore.footerCompany?.trim() || websiteName.value || '管理后台');
const icp = computed(() => settingStore.footerIcp || '');
const startYear = computed(() => settingStore.copyrightStartYear?.trim() || '');
const copyrightText = computed(() => {
  if (!company.value) return '';
  const currentYear = new Date().getFullYear();
  const normalizedStart = startYear.value || `${currentYear}`;
  const yearText = Number(normalizedStart) === currentYear ? `${currentYear}` : `${normalizedStart}-${currentYear}`;
  return `Copyright © ${yearText} ${company.value}. All Rights Reserved`;
});

onMounted(async () => {
  migrateSessionStorageKey(UNAUTHORIZED_NOTICE_KEY, [LEGACY_UNAUTHORIZED_NOTICE_KEY]);
  const unauthorizedNotice = sessionStorage.getItem(UNAUTHORIZED_NOTICE_KEY);
  if (unauthorizedNotice) {
    sessionStorage.removeItem(UNAUTHORIZED_NOTICE_KEY);
    MessagePlugin.warning(unauthorizedNotice);
  }

  await settingStore.loadUiSetting();
  if (settingStore.maintenanceEnabled && !maintenanceDialogShown.value) {
    maintenanceDialogShown.value = true;
    const message = settingStore.maintenanceMessage?.trim() || '系统正在维护中，具体恢复时间请关注通知。';
    DialogPlugin.alert({
      header: '维护提示',
      body: message,
      confirmBtn: '我知道了',
    });
  }
});

const switchType = (val: 'login' | 'register' | 'forgot') => {
  const target = val === 'register' ? '/register' : val === 'forgot' ? '/forgot' : '/login';
  if (route.path !== target) {
    router.push({ path: target, query: route.query });
  }
};

const bgStyle = computed(() => {
  if (settingStore.loginBgUrl) {
    return {
      backgroundImage: `url(${settingStore.loginBgUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    };
  }
  return {
    backgroundImage: `url(${defaultLoginBg})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  };
});
</script>
<style lang="less" scoped>
@import './index.less';
</style>
