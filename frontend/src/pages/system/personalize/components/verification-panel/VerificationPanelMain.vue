<template>
  <t-tabs v-model="activeTab">
    <t-tab-panel value="sms" label="短信验证码设置" :destroy-on-hide="false">
      <verification-sms-section
        :form="smsForm"
        :provider-options="smsProviderOptions"
        :readonly="smsReadonly"
        :show-aliyun="showAliyun"
        :show-tencent="showTencent"
        :can-update="canUpdate"
        @submit="onSubmitSms"
      />
    </t-tab-panel>
    <t-tab-panel value="email" label="邮箱验证码设置" :destroy-on-hide="false">
      <verification-email-section :form="emailForm" :readonly="emailReadonly" :can-update="canUpdate" @submit="onSubmitEmail" />
    </t-tab-panel>
  </t-tabs>
</template>

<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useSettingStore } from '@/store';
import { request } from '@/utils/request';

import { useVerificationPanelForm } from './hooks/useVerificationPanelForm';
import { useVerificationPanelPermissions } from './hooks/useVerificationPanelPermissions';
import { useVerificationSections } from './hooks/useVerificationSections';
import VerificationEmailSection from './VerificationEmailSection.vue';
import VerificationSmsSection from './VerificationSmsSection.vue';
import { resolveVerificationTab } from './utils/verificationMappers';

const route = useRoute();
const router = useRouter();
const settingStore = useSettingStore();
const activeTab = ref(resolveVerificationTab(route.query.tab));
const { smsForm, emailForm, load, buildEmailPayload, buildSmsPayload } = useVerificationPanelForm();
const { canUpdate, ensureCanUpdate } = useVerificationPanelPermissions();
const { emailReadonly, showAliyun, showTencent, smsProviderDict, smsProviderOptions, smsReadonly } =
  useVerificationSections(smsForm, emailForm);

watch(
  () => route.query.tab,
  (value) => {
    const next = resolveVerificationTab(value);
    if (next !== activeTab.value) activeTab.value = next;
  },
);
watch(
  activeTab,
  (value) => {
    if (route.query.tab === value) return;
    router.replace({ query: { ...route.query, tab: value } }).catch(() => {});
  },
  { immediate: true },
);

const isInvalidSubmit = (ctx: any) => ctx && typeof ctx === 'object' && 'validateResult' in ctx && ctx.validateResult !== true;

const onSubmitSms = async (ctx?: any) => {
  if (!ensureCanUpdate()) {
    MessagePlugin.warning('No permission to update verification settings.');
    return;
  }
  if (isInvalidSubmit(ctx)) return;
  await request.post({ url: '/system/ui', data: buildSmsPayload() });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

const onSubmitEmail = async (ctx?: any) => {
  if (!ensureCanUpdate()) {
    MessagePlugin.warning('No permission to update verification settings.');
    return;
  }
  if (isInvalidSubmit(ctx)) return;
  await request.post({ url: '/system/ui', data: buildEmailPayload() });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

onMounted(() => {
  void smsProviderDict.load();
  void load();
});
</script>
