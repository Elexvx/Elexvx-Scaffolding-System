<template>
  <div class="user-panel__filters">
    <t-form layout="inline" label-width="0" @submit.prevent="$emit('search')">
      <t-form-item>
        <t-input v-model="filters.keyword" clearable placeholder="用户名称" style="width: 240px" />
      </t-form-item>
      <t-form-item>
        <t-input v-model="filters.mobile" clearable placeholder="手机号" style="width: 240px" />
      </t-form-item>
      <t-form-item>
        <t-select v-model="filters.status" :options="statusOptions" clearable placeholder="用户状态" style="width: 240px" />
      </t-form-item>
      <t-form-item>
        <t-date-range-picker
          v-model="filters.createdRange"
          allow-input
          clearable
          format="YYYY-MM-DD"
          value-type="YYYY-MM-DD"
          placeholder="开始日期 - 结束日期"
          style="width: 320px"
        />
      </t-form-item>
      <t-form-item>
        <t-space size="small">
          <t-button v-perm:disable="'system:SystemUser:query'" theme="primary" type="submit">
            <template #icon><t-icon name="search" /></template>
            搜索
          </t-button>
          <t-button variant="outline" @click="$emit('reset')">
            <template #icon><t-icon name="refresh" /></template>
            重置
          </t-button>
        </t-space>
      </t-form-item>
    </t-form>
    <div class="user-filter__actions">
      <t-button v-perm="'system:SystemUser:create'" theme="primary" @click="$emit('create')">
        <template #icon><t-icon name="add" /></template>
        新增
      </t-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Icon as TIcon } from 'tdesign-icons-vue-next';
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

