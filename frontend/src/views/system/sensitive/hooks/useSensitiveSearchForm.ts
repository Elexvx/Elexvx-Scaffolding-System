import { reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createSensitiveWordCreateForm } from '../schema/sensitiveFormSchema';
import { createSensitiveWordSearchForm } from '../schema/searchSchema';
import type { SensitiveTabValue } from '../types';

export const useSensitiveSearchForm = () => {
  const route = useRoute();
  const router = useRouter();
  const activeTab = ref<SensitiveTabValue>((route.query.tab as SensitiveTabValue) || 'words');
  const wordSearchForm = reactive(createSensitiveWordSearchForm());
  const wordCreateForm = reactive(createSensitiveWordCreateForm());
  const pageKeyword = ref('');

  watch(activeTab, (value) => {
    router.replace({ query: { ...route.query, tab: value } });
  });

  return {
    activeTab,
    wordSearchForm,
    wordCreateForm,
    pageKeyword,
  };
};
