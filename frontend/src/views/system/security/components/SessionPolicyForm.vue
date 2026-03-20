<template>
  <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit.prevent>
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12"><t-form-item label="统计窗口（分钟）" help="用于统计高频访问的时间窗口大小"><t-input-number v-model="form.defenseWindowMinutes" :min="1" :step="1" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="最大验证次数" help="统计窗口内允许的最大验证请求次数（超过将拦截）"><t-input-number v-model="form.defenseMaxRequests" :min="10" :step="10" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="最大错误次数" help="统计窗口内允许的最大验证失败次数（超过将拦截）"><t-input-number v-model="form.defenseMaxErrors" :min="3" :step="1" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :span="24"><security-toolbar :saving="saving" :can-edit="canEdit" @save="$emit('save')" /></t-col>
    </t-row>
  </t-form>
</template>
<script setup lang="ts">import SecurityToolbar from './SecurityToolbar.vue'; defineProps<{ form: Record<string, any>; saving: boolean; canEdit: boolean }>(); defineEmits(['save']);</script>
