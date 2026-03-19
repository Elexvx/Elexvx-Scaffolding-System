<template>
  <confirm-drawer
    :visible="visible"
    :header="drawerTitle"
    size="760px"
    :confirm-btn="{ content: '提交', loading: saving }"
    @update:visible="$emit('update:visible', $event)"
    @confirm="$emit('confirm')"
  >
    <t-form
      ref="formRef"
      :data="form"
      :rules="rules"
      layout="vertical"
      label-align="right"
      label-width="120px"
      class="download-drawer-form drawer-form--single"
      @submit="$emit('submit', $event)"
    >
      <t-row :gutter="[24, 24]">
        <t-col :xs="24" :sm="12">
          <t-form-item label="内容" name="content">
            <t-textarea v-model="form.content" :autosize="{ minRows: 2, maxRows: 4 }" placeholder="请描述文件用途或说明" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="文件" name="fileUrl">
            <div class="download-upload-panel">
              <t-upload
                v-model="innerFiles"
                :auto-upload="false"
                :max="1"
                theme="file"
                :accept="uploadAccept"
                :disabled="uploadingFile"
                :before-upload="(file) => emit('before-upload', file)"
                @select-change="(selected) => emit('select-change', selected)"
                @remove="emit('remove-upload')"
              />
              <div class="download-upload-tips">
                <p>支持 Office/PDF/图片等常见格式。</p>
                <p>编辑时可保留当前文件，如需替换请先移除再重新上传。</p>
                <p>文件会先缓存在本地，点击提交后开始分片上传并支持断点续传，建议单文件大小 10GB 内。</p>
              </div>
              <div v-if="formMode === 'edit' && form.fileUrl && !pendingFile" class="download-existing-file">
                <span>当前文件：</span>
                <t-link theme="primary" hover="color" @click="$emit('open-file', form.fileUrl)">
                  {{ form.fileName || '点击查看' }}
                </t-link>
                <span class="download-existing-hint">如需替换，请先移除再重新上传。</span>
              </div>
              <p v-if="uploadingFile" class="download-upload-caption">上传中 {{ uploadProgress }}%</p>
              <p v-else-if="uploadProgress > 0" class="download-upload-caption">已完成 {{ uploadProgress }}%</p>
              <p v-if="uploadError" class="download-upload-error">{{ uploadError }}</p>
            </div>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </confirm-drawer>
</template>

<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, SubmitContext, UploadFile } from 'tdesign-vue-next';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';

import type { DownloadFormState } from '../types';

const formRef = defineModel<FormInstanceFunctions | undefined>('formRef');
const innerFiles = defineModel<UploadFile[]>('files', { required: true });

defineProps<{
  visible: boolean;
  drawerTitle: string;
  form: DownloadFormState;
  rules: Record<string, FormRule[]>;
  saving: boolean;
  uploadAccept: string;
  uploadingFile: boolean;
  uploadProgress: number;
  uploadError: string;
  formMode: 'create' | 'edit';
  pendingFile: File | null;
}>();

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'confirm'): void;
  (event: 'submit', context: SubmitContext): void;
  (event: 'before-upload', file: UploadFile): boolean;
  (event: 'select-change', selected: File[]): void;
  (event: 'remove-upload'): void;
  (event: 'open-file', url: string): void;
}>();
</script>
