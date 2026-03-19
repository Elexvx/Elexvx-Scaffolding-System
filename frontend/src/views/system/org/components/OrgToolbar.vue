<template>
  <t-space class="org-toolbar" style="flex-wrap: wrap; margin-bottom: 24px" size="24px">
    <t-input v-model="innerFilters.keyword" clearable placeholder="请输入机构名称" style="width: 240px" @enter="emit('search')" />
    <t-select v-model="innerFilters.status" :options="statusOptions" clearable placeholder="机构状态" style="width: 160px" />
    <t-button theme="primary" @click="emit('search')">搜索</t-button>
    <t-button variant="outline" @click="emit('reset')">重置</t-button>
    <t-button theme="primary" @click="emit('create')">新增</t-button>
    <t-button variant="outline" @click="emit('toggle-expand')">{{ expandAll ? '收起全部' : '展开全部' }}</t-button>
    <t-button theme="primary" :disabled="!dirty" :loading="savingOrder" @click="emit('save-order')">保存排序</t-button>
  </t-space>
</template>

<script setup lang="ts">
import type { SelectOption } from 'tdesign-vue-next';

import type { OrgSearchForm } from '../types';

const innerFilters = defineModel<OrgSearchForm>('filters', { required: true });

defineProps<{
  expandAll: boolean;
  dirty: boolean;
  savingOrder: boolean;
  statusOptions: SelectOption[];
}>();

const emit = defineEmits<{
  search: [];
  reset: [];
  create: [];
  'toggle-expand': [];
  'save-order': [];
}>();
</script>
