import type { SuccessContext } from 'tdesign-vue-next';

import type { ImageField } from '../types';

export const formatUploadResponse = (res: any) => {
  if (res?.code === 0) return { ...res, url: res?.data?.url };
  return { ...res, error: res?.message || '上传失败，请重试' };
};

export const resolveUploadedUrl = (ctx: SuccessContext) => ((ctx.response as any)?.url || ctx.file?.url || '') as string;

export const createImagePanelPayload = (form: Record<ImageField, string>) => ({
  logoExpandedUrl: form.logoExpandedUrl,
  logoCollapsedUrl: form.logoCollapsedUrl,
  loginBgUrl: form.loginBgUrl,
  qrCodeUrl: form.qrCodeUrl,
});
