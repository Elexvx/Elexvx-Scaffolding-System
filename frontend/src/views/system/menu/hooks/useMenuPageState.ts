import type { EnhancedTableInstanceFunctions } from 'tdesign-vue-next';
import { computed, reactive, ref } from 'vue';

import { createMenuSearchModel } from '../schema/searchSchema';
import type { MenuNode } from '../types';
import { buildParentTree, treeFilter } from '../utils/menuTree';

export const useMenuPageState = () => {
  const tableRef = ref<EnhancedTableInstanceFunctions<MenuNode> | null>(null);
  const loading = ref(false);
  const savingOrder = ref(false);
  const savingNode = ref(false);
  const data = ref<MenuNode[]>([]);
  const expandedTreeNodes = ref<Array<string | number>>([]);
  const expandAll = ref(false);
  const dirty = ref(false);
  const rowSaving = ref<Record<number, boolean>>({});
  const search = createMenuSearchModel();

  const treeConfig = reactive({
    childrenKey: 'children',
    treeNodeColumnIndex: 1,
    indent: 24,
    expandTreeNodeOnClick: true,
  });

  const filteredData = computed(() => treeFilter(data.value, search.keyword));
  const dirTreeData = computed(() => buildParentTree(data.value, ['DIR']));
  const allParentTree = computed(() => buildParentTree(data.value, ['DIR', 'PAGE']));

  return {
    tableRef,
    loading,
    savingOrder,
    savingNode,
    data,
    expandedTreeNodes,
    expandAll,
    dirty,
    rowSaving,
    search,
    treeConfig,
    filteredData,
    dirTreeData,
    allParentTree,
  };
};

