import { computed } from 'vue';

import { hasPerm } from '@/utils/permission';

export function useVerificationPanelPermissions() {
  const canUpdate = computed(
    () => hasPerm('system:SystemVerification:update') || hasPerm('system:SystemPersonalize:update'),
  );
  const ensureCanUpdate = () => {
    if (!canUpdate.value) return false;
    return true;
  };
  return { canUpdate, ensureCanUpdate };
}
