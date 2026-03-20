<template>
  <t-card title="对象存储" :bordered="false">
    <t-space direction="vertical" size="large" style="width: 100%">
      <t-alert
        theme="info"
        message="可在此配置系统上传能力，支持本地存储与多种对象存储服务。保存后新的上传会自动按最新配置执行。"
        :close="false"
      />
      <t-form ref="formRef" :data="form" :rules="rules" layout="vertical" label-align="right" label-width="140px" colon>
        <t-row :gutter="[24, 16]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="存储类型" name="provider">
              <t-select
                v-model="form.provider"
                :options="providerOptions"
                placeholder="选择存储类型"
                :disabled="!canEdit"
                style="max-width: 500px; width: 100%"
                @change="handleProviderChange"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="命名空间/桶" name="bucket" help="对象存储 Bucket 或存储桶名称">
              <t-input
                v-model="form.bucket"
                :disabled="!canEdit || form.provider === 'LOCAL'"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="所属 Region" name="region" help="部分服务商必填">
              <t-input
                v-model="form.region"
                :disabled="!canEdit || form.provider === 'LOCAL'"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="Endpoint" name="endpoint" help="可选，用于指定私有域名">
              <t-input
                v-model="form.endpoint"
                :disabled="!canEdit || form.provider === 'LOCAL'"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="Access Key" name="accessKey">
              <t-input
                v-model="form.accessKey"
                :disabled="!canEdit || form.provider === 'LOCAL'"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="Secret Key" name="secretKey" help="留空则保持原配置">
              <t-input
                v-model="form.secretKey"
                :placeholder="secretPlaceholder"
                type="password"
                :disabled="!canEdit || form.provider === 'LOCAL'"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="自定义域名" name="customDomain" help="返回的访问地址将使用该域名">
              <t-input
                v-model="form.customDomain"
                :disabled="!canEdit"
                placeholder="https://static.example.com"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="路径前缀" name="pathPrefix" help="可为空，如 uploads/static">
              <t-input v-model="form.pathPrefix" :disabled="!canEdit" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="文件公开读取" name="publicRead">
              <t-switch v-model="form.publicRead" :disabled="!canEdit" />
            </t-form-item>
          </t-col>

          <t-col :span="24">
            <t-form-item>
              <t-space>
                <t-button variant="outline" :loading="loading" @click="loadSetting">刷新</t-button>
                <t-button theme="default" :disabled="!canEdit" :loading="testing" @click="handleTest">
                  连接测试
                </t-button>
                <t-button theme="primary" :disabled="!canEdit" :loading="saving" @click="handleSave">保存配置</t-button>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-space>
  </t-card>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, SelectOption } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import type { StorageSetting } from '@/api/system/storage';
import { fetchStorageSetting, saveStorageSetting, testStorageSetting } from '@/api/system/storage';
import { useDictionary } from '@/hooks/useDictionary';
import { useUserStore } from '@/store';
import { buildDictOptions } from '@/utils/dict';

const formRef = ref<FormInstanceFunctions>();
const loading = ref(false);
const saving = ref(false);
const testing = ref(false);
const secretConfigured = ref(false);
const secretPlaceholder = computed(() => (secretConfigured.value ? '已配置，留空保持不变' : '请输入 Secret Key'));

const form = reactive<StorageSetting>({
  provider: 'LOCAL',
  bucket: '',
  region: '',
  endpoint: '',
  accessKey: '',
  secretKey: '',
  customDomain: '',
  pathPrefix: '',
  publicRead: true,
  reuseSecret: true,
});

const providerDict = useDictionary('storage_provider');
const fallbackProviderOptions: SelectOption[] = [
  { label: '本地存储', value: 'LOCAL' },
  { label: '阿里云 OSS', value: 'ALIYUN' },
  { label: '腾讯云 COS', value: 'TENCENT' },
];
const PROVIDER_VALUES = ['LOCAL', 'ALIYUN', 'TENCENT'];
const providerOptions = computed(() =>
  buildDictOptions(providerDict.items.value, fallbackProviderOptions, PROVIDER_VALUES),
);

const userStore = useUserStore();
const canEdit = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const rules: Record<string, FormRule[]> = {
  provider: [{ required: true, message: '请选择存储类型', type: 'error' }],
  bucket: [
    {
      validator: (val: string) => form.provider === 'LOCAL' || !!val,
      message: 'Bucket 不能为空',
      type: 'error',
    },
  ],
  accessKey: [
    {
      validator: (val: string) => form.provider === 'LOCAL' || !!val,
      message: 'Access Key 不能为空',
      type: 'error',
    },
  ],
  secretKey: [
    {
      validator: (val: string) => form.provider === 'LOCAL' || !!val || secretConfigured.value,
      message: 'Secret Key 不能为空',
      type: 'error',
    },
  ],
  region: [
    {
      validator: (val: string) => form.provider !== 'TENCENT' || !!val,
      message: 'Region 不能为空',
      type: 'error',
    },
  ],
};

const handleProviderChange = () => {
  if (form.provider === 'LOCAL') {
    form.bucket = '';
    form.region = '';
    form.endpoint = '';
    form.accessKey = '';
    form.secretKey = '';
  }
};

const loadSetting = async () => {
  loading.value = true;
  try {
    const data = await fetchStorageSetting();
    Object.assign(form, data || {});
    form.secretKey = '';
    secretConfigured.value = !!data?.secretConfigured;
  } finally {
    loading.value = false;
  }
};

const buildPayload = () => ({
  ...form,
  reuseSecret: secretConfigured.value && !form.secretKey,
});

const handleSave = async () => {
  const result = await formRef.value?.validate();
  if (result !== true) return;
  saving.value = true;
  try {
    const payload = buildPayload();
    const saved = await saveStorageSetting(payload);
    secretConfigured.value = !!saved?.secretConfigured || !!form.secretKey;
    form.secretKey = '';
    MessagePlugin.success('配置已保存');
  } catch (err: any) {
    const msg = err.message || 'Save failed, please check the configuration.';
    MessagePlugin.error(msg.replace(/\s*\[\d+\]$/, ''));
  } finally {
    saving.value = false;
  }
};

const handleTest = async () => {
  const result = await formRef.value?.validate();
  if (result !== true) return;
  testing.value = true;
  try {
    await testStorageSetting(buildPayload());
    MessagePlugin.success('连接正常');
  } catch (err: any) {
    const msg = err.message || '连接失败，请检查配置';
    // 移除错误消息末尾的 [code]
    MessagePlugin.error(msg.replace(/\s*\[\d+\]$/, ''));
  } finally {
    testing.value = false;
  }
};

onMounted(() => {
  void providerDict.load();
  loadSetting();
});
</script>
<style scoped>
.mb-3 {
  margin-bottom: 16px;
}
</style>
