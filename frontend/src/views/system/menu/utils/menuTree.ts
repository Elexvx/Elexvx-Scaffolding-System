import type { MenuNode, MenuReorderItem, MenuType } from '../types';

export const treeFilter = (nodes: MenuNode[], keyword: string): MenuNode[] => {
  const normalizedKeyword = keyword.trim().toLowerCase();
  if (!normalizedKeyword) return nodes;
  const isHit = (node: MenuNode) =>
    (node.titleZhCn || '').toLowerCase().includes(normalizedKeyword) ||
    (node.titleEnUs || '').toLowerCase().includes(normalizedKeyword) ||
    (node.path || '').toLowerCase().includes(normalizedKeyword) ||
    (node.routeName || '').toLowerCase().includes(normalizedKeyword);

  const walk = (list: MenuNode[]): MenuNode[] => {
    const output: MenuNode[] = [];
    list.forEach((node) => {
      const children = node.children?.length ? walk(node.children) : [];
      if (isHit(node) || children.length > 0) {
        output.push({ ...node, children });
      }
    });
    return output;
  };

  return walk(nodes);
};

export const findNodeById = (nodes: MenuNode[], id: number): MenuNode | null => {
  const queue = [...nodes];
  while (queue.length > 0) {
    const current = queue.shift();
    if (!current) continue;
    if (current.id === id) return current;
    if (current.children?.length) {
      queue.unshift(...current.children);
    }
  }
  return null;
};

export const computeFullPath = (node: MenuNode, allTree: MenuNode[]) => {
  const segments: string[] = [];
  const visited = new Set<number>();
  let current: MenuNode | null = node;
  while (current && !visited.has(current.id)) {
    visited.add(current.id);
    segments.unshift(current.path || '');
    current = current.parentId ? findNodeById(allTree, current.parentId) : null;
  }
  const path = segments.join('/').replace(/\/+/g, '/');
  return path.startsWith('/') ? path : `/${path}`;
};

export const buildParentTree = (nodes: MenuNode[], allowTypes: MenuType[]) => {
  const walk = (list: MenuNode[]): MenuNode[] =>
    (list || [])
      .filter((node) => allowTypes.includes(node.nodeType))
      .map((node) => ({
        ...node,
        children: node.children?.length ? walk(node.children) : [],
      }));
  return walk(nodes);
};

export const nextOrderNo = (tree: MenuNode[], parentId: number) => {
  const parent = findNodeById(tree, parentId);
  const siblings = parent?.children || [];
  let max = -1;
  siblings.forEach((item) => {
    max = Math.max(max, item.orderNo == null ? 0 : item.orderNo);
  });
  return max + 1;
};

export const buildReorderItems = (tree: MenuNode[]) => {
  const items: MenuReorderItem[] = [];
  const walk = (nodes: MenuNode[], parentId: number | null) => {
    nodes.forEach((node, index) => {
      items.push({ id: node.id, parentId, orderNo: index, version: node.version });
      if (node.children?.length) {
        walk(node.children, node.id);
      }
    });
  };
  walk(tree, null);
  return items;
};

export const updateTreeData = (nodes: MenuNode[], id: number, patch: Partial<MenuNode>) => {
  for (let index = 0; index < nodes.length; index += 1) {
    const item = nodes[index];
    if (item.id === id) {
      const children = item.children;
      Object.assign(item, patch);
      if (children && patch.children == null) item.children = children;
      return true;
    }
    if (item.children?.length && updateTreeData(item.children, id, patch)) {
      return true;
    }
  }
  return false;
};

