import { createDefaultSecurityForm } from '../constants/securityOptions';
import type { CaptchaType, SecuritySettingsForm } from '../types';
import { normalizeBoolean, normalizeNumber } from './securityGuards';

export const mergeSecuritySettings = (data: Record<string, any> | undefined, current?: SecuritySettingsForm): SecuritySettingsForm => {
  const fallback = current || createDefaultSecurityForm();
  return {
    sessionTimeoutMinutes: normalizeNumber(data?.sessionTimeoutMinutes, fallback.sessionTimeoutMinutes),
    tokenTimeoutMinutes: normalizeNumber(data?.tokenTimeoutMinutes, fallback.tokenTimeoutMinutes),
    tokenRefreshGraceMinutes: normalizeNumber(data?.tokenRefreshGraceMinutes, fallback.tokenRefreshGraceMinutes),
    captchaEnabled: normalizeBoolean(data?.captchaEnabled, fallback.captchaEnabled),
    captchaType: ((data?.captchaType as CaptchaType) || fallback.captchaType),
    dragCaptchaWidth: normalizeNumber(data?.dragCaptchaWidth, fallback.dragCaptchaWidth),
    dragCaptchaHeight: normalizeNumber(data?.dragCaptchaHeight, fallback.dragCaptchaHeight),
    dragCaptchaThreshold: normalizeNumber(data?.dragCaptchaThreshold, fallback.dragCaptchaThreshold),
    imageCaptchaLength: normalizeNumber(data?.imageCaptchaLength, fallback.imageCaptchaLength),
    imageCaptchaNoiseLines: normalizeNumber(data?.imageCaptchaNoiseLines, fallback.imageCaptchaNoiseLines),
    passwordMinLength: normalizeNumber(data?.passwordMinLength, fallback.passwordMinLength),
    passwordRequireUppercase: normalizeBoolean(data?.passwordRequireUppercase, fallback.passwordRequireUppercase),
    passwordRequireLowercase: normalizeBoolean(data?.passwordRequireLowercase, fallback.passwordRequireLowercase),
    passwordRequireSpecial: normalizeBoolean(data?.passwordRequireSpecial, fallback.passwordRequireSpecial),
    passwordAllowSequential: normalizeBoolean(data?.passwordAllowSequential, fallback.passwordAllowSequential),
    defenseWindowMinutes: normalizeNumber(data?.defenseWindowMinutes, fallback.defenseWindowMinutes),
    defenseMaxRequests: normalizeNumber(data?.defenseMaxRequests, fallback.defenseMaxRequests),
    defenseMaxErrors: normalizeNumber(data?.defenseMaxErrors, fallback.defenseMaxErrors),
  };
};
