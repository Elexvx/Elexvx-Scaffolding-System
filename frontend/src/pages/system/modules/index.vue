<template>
  <div class="module-management">
    <t-card title="模块管理" :bordered="false">
      <t-space direction="vertical" style="width: 100%" size="large">
        <t-space>
          <t-button variant="outline" :loading="loading" @click="loadModules">刷新</t-button>
          <t-upload
            v-model="installPackages"
            :action="installAction"
            :headers="uploadHeaders"
            theme="file"
            :auto-upload="true"
            :use-mock-progress="true"
            :mock-progress-duration="80"
            :max="1"
            accept=".zip"
            @success="handlePackageInstallSuccess"
            @fail="handlePackageInstallFail"
          >
            <t-button theme="primary" variant="outline">上传 ZIP 安装</t-button>
          </t-upload>
        </t-space>

        <t-table row-key="moduleKey" :data="modules" :columns="columns" :loading="loading">
          <template #installState="{ row }">
            <t-tag :theme="stateTheme(row.installState)" variant="light">
              {{ stateLabel(row.installState) }}
            </t-tag>
          </template>
          <template #enabled="{ row }">
            <t-switch
              :value="isInstalled(row.installState) && Boolean(row.enabled)"
              :disabled="
                !isInstalled(row.installState) ||
                isActionLoading(row.moduleKey, 'enable') ||
                isActionLoading(row.moduleKey, 'disable')
              "
              @change="(val) => toggleEnabled(row, Boolean(val))"
            />
          </template>
          <template #installedAt="{ row }">
            <span>{{ formatDate(row.installedAt) }}</span>
          </template>
          <template #op="{ row }">
            <t-space>
              <t-button
                size="small"
                variant="outline"
                :loading="isActionLoading(row.moduleKey, 'download')"
                :disabled="isActionLoading(row.moduleKey, 'download')"
                @click="downloadPackage(row)"
              >
                下载包
              </t-button>
              <t-button
                size="small"
                theme="primary"
                variant="outline"
                :loading="isActionLoading(row.moduleKey, 'install')"
                :disabled="isInstalled(row.installState)"
                @click="install(row)"
              >
                安装
              </t-button>
              <t-button
                size="small"
                theme="danger"
                variant="outline"
                :loading="isActionLoading(row.moduleKey, 'uninstall')"
                :disabled="!isInstalled(row.installState)"
                @click="uninstall(row)"
              >
                卸载
              </t-button>
            </t-space>
          </template>
        </t-table>
      </t-space>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import dayjs from 'dayjs';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import { importExportApi } from '@/api/importExport';
import type { ModuleRegistryItem } from '@/api/system/module';
import { disableModule, enableModule, fetchModules, installModule, uninstallModule } from '@/api/system/module';
import { usePermissionStore, useUserStore } from '@/store';

interface ModuleTableRow extends ModuleRegistryItem {
  source?: string;
  license?: string;
}

const loading = ref(false);
const modules = ref<ModuleTableRow[]>([]);
const actionLoading = ref<Record<string, boolean>>({});
const userStore = useUserStore();
const permissionStore = usePermissionStore();
const installPackages = ref<any[]>([]);
const uploadHeaders = computed(() => ({
  Authorization: userStore.token,
}));
const installAction = computed(() => importExportApi.modules.installPackageAction());

const refreshNavigation = async () => {
  try {
    await userStore.getUserInfo(true);
  } catch {}
  try {
    await permissionStore.refreshAsyncRoutes(userStore.userInfo);
  } catch (error: any) {
    MessagePlugin.warning(error?.message || '导航刷新失败');
  }
};

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    cell: (_h, { rowIndex }) => String(rowIndex + 1),
  },
  { colKey: 'name', title: '模块名称', minWidth: 160 },
  { colKey: 'moduleKey', title: '模块标识', width: 160 },
  { colKey: 'source', title: '来源/SDK', minWidth: 220, ellipsis: true },
  { colKey: 'license', title: '许可', width: 120 },
  { colKey: 'version', title: '版本', width: 110 },
  { colKey: 'installState', title: '安装状态', width: 120 },
  { colKey: 'enabled', title: '启用状态', width: 110 },
  { colKey: 'installedAt', title: '安装时间', width: 180 },
  { colKey: 'op', title: '操作', width: 200, fixed: 'right' },
];

const normalizeInstallState = (state?: string) =>
  String(state || '')
    .trim()
    .toUpperCase();
const isInstalled = (state?: string) => normalizeInstallState(state) === 'INSTALLED';

const normalizeKey = (key?: string) =>
  String(key || '')
    .trim()
    .toLowerCase();

const fallbackSource = (key: string) => {
  if (key === 'sms') return 'Aliyun Dysmsapi SDK / TencentCloud SMS SDK';
  if (key === 'email') return 'Spring Boot Mail Starter';
  if (key === 'captcha') return 'AJ Captcha';
  if (key === 'sensitive') return 'Sensitive Word Filter';
  return 'Built-in Module';
};

const stateLabel = (state?: string) => {
  const normalized = normalizeInstallState(state);
  if (normalized === 'INSTALLED') return '已安装';
  if (normalized === 'FAILED') return '安装失败';
  if (normalized === 'UNINSTALLED') return '已卸载';
  return '待安装';
};

const stateTheme = (state?: string) => {
  const normalized = normalizeInstallState(state);
  if (normalized === 'INSTALLED') return 'success';
  if (normalized === 'FAILED') return 'danger';
  if (normalized === 'UNINSTALLED') return 'default';
  return 'warning';
};

const formatDate = (value?: string | null) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const setActionLoading = (key: string, action: string, value: boolean) => {
  actionLoading.value[`${key}-${action}`] = value;
};

const isActionLoading = (key: string, action: string) => !!actionLoading.value[`${key}-${action}`];

const loadModules = async () => {
  loading.value = true;
  try {
    const registries = await fetchModules();
    const rows: ModuleTableRow[] = registries.map((registry) => ({
      ...registry,
      source: fallbackSource(normalizeKey(registry.moduleKey)),
      license: 'Apache-2.0',
    }));
    modules.value = rows.sort((a, b) => normalizeKey(a.moduleKey).localeCompare(normalizeKey(b.moduleKey)));
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块加载失败');
  } finally {
    loading.value = false;
  }
};

const install = async (row: ModuleTableRow) => {
  setActionLoading(row.moduleKey, 'install', true);
  try {
    await installModule(row.moduleKey);
    MessagePlugin.success('模块安装完成');
    await refreshNavigation();
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块安装失败');
  } finally {
    setActionLoading(row.moduleKey, 'install', false);
    await loadModules();
  }
};

const downloadPackage = async (row: ModuleTableRow) => {
  const key = normalizeKey(row.moduleKey);
  setActionLoading(row.moduleKey, 'download', true);
  try {
    const response = await importExportApi.modules.downloadPackage(key);
    await importExportApi.utils.downloadBlobResponse(response as any, `${key}.zip`);
  } catch (error: any) {
    MessagePlugin.error(error?.message || '下载失败');
  } finally {
    setActionLoading(row.moduleKey, 'download', false);
  }
};

const handlePackageInstallSuccess = () => {
  MessagePlugin.success('模块包已上传并开始安装');
  void refreshNavigation();
  void loadModules();
};

const handlePackageInstallFail = (context: any) => {
  const msg = String(context?.response?.error || context?.response?.message || context?.error || '模块包安装失败');
  MessagePlugin.error(msg);
  void loadModules();
};

const toggleEnabled = async (row: ModuleTableRow, enabled: boolean) => {
  if (!isInstalled(row.installState)) return;
  setActionLoading(row.moduleKey, enabled ? 'enable' : 'disable', true);
  try {
    if (enabled) {
      await enableModule(row.moduleKey);
      MessagePlugin.success('模块已启用');
    } else {
      await disableModule(row.moduleKey);
      MessagePlugin.success('模块已禁用');
    }
    await refreshNavigation();
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块状态更新失败');
  } finally {
    setActionLoading(row.moduleKey, enabled ? 'enable' : 'disable', false);
    await loadModules();
  }
};

const performUninstall = async (row: ModuleTableRow) => {
  setActionLoading(row.moduleKey, 'uninstall', true);
  try {
    await uninstallModule(row.moduleKey);
    MessagePlugin.success('模块已卸载');
    await refreshNavigation();
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块卸载失败');
  } finally {
    setActionLoading(row.moduleKey, 'uninstall', false);
    await loadModules();
  }
};

const uninstall = (row: ModuleTableRow) => {
  const key = normalizeKey(row.moduleKey);
  const first = DialogPlugin.confirm({
    header: '卸载模块',
    body: `确认卸载模块「${row.name || key}」？该操作会删除模块相关的数据库资源（如表结构/配置数据），且不可恢复。`,
    theme: 'danger',
    confirmBtn: '继续',
    onConfirm: () => {
      first.hide();
      const second = DialogPlugin.confirm({
        header: '再次确认',
        body: `请再次确认卸载模块「${row.name || key}」(${key})。确定继续？`,
        theme: 'danger',
        confirmBtn: '确定卸载',
        onConfirm: async () => {
          second.hide();
          await performUninstall(row);
        },
      });
    },
  });
};

onMounted(() => {
  loadModules();
});
</script>
<style lang="less" scoped>
.module-management {
  :deep(.t-card__body) {
    padding: 16px 24px 24px;
  }
}
</style>
