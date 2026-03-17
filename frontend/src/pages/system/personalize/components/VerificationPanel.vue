<template>
  <t-tabs v-model="activeTab">
    <t-tab-panel value="sms" label="短信验证码设置" :destroy-on-hide="false">
      <div class="verification-content">
        <t-form :data="smsForm" layout="vertical" label-align="right" label-width="140px" @submit="onSubmitSms">
          <t-row :gutter="[24, 16]">
            <t-col :xs="24" :sm="12">
              <t-form-item label="启用短信验证码" name="smsEnabled">
                <t-switch v-model="smsForm.smsEnabled" />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="默认通道" name="smsProvider">
                <t-select
                  v-model="smsForm.smsProvider"
                  :options="smsProviderOptions"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>

            <t-col v-if="showAliyun" :span="24">
              <t-divider>阿里云短信服务</t-divider>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="启用短信服务" name="smsAliyunEnabled">
                <t-switch v-model="smsForm.smsAliyunEnabled" :disabled="smsReadonly" />
              </t-form-item>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="AccessKeyId" name="smsAliyunAccessKeyId" help="短信服务 AccessKeyId">
                <t-input
                  v-model="smsForm.smsAliyunAccessKeyId"
                  placeholder="请输入 AccessKeyId"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="AccessKeySecret" name="smsAliyunAccessKeySecret" help="短信服务 AccessKeySecret">
                <t-input
                  v-model="smsForm.smsAliyunAccessKeySecret"
                  type="password"
                  placeholder="请输入 AccessKeySecret"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="签名名称" name="smsAliyunSignName" help="短信签名名称">
                <t-input
                  v-model="smsForm.smsAliyunSignName"
                  placeholder="请输入签名名称"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="模板 Code" name="smsAliyunTemplateCode" help="短信模板 Code">
                <t-input
                  v-model="smsForm.smsAliyunTemplateCode"
                  placeholder="请输入模板 Code"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="RegionId" name="smsAliyunRegionId" help="例如: cn-hangzhou">
                <t-input
                  v-model="smsForm.smsAliyunRegionId"
                  placeholder="请输入 RegionId"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showAliyun" :xs="24" :sm="12">
              <t-form-item label="Endpoint" name="smsAliyunEndpoint" help="可选，默认: 服务商默认域名">
                <t-input
                  v-model="smsForm.smsAliyunEndpoint"
                  placeholder="请输入 Endpoint"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>

            <t-col v-if="showTencent" :span="24">
              <t-divider>腾讯云短信服务</t-divider>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="启用短信服务" name="smsTencentEnabled">
                <t-switch v-model="smsForm.smsTencentEnabled" :disabled="smsReadonly" />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="SecretId" name="smsTencentSecretId" help="短信服务 SecretId">
                <t-input
                  v-model="smsForm.smsTencentSecretId"
                  placeholder="请输入 SecretId"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="SecretKey" name="smsTencentSecretKey" help="短信服务 SecretKey">
                <t-input
                  v-model="smsForm.smsTencentSecretKey"
                  type="password"
                  placeholder="请输入 SecretKey"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="SdkAppId" name="smsSdkAppId" help="短信应用 SdkAppId">
                <t-input
                  v-model="smsForm.smsSdkAppId"
                  placeholder="请输入 SdkAppId"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="签名名称" name="smsTencentSignName" help="短信签名名称">
                <t-input
                  v-model="smsForm.smsTencentSignName"
                  placeholder="请输入签名名称"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="模板 ID" name="smsTencentTemplateId" help="短信模板 ID">
                <t-input
                  v-model="smsForm.smsTencentTemplateId"
                  placeholder="请输入模板 ID"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="Region" name="smsTencentRegion" help="例如: ap-guangzhou">
                <t-input
                  v-model="smsForm.smsTencentRegion"
                  placeholder="请输入 Region"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col v-if="showTencent" :xs="24" :sm="12">
              <t-form-item label="Endpoint" name="smsTencentEndpoint" help="可选，默认: 服务商默认域名">
                <t-input
                  v-model="smsForm.smsTencentEndpoint"
                  placeholder="请输入 Endpoint"
                  style="max-width: 500px; width: 100%"
                  :disabled="smsReadonly"
                />
              </t-form-item>
            </t-col>

            <t-col :span="24">
              <t-form-item>
                <t-space :size="12">
                  <t-button theme="primary" :disabled="!canUpdate" @click="onSubmitSms">保存</t-button>
                </t-space>
              </t-form-item>
            </t-col>
          </t-row>
        </t-form>
      </div>
    </t-tab-panel>
    <t-tab-panel value="email" label="邮箱验证码设置" :destroy-on-hide="false">
      <div class="verification-content">
        <t-form :data="emailForm" layout="vertical" label-align="right" label-width="140px" @submit="onSubmitEmail">
          <t-row :gutter="[24, 16]">
            <t-col :xs="24" :sm="12">
              <t-form-item label="启用邮箱验证码" name="emailEnabled">
                <t-switch v-model="emailForm.emailEnabled" />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="SMTP Host" name="emailHost">
                <t-input
                  v-model="emailForm.emailHost"
                  placeholder="例如: smtp.qq.com"
                  style="max-width: 500px; width: 100%"
                  :disabled="emailReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="SMTP Port" name="emailPort">
                <t-input
                  v-model="emailForm.emailPort"
                  placeholder="例如: 465"
                  style="max-width: 500px; width: 100%"
                  :disabled="emailReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="邮箱账号" name="emailUsername">
                <t-input
                  v-model="emailForm.emailUsername"
                  placeholder="请输入发送邮箱账号"
                  style="max-width: 500px; width: 100%"
                  :disabled="emailReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="邮箱密码/授权码" name="emailPassword">
                <t-input
                  v-model="emailForm.emailPassword"
                  type="password"
                  placeholder="请输入密码或授权码"
                  style="max-width: 500px; width: 100%"
                  :disabled="emailReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="验证码模板" name="emailTemplate" help="支持HTML，变量 {code} 会被替换">
                <t-textarea
                  v-model="emailForm.emailTemplate"
                  placeholder="请输入邮件内容模板"
                  :autosize="{ minRows: 3, maxRows: 6 }"
                  style="max-width: 500px; width: 100%"
                  :disabled="emailReadonly"
                />
              </t-form-item>
            </t-col>
            <t-col :span="24">
              <t-form-item>
                <t-space :size="12">
                  <t-button theme="primary" :disabled="!canUpdate" @click="onSubmitEmail">保存</t-button>
                </t-space>
              </t-form-item>
            </t-col>
          </t-row>
        </t-form>
      </div>
    </t-tab-panel>
  </t-tabs>
</template>
<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useDictionary } from '@/hooks/useDictionary';
import { useSettingStore } from '@/store';
import { buildDictOptions } from '@/utils/dict';
import { hasPerm } from '@/utils/permission';
import { request } from '@/utils/request';

const route = useRoute();
const router = useRouter();
const verificationTabs = ['sms', 'email'];
const resolveTab = (value: unknown) => {
  const tab = typeof value === 'string' ? value : '';
  return verificationTabs.includes(tab) ? tab : verificationTabs[0];
};

const activeTab = ref(resolveTab(route.query.tab));

const smsForm = reactive({
  smsEnabled: false,
  smsProvider: 'aliyun',
  smsAliyunEnabled: false,
  smsAliyunAccessKeyId: '',
  smsAliyunAccessKeySecret: '',
  smsAliyunSignName: '',
  smsAliyunTemplateCode: '',
  smsAliyunRegionId: 'cn-hangzhou',
  smsAliyunEndpoint: '',
  smsTencentEnabled: false,
  smsTencentSecretId: '',
  smsTencentSecretKey: '',
  smsTencentSignName: '',
  smsTencentTemplateId: '',
  smsTencentRegion: 'ap-guangzhou',
  smsTencentEndpoint: '',
  smsSdkAppId: '',
});

const emailForm = reactive({
  emailEnabled: false,
  emailHost: '',
  emailPort: 465,
  emailUsername: '',
  emailPassword: '',
  emailFrom: '',
  emailSsl: true,
  emailTemplate: '',
});

const settingStore = useSettingStore();

watch(
  () => route.query.tab,
  (value) => {
    const next = resolveTab(value);
    if (next !== activeTab.value) {
      activeTab.value = next;
    }
  },
);
const canUpdate = computed(
  () => hasPerm('system:SystemVerification:update') || hasPerm('system:SystemPersonalize:update'),
);
const smsProviderDict = useDictionary('sms_provider');
const fallbackSmsProviderOptions = [
  { label: '阿里云短信服务', value: 'aliyun' },
  { label: '腾讯云短信服务', value: 'tencent' },
];
const SMS_PROVIDER_VALUES = ['aliyun', 'tencent'];
const smsProviderOptions = computed(() =>
  buildDictOptions(smsProviderDict.items.value, fallbackSmsProviderOptions, SMS_PROVIDER_VALUES),
);
const smsReadonly = computed(() => !smsForm.smsEnabled);
const emailReadonly = computed(() => !emailForm.emailEnabled);
const showAliyun = computed(() => smsForm.smsProvider === 'aliyun');
const showTencent = computed(() => smsForm.smsProvider === 'tencent');

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  smsForm.smsEnabled = !!s.smsEnabled;
  smsForm.smsProvider = s.smsProvider || 'aliyun';
  smsForm.smsAliyunEnabled = s.smsAliyunEnabled ?? false;
  smsForm.smsAliyunAccessKeyId = s.smsAliyunAccessKeyId || s.smsAccessKeyId || '';
  smsForm.smsAliyunAccessKeySecret = s.smsAliyunAccessKeySecret || s.smsAccessKeySecret || '';
  smsForm.smsAliyunSignName = s.smsAliyunSignName || s.smsSignName || '';
  smsForm.smsAliyunTemplateCode = s.smsAliyunTemplateCode || s.smsTemplateCode || '';
  smsForm.smsAliyunRegionId = s.smsAliyunRegionId || s.smsRegionId || 'cn-hangzhou';
  smsForm.smsAliyunEndpoint = s.smsAliyunEndpoint || s.smsEndpoint || '';
  smsForm.smsTencentEnabled = s.smsTencentEnabled ?? false;
  const tencentFallback = s.smsProvider === 'tencent';
  smsForm.smsTencentSecretId = s.smsTencentSecretId || (tencentFallback ? s.smsAccessKeyId : '') || '';
  smsForm.smsTencentSecretKey = s.smsTencentSecretKey || (tencentFallback ? s.smsAccessKeySecret : '') || '';
  smsForm.smsTencentSignName = s.smsTencentSignName || (tencentFallback ? s.smsSignName : '') || '';
  smsForm.smsTencentTemplateId = s.smsTencentTemplateId || (tencentFallback ? s.smsTemplateCode : '') || '';
  smsForm.smsTencentRegion = s.smsTencentRegion || (tencentFallback ? s.smsRegionId : '') || 'ap-guangzhou';
  smsForm.smsTencentEndpoint = s.smsTencentEndpoint || (tencentFallback ? s.smsEndpoint : '') || '';
  smsForm.smsSdkAppId = s.smsSdkAppId || '';

  emailForm.emailEnabled = !!s.emailEnabled;
  emailForm.emailHost = s.emailHost || '';
  emailForm.emailPort = s.emailPort !== null && s.emailPort !== undefined ? Number(s.emailPort) : 465;
  emailForm.emailUsername = s.emailUsername || '';
  emailForm.emailPassword = s.emailPassword || '';
  emailForm.emailFrom = s.emailFrom || '';
  emailForm.emailSsl = s.emailSsl !== null && s.emailSsl !== undefined ? !!s.emailSsl : true;
  emailForm.emailTemplate = s.emailTemplate || '';
};

const ensureCanUpdate = () => {
  if (!canUpdate.value) {
    MessagePlugin.warning('No permission to update verification settings.');
    return false;
  }
  return true;
};

const isInvalidSubmit = (ctx: any) =>
  ctx && typeof ctx === 'object' && 'validateResult' in ctx && ctx.validateResult !== true;

const onSubmitSms = async (ctx: any) => {
  if (!ensureCanUpdate()) return;
  if (isInvalidSubmit(ctx)) return;
  await request.post({
    url: '/system/ui',
    data: {
      smsEnabled: smsForm.smsEnabled,
      smsProvider: smsForm.smsProvider,
      smsAliyunEnabled: smsForm.smsAliyunEnabled,
      smsAliyunAccessKeyId: smsForm.smsAliyunAccessKeyId,
      smsAliyunAccessKeySecret: smsForm.smsAliyunAccessKeySecret,
      smsAliyunSignName: smsForm.smsAliyunSignName,
      smsAliyunTemplateCode: smsForm.smsAliyunTemplateCode,
      smsAliyunRegionId: smsForm.smsAliyunRegionId,
      smsAliyunEndpoint: smsForm.smsAliyunEndpoint,
      smsTencentEnabled: smsForm.smsTencentEnabled,
      smsTencentSecretId: smsForm.smsTencentSecretId,
      smsTencentSecretKey: smsForm.smsTencentSecretKey,
      smsTencentSignName: smsForm.smsTencentSignName,
      smsTencentTemplateId: smsForm.smsTencentTemplateId,
      smsTencentRegion: smsForm.smsTencentRegion,
      smsTencentEndpoint: smsForm.smsTencentEndpoint,
      smsSdkAppId: smsForm.smsSdkAppId,
    },
  });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

const onSubmitEmail = async (ctx: any) => {
  if (!ensureCanUpdate()) return;
  if (isInvalidSubmit(ctx)) return;
  await request.post({
    url: '/system/ui',
    data: {
      emailEnabled: emailForm.emailEnabled,
      emailHost: emailForm.emailHost,
      emailPort: emailForm.emailPort,
      emailUsername: emailForm.emailUsername,
      emailPassword: emailForm.emailPassword,
      emailFrom: emailForm.emailFrom,
      emailSsl: emailForm.emailSsl,
      emailTemplate: emailForm.emailTemplate,
    },
  });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

watch(
  activeTab,
  (value) => {
    if (route.query.tab === value) return;
    router.replace({ query: { ...route.query, tab: value } }).catch(() => {});
  },
  { immediate: true },
);

onMounted(() => {
  void smsProviderDict.load();
  load();
});
</script>
<style lang="less" scoped>
.verification-content {
  padding-top: 24px;

  :deep(.t-divider) {
    margin: var(--td-starter-gap-lg) 0 0;
  }
}
</style>
