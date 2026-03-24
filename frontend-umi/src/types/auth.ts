export interface LoginPayload {
  account?: string;
  password?: string;
  phone?: string;
  email?: string;
  verifyCode?: string;
  captcha?: string;
  captchaId?: string;
  captchaCode?: string;
  force?: boolean;
}

export interface LoginResponse {
  token: string;
  refreshToken?: string;
  expiresIn?: number;
  status?: string;
  accessToken?: string;
  requestId?: string;
  requestKey?: string;
}

export interface LoginCaptchaResponse {
  id?: string;
  image?: string;
  expiresIn?: number;
  type?: 'image' | 'drag';
  width?: number;
  height?: number;
  threshold?: number;
  enabled?: boolean;
}

export interface CurrentUser {
  id?: number;
  guid?: string;
  name: string;
  avatar?: string;
  roles: string[];
  permissions?: string[];
  assignedRoles?: string[];
  roleSimulated?: boolean;
  orgUnitNames?: string[];
}
