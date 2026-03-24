<template>
  <t-form :data="form" label-width="120px" layout="vertical" label-align="right" @submit.prevent>
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12">
        <t-form-item label="公司名称" name="footerCompany">
          <t-input v-model="form.footerCompany" placeholder="如：某某公司" style="max-width: 500px; width: 100%" />
        </t-form-item>
      </t-col>
      <t-col :xs="24" :sm="12">
        <t-form-item label="网站名" name="websiteName">
          <t-input v-model="form.websiteName" placeholder="如：某某系统" style="max-width: 500px; width: 100%" />
        </t-form-item>
      </t-col>
      <t-col :xs="24" :sm="12">
        <t-form-item label="版权起始年份" name="copyrightStartYear">
          <t-input v-model="form.copyrightStartYear" placeholder="如：2021" style="max-width: 500px; width: 100%" />
        </t-form-item>
      </t-col>
      <t-col :xs="24" :sm="12">
        <t-form-item label="ICP备案号" name="footerIcp">
          <t-input v-model="form.footerIcp" placeholder="如：粤ICP备xxxx号" style="max-width: 500px; width: 100%" />
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

const form = reactive({ footerCompany: '', footerIcp: '', websiteName: '', copyrightStartYear: '' });

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  form.footerCompany = s.footerCompany || '';
  form.footerIcp = s.footerIcp || '';
  form.websiteName = s.websiteName || '';
  form.copyrightStartYear = s.copyrightStartYear || '';
};

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const onSubmit = async () => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  await request.post({
    url: '/system/ui',
    data: {
      footerCompany: form.footerCompany,
      footerIcp: form.footerIcp,
      websiteName: form.websiteName,
      copyrightStartYear: form.copyrightStartYear,
    },
  });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

onMounted(() => {
  load();
});
</script>
