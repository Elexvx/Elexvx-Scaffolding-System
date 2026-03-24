import { onUnmounted, watch, type Ref } from 'vue';

import { prefix } from '@/config/global';

export const useDownloadDialog = (
  previewVisible: Ref<boolean>,
  previewBodyOverflowBackup: Ref<string | null>,
  previewMainOverflowBackup: Ref<string | null>,
) => {
  const restoreScrollLock = () => {
    if (typeof document === 'undefined') return;
    const body = document.body;
    const mainWrapper = document.querySelector<HTMLElement>(`.${prefix}-main-wrapper`);
    try {
      body.style.overflow = previewBodyOverflowBackup.value ?? '';
      previewBodyOverflowBackup.value = null;
      const layout = document.querySelector<HTMLElement>('.t-layout');
      if (layout) layout.style.overflow = '';
    } catch {
      //
    }
    if (mainWrapper) {
      try {
        mainWrapper.style.overflow = previewMainOverflowBackup.value ?? '';
        previewMainOverflowBackup.value = null;
      } catch {
        //
      }
    }
  };

  watch(previewVisible, (visible) => {
    if (typeof document === 'undefined') return;
    const body = document.body;
    const mainWrapper = document.querySelector<HTMLElement>(`.${prefix}-main-wrapper`);
    if (visible) {
      if (previewBodyOverflowBackup.value === null) previewBodyOverflowBackup.value = body.style.overflow;
      try {
        body.style.overflow = 'hidden';
        const layout = document.querySelector<HTMLElement>('.t-layout');
        if (layout) layout.style.overflow = 'hidden';
      } catch {
        //
      }
      if (mainWrapper) {
        if (previewMainOverflowBackup.value === null) previewMainOverflowBackup.value = mainWrapper.style.overflow;
        try {
          mainWrapper.style.overflow = 'hidden';
        } catch {
          //
        }
      }
      return;
    }
    restoreScrollLock();
  });

  onUnmounted(() => {
    restoreScrollLock();
  });

  return { restoreScrollLock };
};
