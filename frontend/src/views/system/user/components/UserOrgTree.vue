<template>
  <div class="user-panel__tree">
    <t-input v-model="keyword" type="search" clearable placeholder="请输入部门名称" @change="$emit('filter')" />
    <t-tree
      class="org-tree"
      :data="treeData"
      :keys="treeKeys"
      hover
      activable
      :expanded="expandedIds"
      @click="(context) => $emit('select', context)"
    />
  </div>
</template>

<script setup lang="ts">
import type { OrgUnitNode } from '../types';

const keyword = defineModel<string>('keyword', { required: true });

defineProps<{
  treeData: OrgUnitNode[];
  expandedIds: number[];
  treeKeys: {
    value: string;
    label: string;
    children: string;
  };
}>();

defineEmits<{
  (event: 'filter'): void;
  (event: 'select', context: { node?: unknown }): void;
}>();
</script>

