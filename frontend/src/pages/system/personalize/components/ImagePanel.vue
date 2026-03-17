<template>
  <t-form :data="form" label-width="140px" layout="vertical" label-align="right" @submit="onSubmit">
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12">
        <t-form-item label="侧边展开图标" name="logoExpandedUrl" :help="logoCropSpecs.logoExpandedUrl.help">
          <t-space direction="vertical" :size="8" class="image-panel__url-wrap">
            <div class="image-panel__url-row">
              <t-input
                v-model="form.logoExpandedUrl"
                class="image-panel__url-input"
                placeholder="图片URL，展开模式使用"
              />
              <input
                ref="logoExpandedInputRef"
                class="logo-file-input"
                type="file"
                accept="image/*"
                :disabled="!isAdmin"
                @change="(event) => handleLogoSelect('logoExpandedUrl', event)"
              />
              <t-button
                class="image-panel__upload-btn"
                variant="outline"
                :disabled="!isAdmin"
                @click="triggerLogoSelect('logoExpandedUrl')"
              >
                上传
              </t-button>
            </div>
            <div v-if="form.logoExpandedUrl" class="logo-preview-container">
              <img
                :src="form.logoExpandedUrl"
                alt="Expanded Logo Preview"
                class="logo-preview-img"
                style="max-height: 40px; max-width: 100%; object-fit: contain"
              />
            </div>
          </t-space>
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="侧边折叠图标" name="logoCollapsedUrl" :help="logoCropSpecs.logoCollapsedUrl.help">
          <t-space direction="vertical" :size="8" class="image-panel__url-wrap">
            <div class="image-panel__url-row">
              <t-input
                v-model="form.logoCollapsedUrl"
                class="image-panel__url-input"
                placeholder="图片URL，折叠模式使用"
              />
              <input
                ref="logoCollapsedInputRef"
                class="logo-file-input"
                type="file"
                accept="image/*"
                :disabled="!isAdmin"
                @change="(event) => handleLogoSelect('logoCollapsedUrl', event)"
              />
              <t-button
                class="image-panel__upload-btn"
                variant="outline"
                :disabled="!isAdmin"
                @click="triggerLogoSelect('logoCollapsedUrl')"
              >
                上传
              </t-button>
            </div>
            <div v-if="form.logoCollapsedUrl" class="logo-preview-container">
              <img
                :src="form.logoCollapsedUrl"
                alt="Collapsed Logo Preview"
                class="logo-preview-img"
                style="max-height: 40px; max-width: 100%; object-fit: contain"
              />
            </div>
          </t-space>
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="登录页背景图" name="loginBgUrl">
          <t-space direction="vertical" :size="8" class="image-panel__url-wrap">
            <div class="image-panel__url-row">
              <t-input v-model="form.loginBgUrl" class="image-panel__url-input" placeholder="图片URL，登录页右侧背景" />
              <t-upload
                class="image-panel__upload"
                :action="uiUploadUrl"
                accept="image/*"
                :auto-upload="true"
                :max="1"
                :disabled="!isAdmin"
                :format-response="formatUpload"
                @success="(ctx) => onUploaded('loginBgUrl', ctx)"
              >
                <t-button class="image-panel__upload-btn" variant="outline">上传</t-button>
              </t-upload>
            </div>
            <div v-if="form.loginBgUrl" class="logo-preview-container">
              <img
                :src="form.loginBgUrl"
                alt="Login Background Preview"
                class="logo-preview-img"
                style="max-height: 40px; max-width: 100%; object-fit: contain"
              />
            </div>
          </t-space>
        </t-form-item>
      </t-col>

      <t-col :xs="24" :sm="12">
        <t-form-item label="二维码" name="qrCodeUrl">
          <t-space direction="vertical" :size="8" class="image-panel__url-wrap">
            <div class="image-panel__url-row">
              <t-input
                v-model="form.qrCodeUrl"
                class="image-panel__url-input"
                placeholder="图片URL，用于侧边栏展示"
                :disabled="!isAdmin"
              />
              <input
                ref="qrInputRef"
                class="logo-file-input"
                type="file"
                accept="image/*"
                :disabled="!isAdmin"
                @change="handleQrSelect"
              />
              <t-button
                class="image-panel__upload-btn"
                variant="outline"
                :disabled="!isAdmin"
                @click="triggerQrSelect"
              >
                上传
              </t-button>
            </div>
            <div v-if="form.qrCodeUrl" class="logo-preview-container">
              <img
                :src="form.qrCodeUrl"
                alt="QR Code Preview"
                class="logo-preview-img"
                style="max-height: 120px; max-width: 100%; object-fit: contain"
              />
            </div>
          </t-space>
        </t-form-item>
      </t-col>

      <t-col :span="24">
        <t-form-item>
          <t-space :size="12">
            <t-button type="submit" theme="primary" :disabled="!isAdmin">保存</t-button>
          </t-space>
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>

  <t-dialog
    v-model:visible="cropDialogVisible"
    :header="cropDialogTitle"
    :confirm-btn="{ content: '裁切并上传', loading: cropUploading, disabled: !cropImageUrl }"
    :cancel-btn="{ content: '取消', theme: 'default' }"
    width="640px"
    @confirm="handleCropConfirm"
  >
    <div class="logo-cropper">
      <div class="logo-cropper-frame" :style="cropFrameStyle">
        <vue-cropper
          v-if="cropImageUrl"
          ref="cropperRef"
          :img="cropImageUrl"
          :output-size="1"
          output-type="png"
          :auto-crop="true"
          :auto-crop-width="activeCropSpec?.width"
          :auto-crop-height="activeCropSpec?.height"
          :fixed="true"
          :fixed-number="[activeCropSpec?.width, activeCropSpec?.height]"
          :center-box="true"
          :info="true"
          mode="contain"
        ></vue-cropper>
        <div v-else class="logo-cropper-empty">请先选择图片</div>
      </div>
      <div class="logo-cropper-controls">
        <div class="logo-cropper-hint">{{ cropDialogHint }}</div>
      </div>
    </div>
  </t-dialog>
</template>
<script setup lang="ts">
import 'vue-cropper/dist/index.css';

import type { SuccessContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { VueCropper } from 'vue-cropper';

import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

const form = reactive({
  logoExpandedUrl: '',
  logoCollapsedUrl: '',
  loginBgUrl: '',
  qrCodeUrl: '',
  faviconUrl: '',
});

type LogoField = 'logoExpandedUrl' | 'logoCollapsedUrl';

const uiUploadUrl = `${String(import.meta.env.VITE_API_URL_PREFIX || '/api').replace(/\/$/, '')}/system/ui/upload?page=system-personalize`;

const logoCropSpecs: Record<LogoField, { label: string; width: number; height: number; help: string }> = {
  logoExpandedUrl: {
    label: '侧边展开图标',
    width: 188,
    height: 26,
    help: '尺寸 188×26，上传后可拖拽裁切',
  },
  logoCollapsedUrl: {
    label: '侧边折叠图标',
    width: 32,
    height: 32,
    help: '尺寸 32×32，上传后可拖拽裁切',
  },
};

const logoExpandedInputRef = ref<HTMLInputElement | null>(null);
const logoCollapsedInputRef = ref<HTMLInputElement | null>(null);
const qrInputRef = ref<HTMLInputElement | null>(null);

const cropperRef = ref<any>(null);
const cropDialogVisible = ref(false);
const cropField = ref<LogoField | null>(null);
const cropImageUrl = ref('');
const cropUploading = ref(false);

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  form.logoExpandedUrl = s.logoExpandedUrl || '';
  form.logoCollapsedUrl = s.logoCollapsedUrl || '';
  form.loginBgUrl = s.loginBgUrl || '';
  form.qrCodeUrl = s.qrCodeUrl || '';
};

const formatUpload = (res: any) => {
  if (res?.code === 0) {
    return { ...res, url: res?.data?.url };
  }
  return { ...res, error: res?.message || '上传失败，请重试' };
};

const onUploaded = (field: keyof typeof form, ctx: SuccessContext) => {
  const url = (ctx.response as any)?.url || ctx.file?.url;
  if (url) (form as any)[field] = url;
};

const activeCropSpec = computed(() => (cropField.value ? logoCropSpecs[cropField.value] : null));
const cropDialogTitle = computed(() => (activeCropSpec.value ? `${activeCropSpec.value.label}裁切` : '图片裁切'));
const cropDialogHint = computed(() => {
  if (!activeCropSpec.value) return '';
  return `尺寸 ${activeCropSpec.value.width}×${activeCropSpec.value.height}，拖拽图片调整裁切区域，滚轮缩放`;
});
const cropFrameStyle = computed(() => {
  return { width: '100%', height: '400px' };
});

const triggerLogoSelect = (field: LogoField) => {
  if (field === 'logoExpandedUrl') logoExpandedInputRef.value?.click();
  else if (field === 'logoCollapsedUrl') logoCollapsedInputRef.value?.click();
};

const handleLogoSelect = (field: LogoField, event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  cropField.value = field;
  cropImageUrl.value = URL.createObjectURL(file);
  cropDialogVisible.value = true;

  target.value = '';
};

const resetCropper = () => {
  if (cropImageUrl.value) {
    URL.revokeObjectURL(cropImageUrl.value);
  }
  cropImageUrl.value = '';
  cropField.value = null;
};

const uploadFile = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  const headers: Record<string, string> = {};
  if (userStore.token) headers.Authorization = userStore.token;
  const response = await fetch(uiUploadUrl, {
    method: 'POST',
    body: formData,
    headers,
    credentials: 'include',
  });
  let payload: any = null;
  try {
    payload = await response.json();
  } catch {
    payload = null;
  }
  if (!response.ok || payload?.code !== 0) {
    const message = payload?.message || '上传失败，请重试';
    throw new Error(message);
  }
  return payload?.data?.url as string;
};

const handleCropConfirm = async () => {
  if (!activeCropSpec.value || !cropField.value || !cropperRef.value) return;
  cropUploading.value = true;
  try {
    const blob = await new Promise<Blob | null>((resolve) => {
      cropperRef.value.getCropBlob((data: Blob) => {
        resolve(data);
      });
    });

    if (!blob) throw new Error('裁切失败，请重试');
    const fileName = `${cropField.value}-${Date.now()}.png`;
    const file = new File([blob], fileName, { type: 'image/png' });
    const url = await uploadFile(file);
    (form as any)[cropField.value] = url;
    MessagePlugin.success('图片已上传，请点击保存按钮应用更改');
    cropDialogVisible.value = false;
  } catch (err: any) {
    MessagePlugin.error(err?.message || '上传失败，请重试');
  } finally {
    cropUploading.value = false;
  }
};

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const triggerQrSelect = () => {
  if (!isAdmin.value) return;
  qrInputRef.value?.click();
};

const handleQrSelect = async (event: Event) => {
  if (!isAdmin.value) return;
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;
  try {
    const url = await uploadFile(file);
    form.qrCodeUrl = url;
    MessagePlugin.success('二维码已上传，请点击保存按钮应用更改');
  } catch (err: any) {
    MessagePlugin.error(err?.message || '上传失败，请重试');
  } finally {
    target.value = '';
  }
};

watch(cropDialogVisible, (visible) => {
  if (!visible) resetCropper();
});

onBeforeUnmount(() => {
  if (cropImageUrl.value) URL.revokeObjectURL(cropImageUrl.value);
});

const onSubmit = async (ctx: any) => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  if (ctx && ctx.validateResult !== true) {
    return;
  }

  try {
    await request.post({
      url: '/system/ui',
      data: {
        logoExpandedUrl: form.logoExpandedUrl,
        logoCollapsedUrl: form.logoCollapsedUrl,
        loginBgUrl: form.loginBgUrl,
        qrCodeUrl: form.qrCodeUrl,
      },
    });
    await settingStore.loadUiSetting();
    MessagePlugin.success('保存成功');
  } catch (err: any) {
    MessagePlugin.error(err?.message || '保存失败');
  }
};

onMounted(() => {
  load();
});
</script>
<style lang="less" scoped>
.image-panel__url-wrap {
  width: 100%;
  max-width: 500px;
}

.image-panel__url-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
}

.image-panel__url-input {
  flex: 1;
  min-width: 0;
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

.logo-file-input {
  display: none;
}

.logo-preview-container {
  margin-top: 8px;
  padding: 8px;
  border: 1px dashed var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  background-color: var(--td-bg-color-container);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo-preview-img {
  display: block;
}

.logo-cropper {
  &-frame {
    background: #ebebeb;
    border-radius: 4px;
    overflow: hidden;
  }

  &-controls {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }

  &-hint {
    color: var(--td-text-color-placeholder);
    font-size: 12px;
  }

  &-empty {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--td-text-color-placeholder);
  }
}
</style>
