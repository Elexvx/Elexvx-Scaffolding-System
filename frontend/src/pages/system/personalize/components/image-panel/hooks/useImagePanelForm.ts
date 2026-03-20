import { reactive } from 'vue';

import { request } from '@/utils/request';

import { IMAGE_PANEL_INITIAL_FORM } from '../constants/imagePanelOptions';
import { createImagePanelPayload } from '../utils/imagePanelMappers';

export function useImagePanelForm() {
  const form = reactive({ ...IMAGE_PANEL_INITIAL_FORM });

  const load = async () => {
    const settings = await request.get<any>({ url: '/system/ui' });
    form.logoExpandedUrl = settings.logoExpandedUrl || '';
    form.logoCollapsedUrl = settings.logoCollapsedUrl || '';
    form.loginBgUrl = settings.loginBgUrl || '';
    form.qrCodeUrl = settings.qrCodeUrl || '';
  };

  const buildPayload = () => createImagePanelPayload(form);

  return { buildPayload, form, load };
}
