<template>
  <t-space>
    <t-button v-perm="'system:SystemRole:create'" theme="primary" @click="$emit('create')">新增角色</t-button>
    <t-button variant="outline" @click="$emit('reload')">刷新</t-button>
  </t-space>
</template>

<script setup lang="ts">
defineEmits<{
  (e: 'create'): void;
  (e: 'reload'): void;
}>();
</script>
