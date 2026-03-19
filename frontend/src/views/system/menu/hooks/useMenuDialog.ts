import { computed, ref } from 'vue';

export const useMenuDialog = () => {
  const drawerVisible = ref(false);
  const mode = ref<'create' | 'edit'>('create');
  const drawerTitle = computed(() => (mode.value === 'create' ? '添加菜单' : '编辑菜单'));
  return {
    drawerVisible,
    mode,
    drawerTitle,
  };
};
