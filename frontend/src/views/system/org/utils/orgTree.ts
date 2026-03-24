import type { OrgUnitNode, OrgSearchForm } from '../types';

export const applyOrgFilters = (nodes: OrgUnitNode[], filters: OrgSearchForm): OrgUnitNode[] => {
  if (!filters.keyword && filters.status == null) return [...nodes];
  const matched: OrgUnitNode[] = [];
  nodes.forEach((node) => {
    const children = node.children ? applyOrgFilters(node.children, filters) : [];
    const keywordMatch = filters.keyword ? node.name.includes(filters.keyword) || node.shortName?.includes(filters.keyword) : true;
    const statusMatch = filters.status == null ? true : node.status === filters.status;
    if ((keywordMatch && statusMatch) || children.length > 0) {
      matched.push({ ...node, children });
    }
  });
  return matched;
};

export const filterOrgTreeByKeyword = (nodes: OrgUnitNode[], keywordValue: string): OrgUnitNode[] => {
  const matched: OrgUnitNode[] = [];
  nodes.forEach((node) => {
    const children = node.children ? filterOrgTreeByKeyword(node.children, keywordValue) : [];
    if (node.name.includes(keywordValue) || node.shortName?.includes(keywordValue) || children.length > 0) {
      matched.push({ ...node, children });
    }
  });
  return matched;
};

export const buildOrgReorderItems = (tree: OrgUnitNode[]) => {
  const items: Array<{ id: number; parentId: number | null; sortOrder: number }> = [];
  const walk = (nodes: OrgUnitNode[], parentId: number | null) => {
    nodes.forEach((node, index) => {
      items.push({ id: node.id, parentId, sortOrder: index });
      if (node.children?.length) walk(node.children, node.id);
    });
  };
  walk(tree || [], null);
  return items;
};
