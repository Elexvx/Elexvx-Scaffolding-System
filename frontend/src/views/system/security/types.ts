export type SecurityTab = 'token' | 'captcha' | 'password' | 'defense';
export type CaptchaType = 'drag' | 'image';

export interface SecuritySettingsForm {
  sessionTimeoutMinutes: number;
  tokenTimeoutMinutes: number;
  tokenRefreshGraceMinutes: number;
  captchaEnabled: boolean;
  captchaType: CaptchaType;
  dragCaptchaWidth: number;
  dragCaptchaHeight: number;
  dragCaptchaThreshold: number;
  imageCaptchaLength: number;
  imageCaptchaNoiseLines: number;
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireLowercase: boolean;
  passwordRequireSpecial: boolean;
  passwordAllowSequential: boolean;
  defenseWindowMinutes: number;
  defenseMaxRequests: number;
  defenseMaxErrors: number;
}
