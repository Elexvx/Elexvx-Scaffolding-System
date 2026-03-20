<template>
  <t-form :data="form" label-width="140px" layout="vertical" label-align="right" @submit="onSubmit">
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12"><t-form-item label="默认首页" name="defaultHome" help="用户登录成功后默认跳转的页面"><t-select v-model="form.defaultHome" :options="homeOptions" filterable placeholder="请选择或输入页面路径" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="多设备登录" name="allowMultiDeviceLogin" help="关闭后同一账号只能在线一个设备"><t-switch v-model="form.allowMultiDeviceLogin" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="日志保留天数" name="logRetentionDays" help="0 表示不自动清理"><t-input-number v-model="form.logRetentionDays" :min="0" :step="1" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="顶部 GitHub 链接" name="headerGithubUrl" help="顶部导航栏 GitHub 图标跳转链接，留空则点击不跳转"><t-input v-model="form.headerGithubUrl" placeholder="https://github.com/your-org/your-repo" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="顶部帮助链接" name="headerHelpUrl" help="顶部导航栏问号图标跳转链接，留空则点击不跳转"><t-input v-model="form.headerHelpUrl" placeholder="https://docs.example.com" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="维护模式" name="maintenanceEnabled" help="开启后登录页将提示维护信息"><t-switch v-model="form.maintenanceEnabled" /></t-form-item></t-col>
      <t-col v-if="form.maintenanceEnabled" :xs="24" :sm="12"><t-form-item label="维护提示文案" name="maintenanceMessage" class="personalize-maintenance-message"><t-textarea v-model="form.maintenanceMessage" :autosize="{ minRows: 1, maxRows: 1 }" placeholder="请输入维护提示，例如：系统维护中，预计 XX 恢复" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col v-if="form.maintenanceEnabled" :xs="24" :sm="12" class="personalize-maintenance-spacer" />
      <t-col :xs="24" :sm="12"><t-form-item label="自动切换主题" name="autoTheme" help="根据时间自动切换浅色/深色"><t-switch v-model="form.autoTheme" /></t-form-item></t-col>
      <t-col v-if="form.autoTheme" :xs="24" :sm="12"><t-form-item label="浅色主题开始" name="lightStartTime"><t-time-picker v-model="form.lightStartTime" format="HH:mm" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col v-if="form.autoTheme" :xs="24" :sm="12"><t-form-item label="深色主题开始" name="darkStartTime"><t-time-picker v-model="form.darkStartTime" format="HH:mm" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :span="24"><t-form-item><t-space :size="12"><t-button theme="primary" type="submit" :disabled="!isAdmin">保存</t-button></t-space></t-form-item></t-col>
    </t-row>
  </t-form>
</template>

<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted } from 'vue';

import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

import { usePersonalizePanel } from './personalize-panel/hooks/usePersonalizePanel';

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));
const { createPayload, form, homeOptions, load, loadHomeOptions } = usePersonalizePanel();

const onSubmit = async (ctx: any) => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  if (ctx && ctx.validateResult !== true) return;
  try {
    const payload = createPayload();
    await request.post({ url: '/system/ui', data: payload });
    form.defaultHome = payload.defaultHome;
    await settingStore.loadUiSetting();
    MessagePlugin.success('保存成功');
  } catch (err: any) {
    MessagePlugin.error(err?.message || '保存失败');
  }
};

onMounted(async () => {
  await loadHomeOptions();
  await load();
});
</script>

<style scoped lang="less">
@import '@/style/variables.less';

.personalize-maintenance-message {
  :deep(.t-form__controls-content) {
    width: 100%;
    max-width: 500px;
  }
  :deep(.t-textarea),
  :deep(.t-textarea__inner) {
    width: 100%;
    max-width: 500px;
  }
  :deep(.t-textarea__inner) {
    height: var(--td-comp-size-m);
    min-height: var(--td-comp-size-m);
    padding-top: 0;
    padding-bottom: 0;
    line-height: var(--td-comp-size-m);
  }
}
.personalize-maintenance-spacer {
  display: block;
}
@media (max-width: @screen-sm-max) {
  .personalize-maintenance-spacer {
    display: none;
  }
}
</style>
