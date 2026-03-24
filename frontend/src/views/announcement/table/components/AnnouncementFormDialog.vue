<template>
  <confirm-drawer v-model:visible="visibleProxy" :header="title" size="760px" :confirm-btn="{ content: '保存', loading: saving }" @confirm="$emit('confirm')">
    <t-form ref="formRef" class="drawer-form--single" layout="vertical" label-width="120px" label-align="right" :data="form" :rules="rules" @submit="$emit('submit', $event)">
      <t-row :gutter="[16, 12]">
        <t-col :xs="24" :sm="12"><t-form-item label="标题" name="title"><t-input v-model="form.title" placeholder="请输入标题" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="摘要" name="summary"><t-input v-model="form.summary" placeholder="用于列表展示的简要说明" /></t-form-item></t-col>
        <t-col :xs="24" :sm="6"><t-form-item label="类型" name="type"><t-select v-model="form.type" :options="typeOptions" /></t-form-item></t-col>
        <t-col :xs="24" :sm="6"><t-form-item label="优先级" name="priority"><t-select v-model="form.priority" :options="priorityOptions" /></t-form-item></t-col>
        <t-col :xs="24" :sm="6"><t-form-item label="状态" name="status"><t-select v-model="form.status" :options="statusOptions" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="封面图片" name="coverUrl"><t-upload v-model="filesProxy" action="/api/system/file/upload?page=announcement" name="file" :headers="uploadHeaders" :format-response="formatUploadResponse" theme="image" tips="请上传公告封面图" accept="image/*" :auto-upload="true" :locale="{ triggerUploadText: { image: '点击上传封面' } }" @success="$emit('upload-success', $event)" @fail="$emit('upload-fail', '封面图片', $event)" @remove="$emit('remove-cover')" /></t-form-item></t-col>
        <t-col :xs="24" :sm="12"><t-form-item label="附件" name="attachmentUrl"><t-upload v-model="attachmentFilesProxy" action="/api/system/file/upload?page=announcement" name="file" :headers="uploadHeaders" :format-response="formatUploadResponse" theme="file" tips="支持图片、PDF、Word、Excel、PPT等常见格式" :auto-upload="true" :max="1" accept=".pdf,.doc,.docx,.xls,.xlsx,.csv,.ppt,.pptx,.png,.jpg,.jpeg,.gif,.webp,image/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/csv,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation" :before-upload="beforeAttachmentUpload" @success="$emit('attachment-success', $event)" @fail="$emit('upload-fail', '附件', $event)" @remove="$emit('remove-attachment')" /></t-form-item></t-col>
        <t-col :xs="24"><t-form-item label="正文" name="content"><rich-text-editor v-model="form.content" :min-height="260" /></t-form-item></t-col>
      </t-row>
    </t-form>
  </confirm-drawer>
</template>
<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
const RichTextEditor = defineAsyncComponent(() => import('@/components/RichTextEditor.vue'));
const props = defineProps<{ visible: boolean; title: string; saving: boolean; formRef: any; form: Record<string, any>; rules: Record<string, any>; typeOptions: any[]; priorityOptions: any[]; statusOptions: any[]; files: any[]; attachmentFiles: any[]; uploadHeaders: Record<string, string>; formatUploadResponse: (res: any) => any; beforeAttachmentUpload: (file: any) => boolean; }>();
const emit = defineEmits(['update:visible','confirm','submit','upload-success','upload-fail','remove-cover','attachment-success','remove-attachment','update:files','update:attachment-files']);
const visibleProxy = computed({ get: () => props.visible, set: (value) => emit('update:visible', value) });
const filesProxy = computed({ get: () => props.files, set: (value) => emit('update:files', value) });
const attachmentFilesProxy = computed({ get: () => props.attachmentFiles, set: (value) => emit('update:attachment-files', value) });
</script>
