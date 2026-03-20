<template>
  <t-space style="flex-wrap: wrap; margin-bottom: 24px" size="24px">
    <t-input :model-value="query.keyword" placeholder="请输入标题/摘要" style="width: 240px" @update:model-value="emitUpdate('keyword', $event)" @enter="$emit('search')" />
    <t-select :model-value="query.status" clearable :options="statusOptions" placeholder="通知状态" style="width: 160px" @update:model-value="emitUpdate('status', $event)" />
    <t-select :model-value="query.priority" clearable :options="priorityOptions" placeholder="优先级" style="width: 160px" @update:model-value="emitUpdate('priority', $event)" />
    <t-button theme="primary" @click="$emit('search')">查询</t-button>
    <t-button variant="outline" @click="$emit('reset')">重置</t-button>
    <t-button theme="primary" :disabled="!canCreate" @click="$emit('create')">新增通知</t-button>
  </t-space>
</template>

<script setup lang="ts">
defineProps<{ query: Record<string, any>; statusOptions: any[]; priorityOptions: any[]; canCreate: boolean }>();
const emit = defineEmits<{ (e: 'update:query', payload: { key: string; value: any }): void; (e: 'search'): void; (e: 'reset'): void; (e: 'create'): void; }>();
const emitUpdate = (key: string, value: any) => emit('update:query', { key, value: value ?? '' });
</script>
