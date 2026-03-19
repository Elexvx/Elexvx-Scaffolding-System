<template>
  <t-dialog
    :visible="visible"
    width="90%"
    :header="previewTitle"
    placement="center"
    attach="body"
    :close-on-overlay-click="false"
    :close-on-esc-keydown="false"
    class="download-preview-dialog"
    @update:visible="$emit('update:visible', $event)"
  >
    <div class="download-preview-body">
      <t-alert v-if="isLocalhostOfficePreview" theme="warning" style="margin-bottom: 8px">
        当前处于本地开发环境，微软 Office 在线预览无法访问本地文件，请部署到公网测试。
      </t-alert>
      <iframe v-if="previewSource" :src="previewSource" />
      <div v-else class="download-preview-empty">当前文件暂不支持在线预览</div>
    </div>
    <template #footer>
      <div class="download-preview-footer">
        <t-button variant="outline" :disabled="!previewContext?.fileUrl" @click="$emit('open-tab')">新窗口打开</t-button>
        <t-button theme="primary" :disabled="!previewContext?.fileUrl" @click="$emit('download')">下载</t-button>
        <t-button variant="outline" @click="$emit('update:visible', false)">关闭</t-button>
      </div>
    </template>
  </t-dialog>
</template>

<script setup lang="ts">
import type { DownloadPreviewContext } from '../types';

defineProps<{
  visible: boolean;
  previewTitle: string;
  previewSource: string;
  isLocalhostOfficePreview: boolean;
  previewContext: DownloadPreviewContext | null;
}>();

defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'open-tab'): void;
  (event: 'download'): void;
}>();
</script>
