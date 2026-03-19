import { computed, ref } from 'vue';
import type { Mode, UserRow } from '../types';

export const useUserDialog = () => {
  const drawerVisible = ref(false);
  const mode = ref<Mode>('create');
  const editingId = ref<number | null>(null);
  const resetDialogVisible = ref(false);
  const resetSubmitting = ref(false);
  const resetTarget = ref<UserRow | null>(null);
  const drawerTitle = computed(() => (mode.value === 'create' ? '新增用户' : '编辑用户'));

  return {
    drawerVisible,
    mode,
    editingId,
    resetDialogVisible,
    resetSubmitting,
    resetTarget,
    drawerTitle,
  };
};
