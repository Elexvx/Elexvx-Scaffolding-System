<template>
  <div class="verification-content">
    <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit="emit('submit', $event)">
      <t-row :gutter="[24, 16]">
        <t-col :xs="24" :sm="12"><t-form-item label="启用邮箱验证码" name="emailEnabled"><t-switch v-model="form.emailEnabled" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="SMTP Host" name="emailHost"><t-input v-model="form.emailHost" placeholder="例如: smtp.qq.com" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="SMTP Port" name="emailPort"><t-input v-model="form.emailPort" placeholder="例如: 465" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="邮箱账号" name="emailUsername"><t-input v-model="form.emailUsername" placeholder="请输入发送邮箱账号" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="邮箱密码/授权码" name="emailPassword"><t-input v-model="form.emailPassword" type="password" placeholder="请输入密码或授权码" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="验证码模板" name="emailTemplate" help="支持HTML，变量 {code} 会被替换"><t-textarea v-model="form.emailTemplate" placeholder="请输入邮件内容模板" :autosize="{ minRows: 3, maxRows: 6 }" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        <t-col :span="24"><t-form-item><t-space :size="12"><t-button theme="primary" :disabled="!canUpdate" @click="emit('submit')">保存</t-button></t-space></t-form-item></t-col>
      </t-row>
    </t-form>
  </div>
</template>

<script setup lang="ts">
import type { EmailVerificationForm } from './types';

defineProps<{ canUpdate: boolean; form: EmailVerificationForm; readonly: boolean }>();
const emit = defineEmits<{ submit: [ctx?: any] }>();
</script>

<style scoped lang="less">
.verification-content {
  padding-top: 24px;
}
</style>
