import type { UserRow } from '../types';

export const isRootAdmin = (row: UserRow) => (row.account || '').trim().toLowerCase() === 'admin';

export const canResetPasswordForRow = (row: UserRow, currentUserId?: number) => {
  if (isRootAdmin(row)) return false;
  if (currentUserId && row.id === currentUserId) return false;
  return true;
};

export const canDeleteRow = (row: UserRow, currentUserId?: number) => {
  if (isRootAdmin(row)) return false;
  if (currentUserId && row.id === currentUserId) return false;
  return true;
};

export const canEditRow = (_row: UserRow) => true;

