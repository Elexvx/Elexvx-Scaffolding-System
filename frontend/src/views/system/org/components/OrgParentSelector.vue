<template>
  <t-tree-select
    :model-value="modelValue"
    :data="data"
    :keys="treeKeys"
    clearable
    placeholder="选择上级机构"
    style="width: 100%"
    @update:model-value="emit('update:modelValue', $event as number | undefined)"
  />
</template>

<script setup lang="ts">
import type { TreeProps } from 'tdesign-vue-next';

import type { OrgUnitNode } from '../types';

defineProps<{
  modelValue: number | undefined;
  data: OrgUnitNode[];
  treeKeys: TreeProps['keys'];
}>();

const emit = defineEmits<{
  'update:modelValue': [number | undefined];
}>();
</script>
