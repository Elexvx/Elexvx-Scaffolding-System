<template>
  <t-enhanced-table
    ref="innerTableRef"
    :expanded-tree-nodes="expandedTreeNodes"
    row-key="id"
    drag-sort="row-handler"
    :data="data"
    :columns="columns"
    :loading="loading"
    :tree="treeConfig"
    :before-drag-sort="beforeDragSort"
    @update:expanded-tree-nodes="emit('update:expanded-tree-nodes', $event)"
    @abnormal-drag-sort="emit('abnormal-drag-sort', $event)"
    @drag-sort="emit('drag-sort', $event)"
  >
    <template #leaderNames="{ row }">
      <span>{{ formatNameList(row.leaderNames) }}</span>
    </template>
    <template #userCount="{ row }">
      <span>{{ row.userCount ?? 0 }}</span>
    </template>
    <template #status="{ row }">
      <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light">
        {{ row.status === 1 ? '正常' : '停用' }}
      </t-tag>
    </template>
    <template #createdAt="{ row }">
      <span>{{ formatOrgTime(row.createdAt) }}</span>
    </template>
    <template #op="{ row }">
      <t-space class="org-table-actions">
        <t-link theme="primary" @click="emit('create', row)">新增</t-link>
        <t-link theme="primary" @click="emit('edit', row)">编辑</t-link>
        <t-link theme="danger" @click="emit('delete', row)">删除</t-link>
      </t-space>
    </template>
  </t-enhanced-table>
</template>

<script setup lang="ts">
import type { EnhancedTableInstanceFunctions, PrimaryTableCol } from 'tdesign-vue-next';
import { onMounted, ref, watch } from 'vue';

import type { OrgUnitNode } from '../types';
import { formatNameList, formatOrgTime } from '../utils/orgMappers';

defineProps<{
  data: OrgUnitNode[];
  columns: PrimaryTableCol[];
  loading: boolean;
  treeConfig: Record<string, unknown>;
  expandedTreeNodes: Array<string | number>;
  beforeDragSort: (context: unknown) => boolean;
}>();

const emit = defineEmits(['update:expanded-tree-nodes', 'table-ready', 'abnormal-drag-sort', 'drag-sort', 'create', 'edit', 'delete']);

const innerTableRef = ref<EnhancedTableInstanceFunctions<OrgUnitNode> | null>(null);

watch(
  innerTableRef,
  (value) => {
    emit('table-ready', value);
  },
  { immediate: true },
);

onMounted(() => {
  emit('table-ready', innerTableRef.value);
});
</script>
