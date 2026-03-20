import type { CropSpec, ImagePanelForm, LogoField } from '../types';

export const IMAGE_PANEL_INITIAL_FORM: ImagePanelForm = {
  logoExpandedUrl: '',
  logoCollapsedUrl: '',
  loginBgUrl: '',
  qrCodeUrl: '',
};

export const LOGO_CROP_SPECS: Record<LogoField, CropSpec> = {
  logoExpandedUrl: { label: '侧边展开图标', width: 188, height: 26, help: '尺寸 188×26，上传后可拖拽裁切' },
  logoCollapsedUrl: { label: '侧边折叠图标', width: 32, height: 32, help: '尺寸 32×32，上传后可拖拽裁切' },
};
