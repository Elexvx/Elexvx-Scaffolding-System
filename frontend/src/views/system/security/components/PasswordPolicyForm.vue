<template>
  <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit.prevent>
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12"><t-form-item label="最短长度" help="用户密码允许的最少字符数"><t-input-number v-model="form.passwordMinLength" :min="6" :max="32" style="max-width: 500px; width: 100%" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="必须包含大写字母" help="强制密码中必须包含大写字母 (A-Z)"><t-switch v-model="form.passwordRequireUppercase" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="必须包含小写字母" help="强制密码中必须包含小写字母 (a-z)"><t-switch v-model="form.passwordRequireLowercase" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="必须包含特殊字符" help="强制密码中必须包含特殊字符 (!@#$%^&*)"><t-switch v-model="form.passwordRequireSpecial" /></t-form-item></t-col>
      <t-col :xs="24" :sm="12"><t-form-item label="允许连续字符" help="是否允许密码中包含连续字符 (如 123, abc)"><t-switch v-model="form.passwordAllowSequential" /></t-form-item></t-col>
      <t-col :span="24"><security-toolbar :saving="saving" :can-edit="canEdit" @save="$emit('save')" /></t-col>
    </t-row>
  </t-form>
</template>
<script setup lang="ts">import SecurityToolbar from './SecurityToolbar.vue'; defineProps<{ form: Record<string, any>; saving: boolean; canEdit: boolean }>(); defineEmits(['save']);</script>
