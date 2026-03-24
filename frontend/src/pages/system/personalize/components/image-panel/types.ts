export type ImageField = 'logoExpandedUrl' | 'logoCollapsedUrl' | 'loginBgUrl' | 'qrCodeUrl';
export type LogoField = 'logoExpandedUrl' | 'logoCollapsedUrl';

export interface ImagePanelForm {
  logoExpandedUrl: string;
  logoCollapsedUrl: string;
  loginBgUrl: string;
  qrCodeUrl: string;
}

export interface CropSpec {
  label: string;
  width: number;
  height: number;
  help: string;
}
