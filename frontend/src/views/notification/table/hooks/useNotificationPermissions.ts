import { computed } from 'vue';

export const useNotificationPermissions = () => ({
  canCreate: computed(() => true),
  canEdit: computed(() => true),
  canDelete: computed(() => true),
  canPublish: computed(() => true),
});
