<template>
  <t-button variant="outline" theme="default" @click="$emit('download-template')">
    <template #icon><t-icon name="download" /></template>
    下载模板
  </t-button>
  <t-button theme="danger" variant="outline" :disabled="selectedCount === 0" :loading="batchDeleting" @click="$emit('batch-delete')">
    <template #icon><t-icon name="delete" /></template>
    批量删除（{{ selectedCount }}）
  </t-button>
  <t-upload
    v-model="innerImportFiles"
    :action="importAction"
    :headers="uploadHeaders"
    theme="file"
    :auto-upload="true"
    :use-mock-progress="true"
    :mock-progress-duration="80"
    :max="1"
    accept=".xlsx,.xls,.csv,.txt"
    @progress="$emit('import-progress', $event)"
    @success="$emit('import-success', $event)"
    @fail="$emit('import-fail')"
  >
    <t-button variant="outline">
      <template #icon><t-icon name="upload" /></template>
      批量导入
    </t-button>
  </t-upload>
  <span class="import-tips">支持 Excel/CSV/TXT</span>
  <span v-if="progressPercent > 0 && progressPercent < 100" class="import-progress">{{ progressPercent }}%</span>
</template>

<script setup lang="ts">
import type { UploadFile } from 'tdesign-vue-next';

const innerImportFiles = defineModel<UploadFile[]>('importFiles', { required: true });

defineProps<{
  importAction: string;
  uploadHeaders: Record<string, string>;
  selectedCount: number;
  batchDeleting: boolean;
  progressPercent: number;
}>();

defineEmits([
  'download-template',
  'batch-delete',
  'import-progress',
  'import-success',
  'import-fail',
]);
</script>
