import { history } from '@umijs/max';

import { logout } from '@/services/auth/auth';
import { clearAuthStorage } from '@/utils/token';

export async function logoutAndRedirect() {
  clearAuthStorage();
  await logout().catch(() => undefined);
  history.push('/login');
}
