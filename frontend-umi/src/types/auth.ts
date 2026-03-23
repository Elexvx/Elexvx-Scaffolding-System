export interface LoginPayload {
  account?: string;
  password?: string;
  phone?: string;
  email?: string;
  verifyCode?: string;
  captcha?: string;
}

export interface LoginResponse {
  token: string;
  refreshToken?: string;
  expiresIn?: number;
  status?: string;
  accessToken?: string;
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
