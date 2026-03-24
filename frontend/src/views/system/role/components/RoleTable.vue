<template>
  <t-table row-key="id" :data="rows" :columns="columns" :loading="loading">
    <template #op="{ row }">
      <t-space>
        <t-link v-perm:disable="'system:SystemRole:update'" theme="primary" @click="$emit('edit', row)">编辑</t-link>
        <t-link
          v-perm:disable="'system:SystemRole:delete'"
          theme="danger"
          :disabled="row.name === 'admin'"
          @click="$emit('remove', row)"
        >
          删除
        </t-link>
      </t-space>
    </template>
  </t-table>
</template>

<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';

import type { RoleRow } from '../types';

defineProps<{
  rows: RoleRow[];
  columns: PrimaryTableCol[];
  loading: boolean;
}>();

defineEmits<{
  (e: 'edit', row: RoleRow): void;
  (e: 'remove', row: RoleRow): void;
}>();
</script>
