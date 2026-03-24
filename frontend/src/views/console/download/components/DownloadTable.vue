<template>
  <t-table :columns="columns" :data="list" :loading="loading" row-key="id" :pagination="pagination" class="custom-table" @page-change="$emit('page-change', $event)">
    <template #index="{ rowIndex }">
      {{ query.page * query.size + rowIndex + 1 }}
    </template>
    <template #file="{ row }">
      <div class="download-file-cell">
        <div class="download-file-info">
          <span class="download-file-link" :title="row.fileName" @click="$emit('preview', row)">
            {{ row.fileName }}
          </span>
          <span class="download-file-suffix">{{ (row.suffix || resolveSuffix(row.fileName)).toUpperCase() || '文件' }}</span>
        </div>
      </div>
    </template>
    <template #actions="{ row }">
      <t-space size="small">
        <t-button size="small" variant="text" :disabled="!canUpdate" @click="$emit('edit', row)">编辑</t-button>
        <t-popconfirm theme="danger" content="确定删除该文件吗？" @confirm="$emit('delete', row)">
          <t-button size="small" variant="text" theme="danger" :disabled="!canDelete">删除</t-button>
        </t-popconfirm>
      </t-space>
    </template>
  </t-table>
</template>

<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';

import type { DownloadRow } from '../types';
import { resolveSuffix } from '../utils/downloadGuards';

defineProps<{
  columns: PrimaryTableCol[];
  list: DownloadRow[];
  loading: boolean;
  pagination: { current: number; pageSize: number; total: number };
  query: { page: number; size: number };
  canUpdate: boolean;
  canDelete: boolean;
}>();

defineEmits<{
  (event: 'page-change', pageInfo: { current: number; pageSize: number }): void;
  (event: 'preview', row: DownloadRow): void;
  (event: 'edit', row: DownloadRow): void;
  (event: 'delete', row: DownloadRow): void;
}>();
</script>
