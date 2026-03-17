<template>
  <t-form :data="form" label-width="140px" layout="vertical" label-align="right" @submit="onSubmit">
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12">
        <t-form-item label="默认首页" name="defaultHome" help="用户登录成功后默认跳转的页面">
          <t-select
            v-model="form.defaultHome"
            :options="homeOptions"
            filterable
            placeholder="请选择或输入页面路径"
            style="max-width: 500px; width: 100%"
          />
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="多设备登录" name="allowMultiDeviceLogin" help="关闭后同一账号只能在线一个设备">
          <t-switch v-model="form.allowMultiDeviceLogin" />
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="日志保留天数" name="logRetentionDays" help="0 表示不自动清理">
          <t-input-number v-model="form.logRetentionDays" :min="0" :step="1" style="max-width: 500px; width: 100%" />
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item
          label="顶部 GitHub 链接"
          name="headerGithubUrl"
          help="顶部导航栏 GitHub 图标跳转链接，留空则点击不跳转"
        >
          <t-input
            v-model="form.headerGithubUrl"
            placeholder="https://github.com/your-org/your-repo"
            style="max-width: 500px; width: 100%"
          />
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="顶部帮助链接" name="headerHelpUrl" help="顶部导航栏问号图标跳转链接，留空则点击不跳转">
          <t-input
            v-model="form.headerHelpUrl"
            placeholder="https://docs.example.com"
            style="max-width: 500px; width: 100%"
          />
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="维护模式" name="maintenanceEnabled" help="开启后登录页将提示维护信息">
          <t-switch v-model="form.maintenanceEnabled" />
        </t-form-item>
      </t-col>

      <t-col v-if="form.maintenanceEnabled" :xs="24" :sm="12">
        <t-form-item label="维护提示文案" name="maintenanceMessage" class="personalize-maintenance-message">
          <t-textarea
            v-model="form.maintenanceMessage"
            :autosize="{ minRows: 1, maxRows: 1 }"
            placeholder="请输入维护提示，例如：系统维护中，预计 XX 恢复"
            style="max-width: 500px; width: 100%"
          />
        </t-form-item>
      </t-col>
      <t-col v-if="form.maintenanceEnabled" :xs="24" :sm="12" class="personalize-maintenance-spacer" />

      <t-col :xs="24" :sm="12">
        <t-form-item label="自动切换主题" name="autoTheme" help="根据时间自动切换浅色/深色">
          <t-switch v-model="form.autoTheme" />
        </t-form-item>
      </t-col>

      <t-col v-if="form.autoTheme" :xs="24" :sm="12">
        <t-form-item label="浅色主题开始" name="lightStartTime">
          <t-time-picker v-model="form.lightStartTime" format="HH:mm" style="max-width: 500px; width: 100%" />
        </t-form-item>
      </t-col>

      <t-col v-if="form.autoTheme" :xs="24" :sm="12">
        <t-form-item label="深色主题开始" name="darkStartTime">
          <t-time-picker v-model="form.darkStartTime" format="HH:mm" style="max-width: 500px; width: 100%" />
        </t-form-item>
      </t-col>

      <t-col :span="24">
        <t-form-item>
          <t-space :size="12">
            <t-button theme="primary" type="submit" :disabled="!isAdmin">保存</t-button>
          </t-space>
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>
</template>
<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

const form = reactive({
  allowMultiDeviceLogin: true,
  logRetentionDays: 90,
  defaultHome: '/user/index',
  autoTheme: false,
  headerGithubUrl: '',
  headerHelpUrl: '',
  maintenanceEnabled: false,
  maintenanceMessage: '',
  lightStartTime: '06:00',
  darkStartTime: '18:00',
});

const homeOptions = ref<{ label: string; value: string }[]>([]);
const homePathMap = ref<Record<string, string>>({});

const normalizePath = (parentPath: string, path?: string) => {
  const next = String(path || '').trim();
  if (!next) return parentPath || '';
  if (next.startsWith('/')) return next;
  if (!parentPath) return `/${next}`;
  return `${parentPath.replace(/\/$/, '')}/${next}`;
};

const loadHomeOptions = async () => {
  try {
    const tree = await request.get<any[]>({ url: '/system/menu/tree' });
    const options: { label: string; value: string }[] = [];
    const pathMap: Record<string, string> = {};

    const walk = (nodes: any[], parentTitle = '', parentPath = '') => {
      nodes.forEach((n) => {
        const fullTitle = parentTitle ? `${parentTitle} / ${n.titleZhCn}` : n.titleZhCn;
        const fullPath = normalizePath(parentPath, n.path);
        // 只添加未禁用的页面（hidden不为true）
        if (n.nodeType === 'PAGE' && fullPath && !n.hidden) {
          options.push({ label: fullTitle, value: fullPath });
          if (n.path && !pathMap[n.path]) {
            pathMap[n.path] = fullPath;
          }
        }
        if (n.children?.length) {
          walk(n.children, fullTitle, fullPath);
        }
      });
    };
    walk(tree);
    homeOptions.value = options;
    homePathMap.value = pathMap;
  } catch (err) {
    console.error('Failed to load home options:', err);
  }
};

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  form.allowMultiDeviceLogin = s.allowMultiDeviceLogin !== undefined ? !!s.allowMultiDeviceLogin : true;
  form.logRetentionDays = s.logRetentionDays !== undefined ? Number(s.logRetentionDays) : 90;
  const rawHome = s.defaultHome || '/user/index';
  form.defaultHome = homePathMap.value[rawHome] || normalizePath('', rawHome);
  form.autoTheme = !!s.autoTheme;
  form.headerGithubUrl = s.headerGithubUrl || '';
  form.headerHelpUrl = s.headerHelpUrl || '';
  form.maintenanceEnabled = s.maintenanceEnabled !== undefined ? !!s.maintenanceEnabled : false;
  form.maintenanceMessage = s.maintenanceMessage || '';
  form.lightStartTime = s.lightStartTime || '06:00';
  form.darkStartTime = s.darkStartTime || '18:00';
};

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const onSubmit = async (ctx: any) => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  if (ctx && ctx.validateResult !== true) {
    return;
  }

  const normalizedDefaultHome = homePathMap.value[form.defaultHome] || normalizePath('', form.defaultHome);
  try {
    await request.post({
      url: '/system/ui',
      data: {
        allowMultiDeviceLogin: form.allowMultiDeviceLogin,
        logRetentionDays: form.logRetentionDays,
        defaultHome: normalizedDefaultHome,
        autoTheme: form.autoTheme,
        headerGithubUrl: form.headerGithubUrl,
        headerHelpUrl: form.headerHelpUrl,
        maintenanceEnabled: form.maintenanceEnabled,
        maintenanceMessage: form.maintenanceMessage,
        lightStartTime: form.lightStartTime,
        darkStartTime: form.darkStartTime,
      },
    });
    form.defaultHome = normalizedDefaultHome;
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
