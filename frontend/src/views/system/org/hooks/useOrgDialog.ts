import { computed, ref } from 'vue';

export const useOrgDialog = () => {
  const dialogVisible = ref(false);
  const leaderDialogVisible = ref(false);
  const memberDialogVisible = ref(false);
  const editingId = ref<number | null>(null);

  const dialogTitle = computed(() => (editingId.value ? '编辑机构' : '添加机构'));

  return {
    dialogVisible,
    leaderDialogVisible,
    memberDialogVisible,
    editingId,
    dialogTitle,
  };
};
