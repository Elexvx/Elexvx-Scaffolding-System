import { request } from '@umijs/max';

import type { CurrentUser, LoginPayload, LoginResponse } from '@/types/auth';
import { unwrapApiEnvelope } from '@/utils/request';

export async function loginByPassword(data: LoginPayload) {
  const payload = await request('/api/auth/login', {
    method: 'POST',
    data,
  });
  return unwrapApiEnvelope<LoginResponse>(payload);
}

export async function getCurrentUser() {
  const payload = await request('/api/auth/user');
  return unwrapApiEnvelope<CurrentUser>(payload);
}

export async function logout() {
  await request('/api/auth/logout', {
    method: 'POST',
  });
}
