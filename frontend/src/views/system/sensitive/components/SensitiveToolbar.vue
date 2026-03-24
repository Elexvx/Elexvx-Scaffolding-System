<template>
  <div class="table-action-bar">
    <div class="action-left">
      <t-input v-model="innerWordCreateForm.word" placeholder="输入敏感词后回车或点击添加" clearable style="width: 320px" @enter="$emit('add-word')" />
      <t-button theme="primary" :loading="savingWord" @click="$emit('add-word')">添加</t-button>
      <sensitive-batch-actions
        v-model:import-files="innerImportFiles"
        :import-action="importAction"
        :upload-headers="uploadHeaders"
        :selected-count="selectedCount"
        :batch-deleting="batchDeleting"
        :progress-percent="progressPercent"
        @download-template="$emit('download-template')"
        @batch-delete="$emit('batch-delete')"
        @import-progress="$emit('import-progress', $event)"
        @import-success="$emit('import-success', $event)"
        @import-fail="$emit('import-fail')"
      />
    </div>
    <div class="action-right">
      <t-input v-model="innerWordSearchForm.keyword" placeholder="搜索敏感词" clearable style="width: 240px" @enter="$emit('search')">
        <template #suffixIcon>
          <t-icon name="search" style="cursor: pointer" @click="$emit('search')" />
        </template>
      </t-input>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProgressContext, SuccessContext, UploadFile } from 'tdesign-vue-next';

import type { SensitiveWordCreateForm, SensitiveWordSearchForm } from '../types';
import SensitiveBatchActions from './SensitiveBatchActions.vue';

const innerWordCreateForm = defineModel<SensitiveWordCreateForm>('wordCreateForm', { required: true });
const innerWordSearchForm = defineModel<SensitiveWordSearchForm>('wordSearchForm', { required: true });
const innerImportFiles = defineModel<UploadFile[]>('importFiles', { required: true });

defineProps<{
  savingWord: boolean;
  importAction: string;
  uploadHeaders: Record<string, string>;
  selectedCount: number;
  batchDeleting: boolean;
  progressPercent: number;
}>();

defineEmits<{
  (event: 'add-word'): void;
  (event: 'search'): void;
  (event: 'download-template'): void;
  (event: 'batch-delete'): void;
  (event: 'import-progress', context: ProgressContext): void;
  (event: 'import-success', context: SuccessContext): void;
  (event: 'import-fail'): void;
}>();
</script>
