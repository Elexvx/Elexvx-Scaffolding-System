import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useSettingStore } from '@/store';

export function useLoginRedirect() {
  const route = useRoute();
  const router = useRouter();
  const settingStore = useSettingStore();

  const redirectTarget = computed(() => {
    const fallbackHome = settingStore.defaultHome || '/user/index';
    const redirect = route.query.redirect as string | undefined;
    if (!redirect) return fallbackHome;
    const decoded = decodeURIComponent(redirect);
    if ((decoded === '/' || decoded === '/user/index') && fallbackHome !== '/user/index') {
      return fallbackHome;
    }
    return decoded;
  });

  const pushAfterLogin = async () => {
    await router.push(redirectTarget.value);
  };

  return { route, router, redirectTarget, pushAfterLogin };
}
