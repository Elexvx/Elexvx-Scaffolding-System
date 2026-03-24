import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';

import defaultLoginBg from '@/assets/logo/background.svg?url';
import { useSettingStore } from '@/store';
import { migrateSessionStorageKey } from '@/utils/storage/compat';

import type { LoginPanelType } from '../types';

const UNAUTHORIZED_NOTICE_KEY = 'elexvx.auth.invalid.notice';
const LEGACY_UNAUTHORIZED_NOTICE_KEY = 'tdesign.auth.invalid.notice';

export function useLoginPageState() {
  const route = useRoute();
  const router = useRouter();
  const settingStore = useSettingStore();
  const maintenanceDialogShown = ref(false);

  const panelType = computed<LoginPanelType>(() => {
    if (route.path === '/register') return 'register';
    if (route.path === '/forgot') return 'forgot';
    return 'login';
  });
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
  const bgStyle = computed(() => ({
    backgroundImage: `url(${settingStore.loginBgUrl || defaultLoginBg})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  }));

  const switchType = (value: LoginPanelType) => {
    const target = value === 'register' ? '/register' : value === 'forgot' ? '/forgot' : '/login';
    if (route.path !== target) {
      void router.push({ path: target, query: route.query });
    }
  };

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
      DialogPlugin.alert({
        header: '维护提示',
        body: settingStore.maintenanceMessage?.trim() || '系统正在维护中，具体恢复时间请关注通知。',
        confirmBtn: '我知道了',
      });
    }
  });

  return { bgStyle, copyrightText, displayName, icp, panelType, switchType };
}
