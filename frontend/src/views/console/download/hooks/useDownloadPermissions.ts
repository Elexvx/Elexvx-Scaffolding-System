import { computed } from 'vue';

import { hasPerm } from '@/utils/permission';

import { downloadPermissionCodes } from '../constants/downloadOptions';

export const useDownloadPermissions = () => {
  const canQuery = computed(() => hasPerm(downloadPermissionCodes.query));
  const canCreate = computed(() => hasPerm(downloadPermissionCodes.create));
  const canUpdate = computed(() => hasPerm(downloadPermissionCodes.update));
  const canDelete = computed(() => hasPerm(downloadPermissionCodes.delete));
  return { canQuery, canCreate, canUpdate, canDelete };
};
