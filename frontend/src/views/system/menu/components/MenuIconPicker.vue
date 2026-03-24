<template>
  <t-select
    :model-value="modelValue"
    :options="options"
    clearable
    filterable
    placeholder="点击选择图标"
    :popup-props="{ attach: 'body', zIndex: 3000, overlayClassName: 'menu-icon-select__popup' }"
    :value-display="valueDisplay as any"
    @update:model-value="$emit('update:modelValue', String($event || ''))"
  />
</template>

<script setup lang="tsx">
import type { SelectOption } from 'tdesign-vue-next';

defineProps<{
  modelValue: string;
  options: SelectOption[];
  valueDisplay: (h: unknown, context: { value?: unknown }) => unknown;
}>();

defineEmits<{
  (event: 'update:modelValue', value: string): void;
}>();
</script>
