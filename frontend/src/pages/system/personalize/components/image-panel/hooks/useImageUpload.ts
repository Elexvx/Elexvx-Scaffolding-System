import { MessagePlugin } from 'tdesign-vue-next';
import { onBeforeUnmount, ref, watch } from 'vue';

import type { LogoField } from '../types';

export function useImageUpload(uploadFile: (file: File) => Promise<string>, assignUrl: (field: LogoField | 'qrCodeUrl', url: string) => void) {
  const logoExpandedInputRef = ref<HTMLInputElement | null>(null);
  const logoCollapsedInputRef = ref<HTMLInputElement | null>(null);
  const qrInputRef = ref<HTMLInputElement | null>(null);
  const cropperRef = ref<any>(null);
  const cropDialogVisible = ref(false);
  const cropField = ref<LogoField | null>(null);
  const cropImageUrl = ref('');
  const cropUploading = ref(false);

  const triggerLogoSelect = (field: LogoField) => {
    if (field === 'logoExpandedUrl') logoExpandedInputRef.value?.click();
    if (field === 'logoCollapsedUrl') logoCollapsedInputRef.value?.click();
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
    if (cropImageUrl.value) URL.revokeObjectURL(cropImageUrl.value);
    cropField.value = null;
    cropImageUrl.value = '';
  };

  const handleCropConfirm = async () => {
    if (!cropField.value || !cropperRef.value) return;
    cropUploading.value = true;
    try {
      const blob = await new Promise<Blob | null>((resolve) => cropperRef.value.getCropBlob((data: Blob) => resolve(data)));
      if (!blob) throw new Error('裁切失败，请重试');
      const file = new File([blob], `${cropField.value}-${Date.now()}.png`, { type: 'image/png' });
      const url = await uploadFile(file);
      assignUrl(cropField.value, url);
      MessagePlugin.success('图片已上传，请点击保存按钮应用更改');
      cropDialogVisible.value = false;
    } catch (err: any) {
      MessagePlugin.error(err?.message || '上传失败，请重试');
    } finally {
      cropUploading.value = false;
    }
  };

  const handleQrSelect = async (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (!file) return;
    try {
      const url = await uploadFile(file);
      assignUrl('qrCodeUrl', url);
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
  onBeforeUnmount(resetCropper);

  return {
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
  };
}
