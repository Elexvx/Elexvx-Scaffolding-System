<template>
  <t-card title="在线用户" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space>
        <t-input v-model="loginAddress" clearable placeholder="请输入登录地址" style="width: 260px">
          <template #label>登录地址</template>
        </t-input>
        <t-input v-model="userName" clearable placeholder="请输入用户名" style="width: 260px">
          <template #label>用户名</template>
        </t-input>
        <t-button theme="primary" @click="handleSearch">
          <template #icon><t-icon name="search" /></template>
          搜索
        </t-button>
        <t-button variant="outline" @click="handleReset">
          <template #icon><t-icon name="refresh" /></template>
          重置
        </t-button>
      </t-space>

      <t-table
        row-key="sessionId"
        :data="rows"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        @page-change="onPageChange"
      >
        <template #index="{ rowIndex }">
          {{ (pagination.current - 1) * pagination.pageSize + rowIndex + 1 }}
        </template>
        <template #operation="{ row }">
          <t-link theme="primary" @click="handleForceLogout(row)">强退</t-link>
        </template>
      </t-table>
    </t-space>
  </t-card>
</template>
<script setup lang="ts">
import type { PageInfo, PrimaryTableCol } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';

import { request } from '@/utils/request';

interface OnlineUser {
  sessionId: string;
  loginName: string;
  userName: string;
  ipAddress: string;
  loginLocation: string;
  browser: string;
  os: string;
  loginTime: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

const loginAddress = ref('');
const userName = ref('');
const loading = ref(false);
const rows = ref<OnlineUser[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const columns: PrimaryTableCol[] = [
  { colKey: 'index', title: '序号', width: 80, fixed: 'left' },
  { colKey: 'sessionId', title: '会话编号', width: 220, ellipsis: true },
  { colKey: 'loginName', title: '账号', width: 140 },
  { colKey: 'userName', title: '用户名称', width: 140 },
  { colKey: 'ipAddress', title: 'IP', width: 150 },
  { colKey: 'loginLocation', title: '登录地点', width: 150 },
  { colKey: 'browser', title: '浏览器', width: 150 },
  { colKey: 'os', title: '操作系统', width: 150 },
  { colKey: 'loginTime', title: '登录时间', width: 180 },
  { colKey: 'operation', title: '操作', width: 100, fixed: 'right' },
];

const reload = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await request.get<PageResult<OnlineUser>>({
      url: '/system/monitor/online',
      params: {
        loginAddress: loginAddress.value || undefined,
        userName: userName.value || undefined,
        page: pagination.current - 1,
        size: pagination.pageSize,
      },
    });
    rows.value = res.list;
    pagination.total = res.total;
  } catch (e) {
    MessagePlugin.error(String((e as Error)?.message || '获取在线用户失败'));
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  reload();
};

const handleReset = () => {
  loginAddress.value = '';
  userName.value = '';
  pagination.current = 1;
  reload();
};

const onPageChange = (pageInfo: PageInfo) => {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  reload();
};

const handleForceLogout = (row: OnlineUser) => {
  const confirm = DialogPlugin.confirm({
    header: '强制退出',
    body: `确定要强制退出用户 ${row.userName} 吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    theme: 'warning',
    onConfirm: async () => {
      confirm.hide();
      try {
        await request.delete({
          url: `/system/monitor/online/${encodeURIComponent(row.sessionId)}`,
        });
        MessagePlugin.success('强制退出成功');
        reload();
      } catch (e) {
        MessagePlugin.error(String((e as Error)?.message || '强制退出失败'));
      }
    },
  });
};

let refreshTimer: number | undefined;

onMounted(() => {
  reload();
  refreshTimer = window.setInterval(() => reload(), 5000);
});

onBeforeUnmount(() => {
  if (refreshTimer != null) window.clearInterval(refreshTimer);
});
</script>
