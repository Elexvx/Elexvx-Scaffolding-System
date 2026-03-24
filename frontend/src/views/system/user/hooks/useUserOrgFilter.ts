import { filterTree, findNodeById, normalizeOrgUnitType, resolveOrgUnitType } from '../helpers';
import type { OrgUnitNode } from '../types';

interface UseUserOrgFilterInput {
  orgTree: { value: OrgUnitNode[] };
  filteredOrgTree: { value: OrgUnitNode[] };
  orgKeyword: { value: string };
  selectedOrgUnitId: { value: number | null };
  selectedDepartmentId: { value: number | null };
  onSelectionChanged: () => void;
}

export const useUserOrgFilter = ({
  orgTree,
  filteredOrgTree,
  orgKeyword,
  selectedOrgUnitId,
  selectedDepartmentId,
  onSelectionChanged,
}: UseUserOrgFilterInput) => {
  const filterOrgTree = () => {
    if (!orgKeyword.value) {
      filteredOrgTree.value = [...orgTree.value];
      return;
    }
    filteredOrgTree.value = filterTree(orgTree.value, orgKeyword.value.trim());
  };

  const handleOrgSelect = (context: { node?: { data?: OrgUnitNode; getModel?: () => OrgUnitNode; value?: unknown; id?: unknown } }) => {
    const node = context?.node;
    if (!node) return;
    const rawNode = (node.data || node.getModel?.() || {}) as Partial<OrgUnitNode>;
    const rawId = rawNode?.id ?? node.value ?? node.id;
    const nodeId = Number(rawId);
    if (Number.isNaN(nodeId)) return;
    const resolvedNode = findNodeById(orgTree.value, nodeId);
    let nodeType = resolveOrgUnitType(resolvedNode || (rawNode as OrgUnitNode));
    if (!nodeType) {
      const parentId = resolvedNode?.parentId ?? rawNode?.parentId;
      if (parentId != null && parentId !== 0) {
        const parentNode = findNodeById(orgTree.value, parentId);
        const parentType = normalizeOrgUnitType(parentNode?.type ?? parentNode?.typeLabel);
        if (parentType === 'UNIT') nodeType = 'DEPARTMENT';
      }
    }
    if (['DEPARTMENT', 'SECTION', 'TEAM'].includes(nodeType)) {
      selectedDepartmentId.value = nodeId;
      selectedOrgUnitId.value = null;
    } else {
      selectedOrgUnitId.value = nodeId;
      selectedDepartmentId.value = null;
    }
    onSelectionChanged();
  };

  return {
    filterOrgTree,
    handleOrgSelect,
  };
};
