<template>
  <div class="user-panel__filters">
    <div class="user-filter">
      <t-input v-model="filters.keyword" clearable placeholder="用户名称" />
      <t-input v-model="filters.mobile" clearable placeholder="手机号" />
      <t-select v-model="filters.status" :options="statusOptions" clearable placeholder="用户状态" />
      <t-date-range-picker
        v-model="filters.createdRange"
        allow-input
        clearable
        format="YYYY-MM-DD"
        value-type="YYYY-MM-DD"
        placeholder="开始日期 - 结束日期"
      />
      <div class="user-filter__buttons">
        <t-space size="small">
          <t-button v-perm:disable="'system:SystemUser:query'" theme="primary" @click="$emit('search')">搜索</t-button>
          <t-button variant="outline" @click="$emit('reset')">重置</t-button>
        </t-space>
      </div>
    </div>
    <div class="user-filter__actions">
      <t-button v-perm="'system:SystemUser:create'" theme="primary" @click="$emit('create')">新增</t-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SelectOption } from 'tdesign-vue-next';

import type { UserSearchModel } from '../types';

defineProps<{
  filters: UserSearchModel;
  statusOptions: SelectOption[];
}>();

defineEmits<{
  (event: 'search'): void;
  (event: 'reset'): void;
  (event: 'create'): void;
}>();
</script>

