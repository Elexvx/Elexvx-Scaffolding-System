import { computed } from 'vue';

import { useUserStore } from '@/store';
import { hasPerm } from '@/utils/permission';

export const useUserPermissions = () => {
  const userStore = useUserStore();
  const currentUserId = computed(() => userStore.userInfo?.id);
  const canQuery = computed(() => hasPerm('system:SystemUser:query'));
  const canCreate = computed(() => hasPerm('system:SystemUser:create'));
  const canUpdate = computed(() => hasPerm('system:SystemUser:update'));
  const canDelete = computed(() => hasPerm('system:SystemUser:delete'));
  const canReset = computed(() => hasPerm('system:SystemUser:update'));

  return {
    currentUserId,
    canQuery,
    canCreate,
    canUpdate,
    canDelete,
    canReset,
  };
};

