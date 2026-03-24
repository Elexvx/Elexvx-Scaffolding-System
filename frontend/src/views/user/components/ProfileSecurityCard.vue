<template>
  <info-section-card
    value="security"
    title="安全设置"
    :percent="percent"
    :sensitive-toggle="true"
    :sensitive-visible="!masked"
    :show-action="false"
    @toggle-sensitive="$emit('toggle-sensitive')"
  >
    <div id="user-block-security" class="section-body">
      <field-status-item
        v-for="item in items"
        :key="item.key"
        :label="item.label"
        :value="item.value"
        :done="item.done"
        :action-text="item.done ? '' : '去完善'"
        @action="$emit('edit-basic')"
      />
      <div class="section-block-title">修改密码</div>
      <t-button theme="primary" variant="outline" @click="$emit('open-password-dialog')">打开密码修改</t-button>
      <div class="section-block-title">最近登录日志</div>
      <t-table row-key="id" class="login-log-table" :data="loginLogs" :columns="loginLogColumns" :loading="loginLogLoading" :pagination="null" />
    </div>
  </info-section-card>
</template>

<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';

import type { LoginLogRow, StatusItem } from '../types';
import FieldStatusItem from './FieldStatusItem.vue';
import InfoSectionCard from './InfoSectionCard.vue';

defineProps<{
  percent: number;
  masked: boolean;
  items: StatusItem[];
  loginLogs: LoginLogRow[];
  loginLogLoading: boolean;
  loginLogColumns: PrimaryTableCol[];
}>();

defineEmits<{
  (event: 'toggle-sensitive'): void;
  (event: 'edit-basic'): void;
  (event: 'open-password-dialog'): void;
}>();
</script>

<style scoped lang="less">
.section-block-title {
  margin-top: 12px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--td-text-color-primary);
}

.login-log-table {
  margin-top: 4px;
}
</style>
