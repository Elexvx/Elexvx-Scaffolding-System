<template>
  <t-form :data="form" label-width="120px" layout="vertical" label-align="right" @submit="onSubmit">
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12">
        <t-form-item label="版本号" name="appVersion">
          <t-input
            v-model="form.appVersion"
            placeholder="如：v1.2.3 或 2025.12"
            style="max-width: 500px; width: 100%"
          />
        </t-form-item>
      </t-col>
      <t-col :span="24">
        <t-form-item>
          <t-space :size="12">
            <t-button theme="primary" :disabled="!isAdmin" @click="onSubmit">保存</t-button>
          </t-space>
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>
</template>
<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive } from 'vue';

import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

const form = reactive({ appVersion: '' });

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  form.appVersion = s.appVersion || '';
};

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const onSubmit = async () => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  await request.post({ url: '/system/ui', data: { appVersion: form.appVersion } });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

onMounted(() => {
  load();
});
</script>
<style scoped></style>
