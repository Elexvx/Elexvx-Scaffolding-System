<template>
  <view-field-status-item v-bind="props" @action="$emit('action')" />
</template>

<script setup lang="ts">
import ViewFieldStatusItem from '@/views/user/components/FieldStatusItem.vue';

const props = defineProps<{
  label: string;
  value?: string;
  done: boolean;
  actionText?: string;
}>();

defineEmits<{
  (e: 'action'): void;
}>();
</script>
