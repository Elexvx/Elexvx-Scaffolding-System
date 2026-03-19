<template>
  <t-space class="download-toolbar">
    <t-button theme="primary" :disabled="!canCreate" @click="$emit('create')">新增文件</t-button>
    <t-button variant="outline" @click="$emit('refresh')">刷新</t-button>
  </t-space>
</template>

<script setup lang="ts">
defineProps<{
  canCreate: boolean;
}>();

defineEmits<{
  (event: 'create'): void;
  (event: 'refresh'): void;
}>();
</script>
