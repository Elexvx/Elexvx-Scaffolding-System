import { request } from 'umi';

import type { CurrentUser, LoginPayload, LoginResponse } from '@/types/auth';
import { unwrapApiEnvelope } from '@/utils/request';

export async function loginByPassword(data: LoginPayload) {
  const payload = await request('/auth/login', {
    method: 'POST',
    data,
  });
  return unwrapApiEnvelope<LoginResponse>(payload);
}

export async function getCurrentUser() {
  const payload = await request('/auth/user');
  return unwrapApiEnvelope<CurrentUser>(payload);
}

export async function logout() {
  await request('/auth/logout', {
    method: 'POST',
  });
}
