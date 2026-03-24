<template>
  <div class="message-send">
    <t-card title="发送通知" :bordered="false">
      <t-space direction="vertical" size="large" style="width: 100%">
        <t-form
          ref="formRef"
          :data="form"
          :rules="rules"
          label-align="right"
          label-width="120px"
          colon
          @submit="onSubmit"
        >
          <t-row :gutter="[24, 16]">
            <t-col :xs="24" :sm="12">
              <t-form-item label="发送范围" name="scope">
                <t-radio-group v-model="form.scope" variant="default-filled">
                  <t-radio-button value="single">指定用户</t-radio-button>
                  <t-radio-button value="broadcast">全员广播</t-radio-button>
                </t-radio-group>
              </t-form-item>
            </t-col>
            <t-col v-if="form.scope === 'single'" :xs="24" :sm="12">
              <t-form-item label="接收用户 ID" name="toUserId">
                <t-input-number v-model="form.toUserId" :min="1" style="max-width: 500px; width: 100%" />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="优先级" name="quality">
                <t-select v-model="form.quality" :options="qualityOptions" style="max-width: 500px; width: 100%" />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="内容" name="content">
                <t-textarea
                  v-model="form.content"
                  placeholder="请输入通知内容"
                  style="max-width: 500px; width: 100%"
                  :autosize="{ minRows: 3, maxRows: 6 }"
                />
              </t-form-item>
            </t-col>
            <t-col :span="24">
              <t-form-item>
                <t-space>
                  <t-button theme="primary" type="submit" :loading="sending">发送</t-button>
                  <t-button variant="outline" @click="reset">重置</t-button>
                </t-space>
              </t-form-item>
            </t-col>
          </t-row>
        </t-form>
      </t-space>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref } from 'vue';

import { broadcastMessage, sendMessage } from '@/api/message';
import { useDictionary } from '@/hooks/useDictionary';
import { useNotificationStore } from '@/store';
import { buildDictOptions } from '@/utils/dict';

type ScopeType = 'single' | 'broadcast';

const formRef = ref<FormInstanceFunctions>();
const sending = ref(false);
const notificationStore = useNotificationStore();
const form = reactive({
  scope: 'single' as ScopeType,
  toUserId: undefined as number | undefined,
  quality: 'middle',
  content: '',
});

const qualityDict = useDictionary('message_quality');
const fallbackQualityOptions = [
  { label: '高', value: 'high' },
  { label: '中', value: 'middle' },
  { label: '低', value: 'low' },
];
const qualityOptions = computed(() => buildDictOptions(qualityDict.items.value, fallbackQualityOptions));
void qualityDict.load();

const rules = computed<Record<string, FormRule[]>>(() => {
  const result: Record<string, FormRule[]> = {
    scope: [{ required: true, message: '请选择发送范围', type: 'error' }],
    quality: [{ required: true, message: '请选择优先级', type: 'error' }],
    content: [{ required: true, message: '请输入通知内容', type: 'error' }],
  };
  if (form.scope === 'single') {
    result.toUserId = [{ required: true, message: '请输入接收用户 ID', type: 'error' }];
  }
  return result;
});

const reset = () => {
  form.scope = 'single';
  form.toUserId = undefined;
  form.quality = 'middle';
  form.content = '';
  formRef.value?.reset();
};

const onSubmit = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) return;
  sending.value = true;
  try {
    if (form.scope === 'broadcast') {
      await broadcastMessage({ content: form.content, quality: form.quality });
      MessagePlugin.success('发送成功');
    } else {
      await sendMessage({ toUserId: Number(form.toUserId), content: form.content, quality: form.quality });
      MessagePlugin.success('发送成功');
    }
    try {
      await notificationStore.fetchMessages();
    } catch {
      // ignore refresh errors
    }
    reset();
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '发送失败'));
  } finally {
    sending.value = false;
  }
};
</script>
<style scoped lang="less">
.message-send {
  :deep(.t-card__title) {
    font-weight: 600;
  }
}
</style>
