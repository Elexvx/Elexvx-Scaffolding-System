import { computed } from 'vue';

import { hasPerm } from '@/utils/permission';

export const useRolePermissions = () => {
  const canQuery = computed(() => hasPerm('system:SystemRole:query'));
  const canCreate = computed(() => hasPerm('system:SystemRole:create'));
  const canUpdate = computed(() => hasPerm('system:SystemRole:update'));
  const canDelete = computed(() => hasPerm('system:SystemRole:delete'));
  return { canQuery, canCreate, canUpdate, canDelete };
};
