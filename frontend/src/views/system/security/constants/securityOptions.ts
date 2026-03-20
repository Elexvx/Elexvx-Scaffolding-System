import type { SecuritySettingsForm } from '../types';

export const createDefaultSecurityForm = (): SecuritySettingsForm => ({
  sessionTimeoutMinutes: 1440,
  tokenTimeoutMinutes: 1440,
  tokenRefreshGraceMinutes: 60,
  captchaEnabled: true,
  captchaType: 'image',
  dragCaptchaWidth: 280,
  dragCaptchaHeight: 160,
  dragCaptchaThreshold: 98,
  imageCaptchaLength: 5,
  imageCaptchaNoiseLines: 8,
  passwordMinLength: 6,
  passwordRequireUppercase: false,
  passwordRequireLowercase: false,
  passwordRequireSpecial: false,
  passwordAllowSequential: true,
  defenseWindowMinutes: 5,
  defenseMaxRequests: 100,
  defenseMaxErrors: 10,
});
