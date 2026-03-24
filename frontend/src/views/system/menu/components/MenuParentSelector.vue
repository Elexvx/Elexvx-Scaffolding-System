<template>
  <t-tree-select
    :model-value="modelValue"
    :data="options"
    :keys="treeKeys"
    clearable
    filterable
    placeholder="根节点请留空"
    :popup-props="{ attach: 'body' }"
    @change="$emit('change', $event)"
    @clear="$emit('clear')"
  />
</template>

<script setup lang="ts">
import type { MenuNode } from '../types';

defineProps<{
  modelValue: number | null;
  options: MenuNode[];
  treeKeys: {
    value: string;
    label: string;
    children: string;
  };
}>();

defineEmits<{
  (event: 'change', value: unknown): void;
  (event: 'clear'): void;
}>();
</script>

