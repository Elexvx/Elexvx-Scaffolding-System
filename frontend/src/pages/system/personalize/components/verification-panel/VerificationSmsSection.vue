<template>
  <div class="verification-content">
    <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit="emit('submit', $event)">
      <t-row :gutter="[24, 16]">
        <t-col :xs="24" :sm="12"><t-form-item label="启用短信验证码" name="smsEnabled"><t-switch v-model="form.smsEnabled" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="默认通道" name="smsProvider"><t-select v-model="form.smsProvider" :options="providerOptions" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>

        <template v-if="showAliyun">
          <t-col :span="24"><t-divider>阿里云短信服务</t-divider></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="启用短信服务" name="smsAliyunEnabled"><t-switch v-model="form.smsAliyunEnabled" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="AccessKeyId" name="smsAliyunAccessKeyId" help="短信服务 AccessKeyId"><t-input v-model="form.smsAliyunAccessKeyId" placeholder="请输入 AccessKeyId" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="AccessKeySecret" name="smsAliyunAccessKeySecret" help="短信服务 AccessKeySecret"><t-input v-model="form.smsAliyunAccessKeySecret" type="password" placeholder="请输入 AccessKeySecret" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="签名名称" name="smsAliyunSignName" help="短信签名名称"><t-input v-model="form.smsAliyunSignName" placeholder="请输入签名名称" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="模板 Code" name="smsAliyunTemplateCode" help="短信模板 Code"><t-input v-model="form.smsAliyunTemplateCode" placeholder="请输入模板 Code" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="RegionId" name="smsAliyunRegionId" help="例如: cn-hangzhou"><t-input v-model="form.smsAliyunRegionId" placeholder="请输入 RegionId" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="Endpoint" name="smsAliyunEndpoint" help="可选，默认: 服务商默认域名"><t-input v-model="form.smsAliyunEndpoint" placeholder="请输入 Endpoint" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        </template>

        <template v-if="showTencent">
          <t-col :span="24"><t-divider>腾讯云短信服务</t-divider></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="启用短信服务" name="smsTencentEnabled"><t-switch v-model="form.smsTencentEnabled" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="SecretId" name="smsTencentSecretId" help="短信服务 SecretId"><t-input v-model="form.smsTencentSecretId" placeholder="请输入 SecretId" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="SecretKey" name="smsTencentSecretKey" help="短信服务 SecretKey"><t-input v-model="form.smsTencentSecretKey" type="password" placeholder="请输入 SecretKey" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="SdkAppId" name="smsSdkAppId" help="短信应用 SdkAppId"><t-input v-model="form.smsSdkAppId" placeholder="请输入 SdkAppId" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="签名名称" name="smsTencentSignName" help="短信签名名称"><t-input v-model="form.smsTencentSignName" placeholder="请输入签名名称" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="模板 ID" name="smsTencentTemplateId" help="短信模板 ID"><t-input v-model="form.smsTencentTemplateId" placeholder="请输入模板 ID" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="Region" name="smsTencentRegion" help="例如: ap-guangzhou"><t-input v-model="form.smsTencentRegion" placeholder="请输入 Region" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="Endpoint" name="smsTencentEndpoint" help="可选，默认: 服务商默认域名"><t-input v-model="form.smsTencentEndpoint" placeholder="请输入 Endpoint" style="max-width: 500px; width: 100%" :disabled="readonly" /></t-form-item></t-col>
        </template>

        <t-col :span="24"><t-form-item><t-space :size="12"><t-button theme="primary" :disabled="!canUpdate" @click="emit('submit')">保存</t-button></t-space></t-form-item></t-col>
      </t-row>
    </t-form>
  </div>
</template>

<script setup lang="ts">
import type { SmsVerificationForm } from './types';

defineProps<{ canUpdate: boolean; form: SmsVerificationForm; providerOptions: Array<{ label?: string; value?: string | number | boolean }>; readonly: boolean; showAliyun: boolean; showTencent: boolean }>();
const emit = defineEmits<{ submit: [ctx?: any] }>();
</script>

<style scoped lang="less">
.verification-content {
  padding-top: 24px;
  :deep(.t-divider) {
    margin: var(--td-starter-gap-lg) 0 0;
  }
}
</style>
