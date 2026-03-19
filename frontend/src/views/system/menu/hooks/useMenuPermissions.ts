import { computed } from 'vue';

import { hasPerm } from '@/utils/permission';

export const useMenuPermissions = () => {
  const canQuery = computed(() => hasPerm('system:SystemMenu:query'));
  const canCreate = computed(() => hasPerm('system:SystemMenu:create'));
  const canUpdate = computed(() => hasPerm('system:SystemMenu:update'));
  const canDelete = computed(() => hasPerm('system:SystemMenu:delete'));

  return {
    canQuery,
    canCreate,
    canUpdate,
    canDelete,
  };
};

