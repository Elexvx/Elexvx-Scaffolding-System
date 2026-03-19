<template>
  <t-table
    class="user-table"
    row-key="id"
    :data="rows"
    :columns="columns"
    :pagination="pagination"
    :loading="loading"
    @page-change="(pageInfo) => $emit('page-change', pageInfo)"
  >
    <template #orgUnitNames="{ row }">
      <span>{{ formatOrgPath(row.orgUnitNames) }}</span>
    </template>
    <template #departmentNames="{ row }">
      <span>{{ formatOrgPath(row.departmentNames) }}</span>
    </template>
    <template #status="{ row }">
      <t-switch :value="row.status === 1" :disabled="!canUpdate" @change="(val) => $emit('toggle-status', row, Boolean(val))" />
    </template>
    <template #roles="{ row }">
      <t-space>
        <t-tag v-for="role in row.roles || []" :key="role" theme="primary" variant="light">{{ role }}</t-tag>
      </t-space>
    </template>
    <template #op="{ row }">
      <t-space class="user-table-actions">
        <t-link
          v-perm:disable="'system:SystemUser:update'"
          :disabled="isEditDisabled(row)"
          theme="primary"
          @click="$emit('edit', row)"
          >编辑</t-link
        >
        <t-link
          v-perm:disable="'system:SystemUser:update'"
          :disabled="isResetDisabled(row)"
          theme="primary"
          @click="$emit('reset-password', row)"
          >重置密码</t-link
        >
        <t-link
          v-perm:disable="'system:SystemUser:delete'"
          :disabled="isDeleteDisabled(row)"
          theme="danger"
          @click="$emit('delete', row)"
          >删除</t-link
        >
      </t-space>
    </template>
  </t-table>
</template>

<script setup lang="ts">
import type { PageInfo, PrimaryTableCol } from 'tdesign-vue-next';

import { formatOrgPath } from '../utils/userMappers';
import type { UserRow } from '../types';

defineProps<{
  rows: UserRow[];
  columns: PrimaryTableCol[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
  loading: boolean;
  canUpdate: boolean;
  isEditDisabled: (row: UserRow) => boolean;
  isResetDisabled: (row: UserRow) => boolean;
  isDeleteDisabled: (row: UserRow) => boolean;
}>();

defineEmits<{
  (event: 'page-change', pageInfo: PageInfo): void;
  (event: 'edit', row: UserRow): void;
  (event: 'reset-password', row: UserRow): void;
  (event: 'delete', row: UserRow): void;
  (event: 'toggle-status', row: UserRow, enabled: boolean): void;
}>();
</script>

