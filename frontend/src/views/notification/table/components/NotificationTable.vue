<template>
  <t-table :data="data" :columns="columns" row-key="id" :loading="loading" :pagination="pagination" @page-change="$emit('page-change', $event)">
    <template #priority="{ row }">
      <t-tag :theme="priorityTheme(row.priority) as any" variant="light-outline">{{ priorityLabel(row.priority) }}</t-tag>
    </template>
    <template #status="{ row }">
      <t-tag :theme="statusTheme(row.status) as any" variant="light-outline">{{ statusLabel(row.status) }}</t-tag>
    </template>
    <template #op="{ row }">
      <t-space size="small">
        <t-button size="small" variant="text" :disabled="!canEdit" @click="$emit('edit', row)">编辑</t-button>
        <t-popconfirm v-if="row.status === 'published'" content="确认撤回该通知？" @confirm="$emit('toggle-publish', row)">
          <t-button size="small" variant="text" :disabled="!canPublish">撤回</t-button>
        </t-popconfirm>
        <t-button v-else size="small" variant="text" :disabled="!canPublish" @click="$emit('toggle-publish', row)">发布</t-button>
        <t-popconfirm theme="danger" content="确认删除该通知？" @confirm="$emit('delete', row.id)">
          <t-button size="small" variant="text" theme="danger" :disabled="!canDelete">删除</t-button>
        </t-popconfirm>
      </t-space>
    </template>
  </t-table>
</template>

<script setup lang="ts">
defineProps<{ data: any[]; columns: any[]; loading: boolean; pagination: any; priorityTheme: (value?: string) => string; priorityLabel: (value?: string) => string; statusTheme: (value?: string) => string; statusLabel: (value?: string) => string; canEdit: boolean; canDelete: boolean; canPublish: boolean; }>();
defineEmits(['page-change', 'edit', 'toggle-publish', 'delete']);
</script>
