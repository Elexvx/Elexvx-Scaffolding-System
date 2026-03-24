import type { LoginFormData, LoginMethod, RegisterFormData, ForgotPasswordFormData } from '../types';

export const INITIAL_LOGIN_FORM: LoginFormData = {
  phone: '',
  email: '',
  account: '',
  password: '',
  verifyCode: '',
  checked: false,
  captcha: '',
  agreed: false,
};

export const INITIAL_REGISTER_FORM: RegisterFormData = {
  account: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  checked: false,
};

export const INITIAL_FORGOT_PASSWORD_FORM: ForgotPasswordFormData = {
  account: '',
  phone: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
  checked: false,
};

export const LOGIN_METHOD_ORDER: LoginMethod[] = ['phone', 'email', 'password'];
export const REMEMBER_ACCOUNT_KEY = 'remember_account';
export const CAPTCHA_REFRESH_FALLBACK_MS = 120 * 1000;
export const CAPTCHA_REFRESH_LEEWAY_MS = 5 * 1000;
