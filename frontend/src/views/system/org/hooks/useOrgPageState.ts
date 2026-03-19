import type { EnhancedTableInstanceFunctions } from 'tdesign-vue-next';
import { ref } from 'vue';

import type { OrgUnitNode } from '../types';

export const useOrgPageState = () => {
  const tableRef = ref<EnhancedTableInstanceFunctions<OrgUnitNode> | null>(null);
  const loading = ref(false);
  const saving = ref(false);
  const savingOrder = ref(false);
  const addingMembers = ref(false);
  const dirty = ref(false);
  const expandAll = ref(true);
  const expandedTreeNodes = ref<Array<string | number>>([]);
  const orgTree = ref<OrgUnitNode[]>([]);
  const filteredTree = ref<OrgUnitNode[]>([]);

  return {
    tableRef,
    loading,
    saving,
    savingOrder,
    addingMembers,
    dirty,
    expandAll,
    expandedTreeNodes,
    orgTree,
    filteredTree,
  };
};
