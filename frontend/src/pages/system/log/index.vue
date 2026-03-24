<template>
  <t-card title="操作日志" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space style="flex-wrap: wrap">
        <t-input
          v-model="keyword"
          type="search"
          clearable
          placeholder="搜索账号/模块/详情/IP/设备"
          style="width: 260px"
          @enter="search"
        />
        <t-select v-model="action" :options="actionOptions" clearable placeholder="操作类型" style="width: 160px" />
        <t-date-range-picker
          v-model="dateRange"
          clearable
          format="YYYY-MM-DD"
          value-type="YYYY-MM-DD"
          :placeholder="['开始日期', '结束日期']"
        />
        <t-button theme="primary" @click="search">查询</t-button>
        <t-button variant="outline" @click="resetFilters">重置</t-button>
        <t-button variant="outline" :loading="exporting" @click="exportCsv">导出</t-button>
      </t-space>

      <t-table
        row-key="id"
        :data="rows"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        @page-change="onPageChange"
      >
        <template #action="{ row }">
          <t-tag :theme="actionTheme(row.action)" variant="light">{{ actionLabel(row.action) }}</t-tag>
        </template>
      </t-table>
    </t-space>
  </t-card>
</template>
<script setup lang="ts">
import type { PageInfo, PrimaryTableCol, SelectOption } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import { importExportApi } from '@/api/importExport';
import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions, resolveLabel, resolveTagTheme } from '@/utils/dict';
import { request } from '@/utils/request';

interface LogRow {
  id: number;
  action: string;
  module?: string;
  detail?: string;
  account?: string;
  ipAddress?: string;
  deviceInfo?: string;
  createdAt?: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

type TagTheme = 'default' | 'success' | 'primary' | 'warning' | 'danger';

const keyword = ref('');
const action = ref('');
const dateRange = ref<string[]>([]);
const loading = ref(false);
const exporting = ref(false);

const rows = ref<LogRow[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const actionDict = useDictionary('log_action');
const fallbackActionOptions: SelectOption[] = [
  { label: '登录', value: 'LOGIN' },
  { label: '新增', value: 'CREATE' },
  { label: '修改', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
];

const actionLabelMap: Record<string, string> = {
  LOGIN: '登录',
  CREATE: '新增',
  UPDATE: '修改',
  DELETE: '删除',
};
const actionThemeMap: Record<string, TagTheme> = {
  LOGIN: 'primary',
  CREATE: 'success',
  UPDATE: 'warning',
  DELETE: 'danger',
};

const actionOptions = computed<SelectOption[]>(() => buildDictOptions(actionDict.items.value, fallbackActionOptions));

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    fixed: 'left',
    cell: (_h, { rowIndex }) => String((pagination.current - 1) * pagination.pageSize + rowIndex + 1),
  },
  { colKey: 'createdAt', title: '时间', width: 180 },
  { colKey: 'action', title: '操作', width: 100 },
  { colKey: 'module', title: '模块', width: 140, ellipsis: true },
  { colKey: 'detail', title: '详情', width: 280, ellipsis: true },
  { colKey: 'account', title: '账号', width: 140, ellipsis: true },
  { colKey: 'ipAddress', title: 'IP', width: 140, ellipsis: true },
  { colKey: 'deviceInfo', title: '设备信息', minWidth: 260, ellipsis: true },
];

const actionLabel = (value?: string) => {
  return resolveLabel(value, actionDict.items.value, actionLabelMap);
};

const actionTheme = (value?: string): TagTheme => {
  return resolveTagTheme(value, actionDict.items.value, actionThemeMap) as TagTheme;
};

const resolveDateParams = () => {
  const start = dateRange.value?.[0];
  const end = dateRange.value?.[1];
  return { start, end };
};

const reload = async () => {
  loading.value = true;
  try {
    const { start, end } = resolveDateParams();
    const res = await request.get<PageResult<LogRow>>({
      url: '/system/log/page',
      params: {
        keyword: keyword.value || undefined,
        action: action.value || undefined,
        start: start || undefined,
        end: end || undefined,
        page: pagination.current - 1,
        size: pagination.pageSize,
      },
    });
    rows.value = res.list || [];
    pagination.total = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const search = () => {
  pagination.current = 1;
  reload();
};

const onPageChange = (pi: PageInfo) => {
  pagination.current = pi.current;
  pagination.pageSize = pi.pageSize;
  reload();
};

const resetFilters = () => {
  keyword.value = '';
  action.value = '';
  dateRange.value = [];
  pagination.current = 1;
  reload();
};

const exportCsv = async () => {
  exporting.value = true;
  try {
    const { start, end } = resolveDateParams();
    const res = await importExportApi.log.exportCsv({
      keyword: keyword.value || undefined,
      action: action.value || undefined,
      start: start || undefined,
      end: end || undefined,
    });
    await importExportApi.utils.downloadBlobResponse(res as any, 'operation-logs.xlsx');
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '导出失败'));
  } finally {
    exporting.value = false;
  }
};

onMounted(() => {
  void actionDict.load();
  reload();
});
</script>
