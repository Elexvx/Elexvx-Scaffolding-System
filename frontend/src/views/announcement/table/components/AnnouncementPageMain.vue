<template>
  <div class="announcement-table">
    <t-card title="公告管理" :bordered="false">
      <announcement-toolbar :query="query" :status-options="statusOptions" :priority-options="priorityOptions" :can-create="canCreate" @update:query="updateQuery" @search="handleSearch" @reset="reset" @create="openCreate" />
      <announcement-table :data="list" :columns="columns" :loading="loading" :pagination="pagination" :priority-theme="getPriorityTheme" :priority-label="priorityLabel" :status-theme="getStatusTheme" :status-label="statusLabel" :can-edit="canEdit" :can-delete="canDelete" :can-publish="canPublish" @page-change="onPageChange" @edit="openEdit" @toggle-publish="togglePublish" @delete="handleDelete" />
    </t-card>

    <announcement-form-dialog v-model:visible="formVisible" :title="drawerTitle" :saving="saving" :form-ref="formRef" :form="form" :rules="rules" :type-options="typeOptions" :priority-options="priorityOptions" :status-options="statusOptions" :files="files" :attachment-files="attachmentFiles" :upload-headers="uploadHeaders" :format-upload-response="formatUploadResponse" :before-attachment-upload="beforeAttachmentUpload" @confirm="confirmSubmit" @submit="submit" @upload-success="handleUploadSuccess" @upload-fail="handleUploadFail" @remove-cover="handleRemove" @attachment-success="handleAttachmentUploadSuccess" @remove-attachment="handleAttachmentRemove" @update:files="files = $event" @update:attachment-files="attachmentFiles = $event" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';

import AnnouncementFormDialog from './AnnouncementFormDialog.vue';
import AnnouncementTable from './AnnouncementTable.vue';
import AnnouncementToolbar from './AnnouncementToolbar.vue';
import { useAnnouncementColumns } from '../hooks/useAnnouncementColumns';
import { useAnnouncementDialog } from '../hooks/useAnnouncementDialog';
import { useAnnouncementForm } from '../hooks/useAnnouncementForm';
import { useAnnouncementPageState } from '../hooks/useAnnouncementPageState';
import { useAnnouncementPermissions } from '../hooks/useAnnouncementPermissions';
import { useAnnouncementSearchForm } from '../hooks/useAnnouncementSearchForm';
import { useAnnouncementTable } from '../hooks/useAnnouncementTable';
import { getPriorityLabel, getPriorityTheme, getStatusLabel, getStatusTheme } from '../utils/announcementMappers';

const { query } = useAnnouncementSearchForm();
let { list, total, loading, saving, formVisible, formMode, formRef, files, attachmentFiles, drawerTitle } = useAnnouncementPageState();
const { columns } = useAnnouncementColumns();
const { canCreate, canDelete, canEdit, canPublish } = useAnnouncementPermissions();
const { load, handleSearch, reset, onPageChange } = useAnnouncementTable({ query, list, total, loading });
const { form, rules, uploadHeaders, formatUploadResponse, typeOptions, priorityOptions, statusOptions, typeDict, priorityDict, statusDict, loadDictionaries, openCreate, openEdit, handleUploadSuccess, handleRemove, beforeAttachmentUpload, handleAttachmentUploadSuccess, handleAttachmentRemove, handleUploadFail, submit, confirmSubmit } = useAnnouncementForm({ formMode, formVisible, formRef, files, attachmentFiles, saving, reload: load });
const { togglePublish, handleDelete } = useAnnouncementDialog(load);
const pagination = computed(() => ({ current: query.page + 1, pageSize: query.size, total: total.value }));
const updateQuery = ({ key, value }: { key: string; value: string }) => {
  if (key === 'keyword' || key === 'priority' || key === 'status') query[key] = value;
};
const priorityLabel = (value?: string) => getPriorityLabel(value, priorityDict.items.value);
const statusLabel = (value?: string) => getStatusLabel(value, statusDict.items.value);

onMounted(() => {
  void loadDictionaries();
  void load();
});
</script>

<style scoped lang="less">
.announcement-table {
  :deep(.t-card__title) {
    font-weight: 600;
  }
}
</style>
