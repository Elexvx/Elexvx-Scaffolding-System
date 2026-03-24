<template>
  <div class="notification-table">
    <t-card title="通知管理" :bordered="false">
      <notification-toolbar :query="query" :status-options="statusOptions" :priority-options="priorityOptions" :can-create="canCreate" @update:query="updateQuery" @search="handleSearch" @reset="reset" @create="openCreate" />
      <notification-table :data="list" :columns="columns" :loading="loading" :pagination="pagination" :priority-theme="getPriorityTheme" :priority-label="priorityLabel" :status-theme="getStatusTheme" :status-label="statusLabel" :can-edit="canEdit" :can-delete="canDelete" :can-publish="canPublish" @page-change="onPageChange" @edit="openEdit" @toggle-publish="togglePublish" @delete="handleDelete" />
    </t-card>

    <notification-form-dialog v-model:visible="formVisible" :title="drawerTitle" :saving="saving" :form-ref="formRef" :form="form" :rules="rules" :type-options="typeOptions" :priority-options="priorityOptions" :status-options="statusOptions" :files="files" :attachment-files="attachmentFiles" :upload-headers="uploadHeaders" :format-upload-response="formatUploadResponse" :before-attachment-upload="beforeAttachmentUpload" @confirm="confirmSubmit" @submit="submit" @upload-success="handleUploadSuccess" @upload-fail="handleUploadFail" @remove-cover="handleRemove" @attachment-success="handleAttachmentUploadSuccess" @remove-attachment="handleAttachmentRemove" @update:files="files = $event" @update:attachment-files="attachmentFiles = $event" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';

import NotificationFormDialog from './NotificationFormDialog.vue';
import NotificationTable from './NotificationTable.vue';
import NotificationToolbar from './NotificationToolbar.vue';
import { useNotificationColumns } from '../hooks/useNotificationColumns';
import { useNotificationDialog } from '../hooks/useNotificationDialog';
import { useNotificationForm } from '../hooks/useNotificationForm';
import { useNotificationPageState } from '../hooks/useNotificationPageState';
import { useNotificationPermissions } from '../hooks/useNotificationPermissions';
import { useNotificationSearchForm } from '../hooks/useNotificationSearchForm';
import { useNotificationTable } from '../hooks/useNotificationTable';
import { getPriorityLabel, getPriorityTheme, getStatusLabel, getStatusTheme } from '../utils/notificationMappers';

const { query } = useNotificationSearchForm();
let { list, total, loading, saving, formVisible, formMode, formRef, files, attachmentFiles, drawerTitle } = useNotificationPageState();
const { columns } = useNotificationColumns();
const { canCreate, canDelete, canEdit, canPublish } = useNotificationPermissions();
const { load, handleSearch, reset, onPageChange } = useNotificationTable({ query, list, total, loading });
const { form, rules, uploadHeaders, formatUploadResponse, typeOptions, priorityOptions, statusOptions, typeDict, priorityDict, statusDict, loadDictionaries, openCreate, openEdit, handleUploadSuccess, handleRemove, beforeAttachmentUpload, handleAttachmentUploadSuccess, handleAttachmentRemove, handleUploadFail, submit, confirmSubmit } = useNotificationForm({ formMode, formVisible, formRef, files, attachmentFiles, saving, reload: load });
const { togglePublish, handleDelete } = useNotificationDialog(load);
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
.notification-table {
  :deep(.t-card__title) {
    font-weight: 600;
  }
}
</style>
