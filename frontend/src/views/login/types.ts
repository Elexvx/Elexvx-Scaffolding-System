import type { FormRule } from 'tdesign-vue-next';

export type LoginPanelType = 'login' | 'register' | 'forgot';
export type LoginMethod = 'password' | 'phone' | 'email';
export type CaptchaType = 'image' | 'drag';

export interface LoginFormData {
  phone: string;
  email: string;
  account: string;
  password: string;
  verifyCode: string;
  checked: boolean;
  captcha: string;
  agreed: boolean;
}

export interface RegisterFormData {
  account: string;
  password: string;
  confirmPassword: string;
  captcha: string;
  checked: boolean;
}

export interface ForgotPasswordFormData {
  account: string;
  phone: string;
  verifyCode: string;
  newPassword: string;
  confirmPassword: string;
  checked: boolean;
}

export interface CaptchaState {
  captchaId: string;
  captchaImage: string;
  captchaEnabled: boolean;
  captchaType: CaptchaType;
  dragWidth: number;
  dragHeight: number;
  dragRefreshKey: number;
  showDragCaptchaDialog: boolean;
  pendingDragSubmit: boolean;
}

export interface CaptchaResponse {
  id: string;
  image: string;
  expiresIn?: number;
  type?: CaptchaType;
  width?: number;
  height?: number;
  enabled?: boolean;
}

export type FormRuleMap = Record<string, FormRule[]>;
