import { computed, ref } from 'vue';

export const useMenuDialog = () => {
  const drawerVisible = ref(false);
  const editingId = ref<number | null>(null);
  const drawerTitle = computed(() => (editingId.value ? '编辑菜单' : '新增菜单'));
  return {
    drawerVisible,
    editingId,
    drawerTitle,
  };
};
