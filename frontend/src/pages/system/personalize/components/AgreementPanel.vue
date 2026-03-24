<template>
  <t-form :data="form" label-width="120px" layout="vertical" label-align="right" @submit.prevent>
    <t-row :gutter="[24, 16]">
      <t-col :span="24">
        <t-form-item label="用户协议" name="userAgreement">
          <rich-text-editor
            v-model="form.userAgreement"
            class="agreement-editor"
            placeholder="请输入用户协议内容"
            :min-height="32"
            :height="300"
          />
        </t-form-item>
      </t-col>
      <t-col :span="24">
        <t-form-item label="隐私协议" name="privacyAgreement">
          <rich-text-editor
            v-model="form.privacyAgreement"
            class="agreement-editor"
            placeholder="请输入隐私协议内容"
            :min-height="32"
            :height="300"
          />
        </t-form-item>
      </t-col>
      <t-col :span="24">
        <t-form-item>
          <t-space :size="12">
            <t-button theme="primary" :disabled="!isAdmin" @click="onSubmit">保存</t-button>
            <t-button theme="danger" variant="outline" :disabled="!isAdmin" @click="clearAgreement('user')">
              清空用户协议
            </t-button>
            <t-button theme="danger" variant="outline" :disabled="!isAdmin" @click="clearAgreement('privacy')">
              清空隐私协议
            </t-button>
          </t-space>
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>
</template>
<script setup lang="ts">
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, defineAsyncComponent, onMounted, reactive } from 'vue';

const RichTextEditor = defineAsyncComponent(() => import('@/components/RichTextEditor.vue'));
import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

const form = reactive({
  userAgreement: '',
  privacyAgreement: '',
});

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  form.userAgreement = s.userAgreement || '';
  form.privacyAgreement = s.privacyAgreement || '';
};

const saveAgreement = async () => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  await request.post({
    url: '/system/ui',
    data: {
      userAgreement: form.userAgreement,
      privacyAgreement: form.privacyAgreement,
    },
  });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

const onSubmit = async (ctx: any) => {
  if (ctx && ctx.validateResult !== true) return;
  await saveAgreement();
};

const clearAgreement = (type: 'user' | 'privacy') => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  const label = type === 'user' ? '用户协议' : '隐私协议';
  const dialog = DialogPlugin.confirm({
    header: '确认清空',
    body: `确定要清空${label}吗？此操作不可恢复。`,
    confirmBtn: '清空',
    cancelBtn: '取消',
    onConfirm: async () => {
      dialog.hide();
      if (type === 'user') {
        form.userAgreement = '';
      } else {
        form.privacyAgreement = '';
      }
      await saveAgreement();
    },
  });
};

onMounted(() => {
  load();
});
</script>
<style scoped lang="less">
.agreement-editor {
  :deep(.w-e-text) {
    padding: 4px 12px;
  }
}
</style>
