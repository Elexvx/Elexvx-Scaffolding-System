import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import type { SecurityTab } from '../types';

export const useSecurityPageState = () => {
  const route = useRoute();
  const router = useRouter();
  const activeTab = ref<SecurityTab>((route.query.tab as SecurityTab) || 'token');
  const saving = ref(false);
  const disableConfirmVisible = ref(false);
  const syncTab = (tab: SecurityTab) => router.replace({ query: { ...route.query, tab } });
  return { activeTab, saving, disableConfirmVisible, syncTab };
};
