import { computed } from 'vue';

import { hasPerm } from '@/utils/permission';

import { sensitivePermissionCodes } from '../constants/sensitiveOptions';

export const useSensitivePermissions = () => {
  const canQuery = computed(() => hasPerm(sensitivePermissionCodes.query));
  const canCreate = computed(() => hasPerm(sensitivePermissionCodes.create));
  const canDelete = computed(() => hasPerm(sensitivePermissionCodes.delete));
  const canUpdate = computed(() => hasPerm(sensitivePermissionCodes.update));
  return { canQuery, canCreate, canDelete, canUpdate };
};
