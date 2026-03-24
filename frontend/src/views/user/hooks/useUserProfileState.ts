import { computed, ref } from 'vue';

import { desktopBreakpoint, mobileBreakpoint } from '../constants/profileOptions';

export const useUserProfileState = () => {
  const viewportWidth = ref(typeof window === 'undefined' ? desktopBreakpoint : window.innerWidth);
  const profileLoading = ref(false);
  const updatingProfile = ref(false);
  const loginLogLoading = ref(false);
  const basicEditVisible = ref(false);
  const documentEditVisible = ref(false);
  const passwordDialogVisible = ref(false);
  const activePanels = ref<string[]>(['basic']);
  const basicMasked = ref(true);
  const documentMasked = ref(true);
  const securityMasked = ref(true);

  const isMobile = computed(() => viewportWidth.value < mobileBreakpoint);
  const isWideDesktop = computed(() => viewportWidth.value > desktopBreakpoint);
  const drawerSize = computed(() => (isMobile.value ? '100%' : '760px'));

  const updateViewport = () => {
    if (typeof window === 'undefined') return;
    viewportWidth.value = window.innerWidth;
  };

  return {
    viewportWidth,
    profileLoading,
    updatingProfile,
    loginLogLoading,
    basicEditVisible,
    documentEditVisible,
    passwordDialogVisible,
    activePanels,
    basicMasked,
    documentMasked,
    securityMasked,
    isMobile,
    isWideDesktop,
    drawerSize,
    updateViewport,
  };
};
