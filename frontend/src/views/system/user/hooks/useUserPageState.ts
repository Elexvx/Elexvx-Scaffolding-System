import { computed, ref } from 'vue';

import { buildDepartmentTree, buildOrgUnitTree, collectSelectableIds, flattenOrgIds, isDepartmentNode, isOrgNode } from '../helpers';
import { createUserPagination } from './useUserTable';
import type { OrgUnitNode, RoleRow, UserRow } from '../types';

export const useUserPageState = () => {
  const rows = ref<UserRow[]>([]);
  const roles = ref<RoleRow[]>([]);
  const orgTree = ref<OrgUnitNode[]>([]);
  const filteredOrgTree = ref<OrgUnitNode[]>([]);
  const orgKeyword = ref('');
  const selectedOrgUnitId = ref<number | null>(null);
  const selectedDepartmentId = ref<number | null>(null);
  const expandedOrgIds = ref<number[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const pagination = createUserPagination();

  const orgUnitTree = computed(() => buildOrgUnitTree(orgTree.value));
  const departmentTree = computed(() => buildDepartmentTree(orgTree.value));
  const orgSelectableIds = computed(() => collectSelectableIds(orgUnitTree.value, (node) => isOrgNode(node)));
  const departmentSelectableIds = computed(() =>
    collectSelectableIds(departmentTree.value, (node) => isDepartmentNode(node)),
  );

  const applyOrgTree = (nodes: OrgUnitNode[]) => {
    orgTree.value = nodes;
    filteredOrgTree.value = [...nodes];
    expandedOrgIds.value = flattenOrgIds(nodes);
  };

  return {
    rows,
    roles,
    orgTree,
    filteredOrgTree,
    orgKeyword,
    selectedOrgUnitId,
    selectedDepartmentId,
    expandedOrgIds,
    loading,
    saving,
    pagination,
    orgUnitTree,
    departmentTree,
    orgSelectableIds,
    departmentSelectableIds,
    applyOrgTree,
  };
};
