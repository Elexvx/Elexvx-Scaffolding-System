import { computed } from 'vue';

export const useOrgPermissions = () => {
  const canQuery = computed(() => true);
  const canCreate = computed(() => true);
  const canUpdate = computed(() => true);
  const canDelete = computed(() => true);

  return { canQuery, canCreate, canUpdate, canDelete };
};
