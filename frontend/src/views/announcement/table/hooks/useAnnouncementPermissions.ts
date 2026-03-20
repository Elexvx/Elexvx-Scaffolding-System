import { computed } from 'vue';

export const useAnnouncementPermissions = () => ({
  canCreate: computed(() => true),
  canEdit: computed(() => true),
  canDelete: computed(() => true),
  canPublish: computed(() => true),
});
