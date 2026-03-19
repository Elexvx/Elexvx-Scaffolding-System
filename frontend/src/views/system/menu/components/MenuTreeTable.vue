<template>
  <div class="menu-table-wrapper">
    <t-enhanced-table
      ref="tableRef"
      v-model:expanded-tree-nodes="expandedNodesModel"
      row-key="id"
      drag-sort="row-handler"
      :data="data"
      :columns="columns as any"
      :tree="treeConfig"
      :loading="loading"
      :before-drag-sort="beforeDragSort"
      @abnormal-drag-sort="(context) => emit('abnormal-drag-sort', context)"
      @drag-sort="(context) => emit('drag-sort', context)"
    />
  </div>
</template>

<script setup lang="ts">
import type { EnhancedTableInstanceFunctions } from 'tdesign-vue-next';

import type { MenuNode } from '../types';

const tableRef = defineModel<EnhancedTableInstanceFunctions<MenuNode> | null>('tableRef', { required: true });
const expandedNodesModel = defineModel<Array<string | number>>('expandedNodes', { required: true });

defineProps<{
  data: MenuNode[];
  columns: unknown;
  treeConfig: {
    childrenKey: string;
    treeNodeColumnIndex: number;
    indent: number;
    expandTreeNodeOnClick: boolean;
  };
  loading: boolean;
  beforeDragSort: (context: unknown) => boolean;
}>();

const emit = defineEmits<{
  (event: 'drag-sort', context: unknown): void;
  (event: 'abnormal-drag-sort', context: { code?: number; reason?: string }): void;
}>();
</script>
