<template>
  <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit.prevent>
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12"><t-form-item label="会话有效期（分钟）" help="用户登录后会话保持有效的时长"><t-input-number v-model="form.sessionTimeoutMinutes" :min="1" :step="30" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="Token 有效期（分钟）" help="Access Token 的有效时间"><t-input-number v-model="form.tokenTimeoutMinutes" :min="1" :step="30" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="过期刷新时限（分钟）" help="Token 过期后允许刷新的宽限期"><t-input-number v-model="form.tokenRefreshGraceMinutes" :min="1" :step="10" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :span="24"><security-toolbar :saving="saving" :can-edit="canEdit" @save="$emit('save')" /></t-col>
    </t-row>
  </t-form>
</template>
<script setup lang="ts">import SecurityToolbar from './SecurityToolbar.vue'; defineProps<{ form: Record<string, any>; saving: boolean; canEdit: boolean }>(); defineEmits(['save']);</script>
