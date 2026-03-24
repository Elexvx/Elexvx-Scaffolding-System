import { request } from '@/utils/request';

export interface UserProfile {
  id: number;
  account: string;
  name: string;
  mobile: string;
  email: string;
  idType?: string;
  idCard: string;
  idValidFrom?: string;
  idValidTo?: string;
  seat: string;
  entity: string;
  leader: string;
  position: string;
  joinDay: string;
  team: string;
  gender: string;
  nickname: string;
  provinceId?: number | null;
  province: string;
  cityId?: number | null;
  city: string;
  districtId?: number | null;
  district: string;
  zipCode?: string;
  address: string;
  introduction: string;
  avatar: string;
  tags: string;
  completenessScore?: number;
  basicInfoScore?: number;
  documentInfoScore?: number;
  incompleteItems?: string[];
  roles?: string[];
  orgUnitNames?: string[];
}

export interface UserProfileUpdate {
  name?: string;
  mobile?: string;
  email?: string;
  idType?: string;
  idCard?: string;
  idValidFrom?: string;
  idValidTo?: string;
  seat?: string;
  entity?: string;
  leader?: string;
  position?: string;
  joinDay?: string;
  team?: string;
  gender?: string;
  nickname?: string;
  provinceId?: number | null;
  province?: string;
  cityId?: number | null;
  city?: string;
  districtId?: number | null;
  district?: string;
  zipCode?: string;
  address?: string;
  introduction?: string;
  avatar?: string;
  tags?: string;
}

const Api = {
  Profile: '/auth/profile',
  Password: '/auth/password',
  Parameters: '/user/parameters',
};

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UserParameter {
  id?: number;
  userId?: number;
  paramKey: string;
  paramValue: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 获取当前登录用户资料
 */
export function getMyProfile() {
  return request.get<UserProfile>({
    url: Api.Profile,
  });
}

/**
 * 获取用户参数列表
 */
export function getUserParameters() {
  return request.get<UserParameter[]>({
    url: Api.Parameters,
  });
}

/**
 * 创建用户参数
 */
export function createUserParameter(data: UserParameter) {
  return request.post<UserParameter>({
    url: Api.Parameters,
    data,
  });
}

/**
 * 更新用户参数
 */
export function updateUserParameter(id: number, data: UserParameter) {
  return request.put<UserParameter>({
    url: `${Api.Parameters}/${id}`,
    data,
  });
}

/**
 * 删除用户参数
 */
export function deleteUserParameter(id: number) {
  return request.delete<void>({
    url: `${Api.Parameters}/${id}`,
  });
}

/**
 * 更新当前登录用户资料
 */
export function updateMyProfile(data: UserProfileUpdate) {
  return request.put<UserProfile>({
    url: Api.Profile,
    data,
  });
}

/**
 * 修改密码
 */
export function changePassword(data: ChangePasswordRequest) {
  return request.post<boolean>({
    url: Api.Password,
    data,
  });
}

// deprecated exports (kept for compatibility)
export type UserInfo = UserProfile;
export type UpdateUserInfo = UserProfileUpdate;
export function getCurrentUser() {
  return getMyProfile();
}
export function getUserById(id: number) {
  return request.get<UserProfile>({
    url: `/system/user/${id}`,
  });
}
export function updateUser(id: number, data: UserProfileUpdate) {
  void id;
  return updateMyProfile(data);
}
