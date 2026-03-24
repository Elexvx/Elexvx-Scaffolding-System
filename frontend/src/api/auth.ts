import { request } from '@/utils/request';

/**
 * 认证相关 API（无需登录或登录态相关）。
 *
 * 约定：
 * - withToken:false：注册/验证码发送/短信或邮箱登录/重置密码等接口不携带 Authorization
 * - 返回值由 utils/request 统一按 { code, message, data } 解包（如后端遵循该结构）
 */
export interface RegisterPayload {
  account: string;
  password: string;
  confirmPassword: string;
  captchaId: string;
  captchaCode: string;
}

export interface SmsSendPayload {
  phone: string;
}

export interface SmsLoginPayload {
  phone: string;
  code: string;
  force?: boolean;
}

export interface EmailSendPayload {
  email: string;
}

export interface EmailLoginPayload {
  email: string;
  code: string;
  force?: boolean;
}

export interface ResetPasswordPayload {
  account: string;
  phone: string;
  code: string;
  newPassword: string;
  confirmPassword: string;
}

export function register(payload: RegisterPayload) {
  return request.post<boolean>({ url: '/auth/register', data: payload }, { withToken: false });
}

export function sendSmsCode(payload: SmsSendPayload) {
  return request.post<{ expiresIn: number }>({ url: '/auth/sms/send', data: payload }, { withToken: false });
}

export function loginBySms(payload: SmsLoginPayload) {
  return request.post<any>({ url: '/auth/login/sms', data: payload }, { withToken: false });
}

export function sendEmailCode(payload: EmailSendPayload) {
  return request.post<{ expiresIn: number }>({ url: '/auth/email/send', data: payload }, { withToken: false });
}

export function loginByEmail(payload: EmailLoginPayload) {
  return request.post<any>({ url: '/auth/login/email', data: payload }, { withToken: false });
}

export function resetPassword(payload: ResetPasswordPayload) {
  return request.post<boolean>({ url: '/auth/password/reset', data: payload }, { withToken: false });
}
