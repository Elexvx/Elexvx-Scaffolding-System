import { ref } from 'vue';
import type { MenuNode } from '../helpers';

export const useMenuTable = () => {
  const keyword = ref('');
  const loading = ref(false);
  const savingOrder = ref(false);
  const data = ref<MenuNode[]>([]);
  const expandedTreeNodes = ref<Array<string | number>>([]);
  const expandAll = ref(false);

  return {
    keyword,
    loading,
    savingOrder,
    data,
    expandedTreeNodes,
    expandAll,
  };
};
