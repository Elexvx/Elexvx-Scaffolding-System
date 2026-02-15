import { store } from '@/store';
import { useUserStore } from '@/store/modules/user';

const ADMIN_ROLE = 'admin';

/**
 * 功能级权限判断工具。
 */
export function hasPerm(code: string): boolean {
  const permissionCode = String(code || '').trim();
  if (!permissionCode) return false;

  const userStore = useUserStore(store);
  if (!userStore.token) return false;

  const roles = userStore.userInfo?.roles || [];
  if (roles.some((role) => String(role || '').trim().toLowerCase() === ADMIN_ROLE)) {
    return true;
  }

  const permissions = userStore.userInfo?.permissions || [];
  return permissions.includes(permissionCode);
}
