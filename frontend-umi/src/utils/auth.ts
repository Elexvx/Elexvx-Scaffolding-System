import { history, request } from '@umijs/max';

import { clearAuthStorage } from '@/utils/token';

export async function logoutAndRedirect() {
  clearAuthStorage();
  await request('/api/auth/logout', { method: 'POST' }).catch(() => undefined);
  history.push('/login');
}
