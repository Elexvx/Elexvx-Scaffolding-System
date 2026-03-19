<template>
  <div class="download-page">
    <t-card title="下载中心" :bordered="false">
      <download-toolbar :can-create="canCreate" @create="openCreate" @refresh="load" />
      <download-table
        :columns="columns"
        :list="list"
        :loading="loading"
        :pagination="pagination"
        :query="query"
        :can-update="canUpdate"
        :can-delete="canDelete"
        @page-change="onPageChange"
        @preview="openPreview"
        @edit="openEdit"
        @delete="remove"
      />
    </t-card>

    <download-form-dialog
      v-model:visible="formVisible"
      v-model:form-ref="formRef"
      v-model:files="files"
      :drawer-title="drawerTitle"
      :form="form"
      :rules="rules"
      :saving="saving"
      :upload-accept="uploadAccept"
      :uploading-file="uploadingFile"
      :upload-progress="uploadProgress"
      :upload-error="uploadError"
      :form-mode="formMode"
      :pending-file="pendingFile"
      @confirm="confirmSubmit"
      @submit="submit"
      @before-upload="handleBeforeUpload"
      @select-change="handleSelectChange"
      @remove-upload="handleUploadRemove"
      @open-file="openFile"
    />

    <download-detail-drawer
      v-model:visible="previewVisible"
      :preview-title="previewTitle"
      :preview-source="previewSource"
      :is-localhost-office-preview="isLocalhostOfficePreview"
      :preview-context="previewContext"
      @open-tab="openPreviewInNewTab"
      @download="downloadFromPreview"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue';

import { useDownloadColumns } from '../hooks/useDownloadColumns';
import { useDownloadDialog } from '../hooks/useDownloadDialog';
import { useDownloadForm } from '../hooks/useDownloadForm';
import { useDownloadPageState } from '../hooks/useDownloadPageState';
import { useDownloadPermissions } from '../hooks/useDownloadPermissions';
import { useDownloadSearchForm } from '../hooks/useDownloadSearchForm';
import { useDownloadTable } from '../hooks/useDownloadTable';
import { uploadAccept } from '../constants/downloadOptions';
import { buildAbsoluteUrl, isOfficeFile, resolveSuffix } from '../utils/downloadGuards';
import DownloadDetailDrawer from './DownloadDetailDrawer.vue';
import DownloadFormDialog from './DownloadFormDialog.vue';
import DownloadTable from './DownloadTable.vue';
import DownloadToolbar from './DownloadToolbar.vue';

const { query } = useDownloadSearchForm();
const {
  list,
  total,
  loading,
  saving,
  formVisible,
  formMode,
  currentId,
  formRef,
  files,
  pendingFile,
  uploadProgress,
  uploadingFile,
  uploadError,
  uploadSessionId,
  previewVisible,
  previewContext,
  previewBodyOverflowBackup,
  previewMainOverflowBackup,
  pagination: paginationBuilder,
} = useDownloadPageState();
const pagination = computed(() => paginationBuilder(query).value);
const { columns } = useDownloadColumns();
const { canQuery, canCreate, canUpdate, canDelete } = useDownloadPermissions();
const { load, onPageChange, remove } = useDownloadTable({ query, list, total, loading });
const { restoreScrollLock } = useDownloadDialog(previewVisible, previewBodyOverflowBackup, previewMainOverflowBackup);
const {
  form,
  rules,
  drawerTitle,
  submit,
  openCreate,
  openEdit,
  resetUploadState,
  handleBeforeUpload,
  handleSelectChange,
  handleUploadRemove,
  confirmSubmit,
} = useDownloadForm({
  formRef,
  formVisible,
  formMode,
  currentId,
  files,
  pendingFile,
  uploadProgress,
  uploadingFile,
  uploadError,
  uploadSessionId,
  saving,
  reload: load,
});

const previewTitle = computed(() => `文件预览 - ${previewContext.value?.fileName || ''}`);
const isLocalhostOfficePreview = computed(() => {
  const suffix = previewContext.value?.suffix;
  if (!suffix || !isOfficeFile(suffix)) return false;
  if (typeof window === 'undefined') return false;
  const host = window.location.hostname.toLowerCase();
  return host === 'localhost' || host === '127.0.0.1';
});

const previewSource = computed(() => {
  if (!previewContext.value?.fileUrl) return '';
  const fileUrl = buildAbsoluteUrl(previewContext.value.fileUrl);
  const suffix = previewContext.value.suffix || resolveSuffix(previewContext.value.fileName);
  if (isOfficeFile(suffix)) {
    return `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fileUrl)}`;
  }
  if (suffix === 'pdf') return fileUrl;
  return '';
});

const openPreview = (row: any) => {
  previewContext.value = {
    fileUrl: row.fileUrl,
    fileName: row.fileName,
    suffix: row.suffix || resolveSuffix(row.fileName),
  };
  previewVisible.value = true;
};

const openFile = (url: string) => {
  if (!url) return;
  window.open(buildAbsoluteUrl(url), '_blank', 'noopener,noreferrer');
};

const openPreviewInNewTab = () => {
  if (!previewSource.value) return;
  window.open(previewSource.value, '_blank', 'noopener,noreferrer');
};

const downloadFromPreview = () => {
  if (!previewContext.value?.fileUrl) return;
  window.open(buildAbsoluteUrl(previewContext.value.fileUrl), '_blank', 'noopener,noreferrer');
};

watch(formVisible, async (visible) => {
  if (!visible) {
    await resetUploadState();
  }
});

watch(previewVisible, (visible) => {
  if (!visible) {
    previewContext.value = null;
    restoreScrollLock();
  }
});

onMounted(() => {
  if (canQuery.value) {
    load();
  }
});
</script>
<style scoped lang="less" src="./DownloadPageMain.less"></style>
