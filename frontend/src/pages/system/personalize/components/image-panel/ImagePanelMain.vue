<template>
  <t-form :data="form" label-width="140px" layout="vertical" label-align="right" @submit="onSubmit">
    <t-row :gutter="[24, 16]">
      <image-upload-section
        v-model="form.logoExpandedUrl"
        field="logoExpandedUrl"
        label="侧边展开图标"
        :help="LOGO_CROP_SPECS.logoExpandedUrl.help"
        placeholder="图片URL，展开模式使用"
        :disabled="!isAdmin"
      >
        <template #upload>
          <input ref="logoExpandedInputRef" class="logo-file-input" type="file" accept="image/*" :disabled="!isAdmin" @change="(event) => handleLogoSelect('logoExpandedUrl', event)" />
          <t-button class="image-panel__upload-btn" variant="outline" :disabled="!isAdmin" @click="triggerLogoSelect('logoExpandedUrl')">上传</t-button>
        </template>
      </image-upload-section>

      <image-upload-section
        v-model="form.logoCollapsedUrl"
        field="logoCollapsedUrl"
        label="侧边折叠图标"
        :help="LOGO_CROP_SPECS.logoCollapsedUrl.help"
        placeholder="图片URL，折叠模式使用"
        :disabled="!isAdmin"
      >
        <template #upload>
          <input ref="logoCollapsedInputRef" class="logo-file-input" type="file" accept="image/*" :disabled="!isAdmin" @change="(event) => handleLogoSelect('logoCollapsedUrl', event)" />
          <t-button class="image-panel__upload-btn" variant="outline" :disabled="!isAdmin" @click="triggerLogoSelect('logoCollapsedUrl')">上传</t-button>
        </template>
      </image-upload-section>

      <image-upload-section v-model="form.loginBgUrl" field="loginBgUrl" label="登录页背景图" placeholder="图片URL，登录页右侧背景" :disabled="!isAdmin">
        <template #upload>
          <t-upload class="image-panel__upload" :action="uiUploadUrl" accept="image/*" :auto-upload="true" :max="1" :disabled="!isAdmin" :format-response="formatUploadResponse" @success="(ctx) => onUploaded('loginBgUrl', ctx)">
            <t-button class="image-panel__upload-btn" variant="outline">上传</t-button>
          </t-upload>
        </template>
      </image-upload-section>

      <image-upload-section
        v-model="form.qrCodeUrl"
        field="qrCodeUrl"
        label="二维码"
        placeholder="图片URL，用于侧边栏展示"
        :disabled="!isAdmin"
        :preview-style="{ maxHeight: '120px', maxWidth: '100%', objectFit: 'contain' }"
      >
        <template #upload>
          <input ref="qrInputRef" class="logo-file-input" type="file" accept="image/*" :disabled="!isAdmin" @change="handleQrSelect" />
          <t-button class="image-panel__upload-btn" variant="outline" :disabled="!isAdmin" @click="qrInputRef?.click()">上传</t-button>
        </template>
      </image-upload-section>

      <t-col :span="24">
        <t-form-item>
          <t-space :size="12">
            <t-button type="submit" theme="primary" :disabled="!isAdmin">保存</t-button>
          </t-space>
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>

  <image-preview-section
    v-model:visible="cropDialogVisible"
    :cropper-ref="cropperRef"
    :frame-style="cropFrameStyle"
    :hint="cropDialogHint"
    :image-url="cropImageUrl"
    :loading="cropUploading"
    :spec="activeCropSpec"
    :title="cropDialogTitle"
    @confirm="handleCropConfirm"
  />
</template>

<script setup lang="ts">
import type { SuccessContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted } from 'vue';

import { useSettingStore } from '@/store';
import { request } from '@/utils/request';

import { LOGO_CROP_SPECS } from './constants/imagePanelOptions';
import { useImagePanelForm } from './hooks/useImagePanelForm';
import { useImagePanelPermissions } from './hooks/useImagePanelPermissions';
import { useImagePreview } from './hooks/useImagePreview';
import { useImageUpload } from './hooks/useImageUpload';
import ImagePreviewSection from './ImagePreviewSection.vue';
import ImageUploadSection from './ImageUploadSection.vue';
import { formatUploadResponse, resolveUploadedUrl } from './utils/imagePanelMappers';
import type { ImageField, LogoField } from './types';

const settingStore = useSettingStore();
const { form, load, buildPayload } = useImagePanelForm();
const { isAdmin, token } = useImagePanelPermissions();
const uiUploadUrl = `${String(import.meta.env.VITE_API_URL_PREFIX || '/api').replace(/\/$/, '')}/system/ui/upload?page=system-personalize`;

const uploadFile = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  const headers: Record<string, string> = {};
  if (token.value) headers.Authorization = token.value;
  const response = await fetch(uiUploadUrl, { method: 'POST', body: formData, headers, credentials: 'include' });
  const payload = await response.json().catch(() => null);
  if (!response.ok || payload?.code !== 0) throw new Error(payload?.message || '上传失败，请重试');
  return payload?.data?.url as string;
};

const assignUrl = (field: LogoField | 'qrCodeUrl', url: string) => {
  form[field] = url;
};
const {
  cropDialogVisible,
  cropField,
  cropImageUrl,
  cropUploading,
  cropperRef,
  handleCropConfirm,
  handleLogoSelect,
  handleQrSelect,
  logoCollapsedInputRef,
  logoExpandedInputRef,
  qrInputRef,
  triggerLogoSelect,
} = useImageUpload(uploadFile, assignUrl);
const { activeCropSpec, cropDialogHint, cropDialogTitle, cropFrameStyle } = useImagePreview(cropField);

const onUploaded = (field: ImageField, ctx: SuccessContext) => {
  const url = resolveUploadedUrl(ctx);
  if (url) form[field] = url;
};

const onSubmit = async (ctx: any) => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  if (ctx && ctx.validateResult !== true) return;
  try {
    await request.post({ url: '/system/ui', data: buildPayload() });
    await settingStore.loadUiSetting();
    MessagePlugin.success('保存成功');
  } catch (err: any) {
    MessagePlugin.error(err?.message || '保存失败');
  }
};

onMounted(() => {
  void load();
});
</script>

<style scoped lang="less">
.logo-file-input {
  display: none;
}

.image-panel__upload {
  flex: 0 0 auto;
}

.image-panel__upload-btn {
  width: 72px;
  padding-left: 0;
  padding-right: 0;
  flex: 0 0 72px;
}
</style>
