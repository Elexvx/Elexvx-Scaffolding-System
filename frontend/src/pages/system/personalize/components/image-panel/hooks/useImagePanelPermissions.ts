import { computed } from 'vue';

import { useUserStore } from '@/store';

export function useImagePanelPermissions() {
  const userStore = useUserStore();
  const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));
  return { isAdmin, token: computed(() => userStore.token) };
}
