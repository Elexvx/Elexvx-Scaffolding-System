<template>
  <t-table
    row-key="id"
    :data="data"
    :columns="columns"
    :selected-row-keys="selectedWordIds"
    :loading="loading"
    hover
    stripe
    size="medium"
    :pagination="pagination"
    class="custom-table"
    @page-change="$emit('page-change', $event)"
    @select-change="$emit('select-change', $event)"
  >
    <template #updatedAt="{ row }">
      {{ formatTime(row.updatedAt || row.createdAt) }}
    </template>
    <template #op="{ row }">
      <t-link theme="danger" hover="color" @click="$emit('delete-row', row)">删除</t-link>
    </template>
  </t-table>
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import type { PrimaryTableCol } from 'tdesign-vue-next';

import type { SensitiveWordData } from '../types';

defineProps<{
  data: SensitiveWordData[];
  columns: PrimaryTableCol[];
  selectedWordIds: number[];
  loading: boolean;
  pagination: { current: number; pageSize: number; total: number };
}>();

defineEmits<{
  (event: 'page-change', pageInfo: { current: number; pageSize: number }): void;
  (event: 'select-change', keys: Array<string | number>): void;
  (event: 'delete-row', row: SensitiveWordData): void;
}>();

const formatTime = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};
</script>
