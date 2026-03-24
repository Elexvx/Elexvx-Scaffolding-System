import type { Ref } from 'vue';
import { nextTick } from 'vue';

import { request } from '@/utils/request';

import { applyOrgFilters } from '../utils/orgTree';
import type { OrgSearchForm, OrgUnitNode } from '../types';

interface UseOrgTreeOptions {
  loading: Ref<boolean>;
  expandAll: Ref<boolean>;
  tableRef: Ref<{ expandAll?: () => void; foldAll?: () => void } | null>;
  orgTree: Ref<OrgUnitNode[]>;
  filteredTree: Ref<OrgUnitNode[]>;
  filters: OrgSearchForm;
  dirty: Ref<boolean>;
}

export const useOrgTree = (options: UseOrgTreeOptions) => {
  const { loading, expandAll, tableRef, orgTree, filteredTree, filters, dirty } = options;

  const reload = async () => {
    loading.value = true;
    try {
      orgTree.value = await request.get<OrgUnitNode[]>({ url: '/system/org/tree' });
      filteredTree.value = applyOrgFilters(orgTree.value, filters);
      dirty.value = false;
      await nextTick();
      if (expandAll.value) {
        tableRef.value?.expandAll?.();
      } else {
        tableRef.value?.foldAll?.();
      }
    } finally {
      loading.value = false;
    }
  };

  const applyCurrentFilter = () => {
    filteredTree.value = applyOrgFilters(orgTree.value, filters);
  };

  return { reload, applyCurrentFilter };
};
