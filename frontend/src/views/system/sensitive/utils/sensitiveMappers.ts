import type { SensitivePageSetting } from '@/api/system/sensitive';

import type { SensitiveMenuNode, SensitivePageRow, SensitivePageTreeNode } from '../types';

export const resolveSensitivePagePath = (parentPath: string, nodePath: string) => {
  const segment = String(nodePath || '').trim();
  if (!segment) return String(parentPath || '').trim();
  if (segment.startsWith('/')) return segment;
  const base = String(parentPath || '').trim();
  if (!base || base === '/') return `/${segment}`;
  const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base;
  return `${normalizedBase}/${segment}`;
};

export const buildSensitivePageTree = (
  nodes: SensitiveMenuNode[],
  enabledMap: Map<string, SensitivePageSetting>,
  parentPath = '',
): SensitivePageTreeNode[] => {
  const out: SensitivePageTreeNode[] = [];
  (nodes || []).forEach((node) => {
    if (node.hidden) return;
    const isBtn = node.nodeType === 'BTN';
    const nextPath = isBtn ? parentPath : resolveSensitivePagePath(parentPath, node.path);
    const children = node.children?.length ? buildSensitivePageTree(node.children, enabledMap, nextPath) : [];
    const isDir = node.nodeType === 'DIR';
    const isPage = node.nodeType === 'PAGE' && node.component && node.component !== 'IFRAME';
    if (isDir) {
      if (children.length) {
        out.push({
          id: node.id,
          nodeType: node.nodeType,
          titleZhCn: node.titleZhCn,
          titleEnUs: node.titleEnUs,
          path: node.path,
          routeName: node.routeName,
          component: node.component,
          fullPath: nextPath,
          children,
        });
      }
      return;
    }
    if (isPage) {
      out.push({
        id: node.id,
        nodeType: node.nodeType,
        titleZhCn: node.titleZhCn,
        titleEnUs: node.titleEnUs,
        path: node.path,
        routeName: node.routeName,
        component: node.component,
        fullPath: nextPath,
        enabled: enabledMap.get(nextPath)?.enabled ?? false,
        children: children.length ? children : undefined,
      });
      return;
    }
    out.push(...children);
  });
  return out;
};

export const collectSensitivePageKeys = (nodes: SensitivePageTreeNode[]) => {
  const keys: string[] = [];
  const walk = (list: SensitivePageTreeNode[]) => {
    (list || []).forEach((node) => {
      if (node.nodeType === 'PAGE' && node.fullPath) keys.push(node.fullPath);
      if (node.children?.length) walk(node.children);
    });
  };
  walk(nodes);
  return keys;
};

export const filterSensitivePageTree = (nodes: SensitivePageTreeNode[], keywordRaw: string): SensitivePageTreeNode[] => {
  const kw = keywordRaw.trim().toLowerCase();
  if (!kw) return nodes;
  const hit = (node: SensitivePageTreeNode) =>
    (node.titleZhCn || '').toLowerCase().includes(kw) || (node.fullPath || '').toLowerCase().includes(kw);
  const walk = (list: SensitivePageTreeNode[]): SensitivePageTreeNode[] => {
    const out: SensitivePageTreeNode[] = [];
    for (const node of list) {
      const children = node.children?.length ? walk(node.children) : [];
      if (hit(node) || children.length) out.push({ ...node, children: children.length ? children : undefined });
    }
    return out;
  };
  return walk(nodes);
};

export const flattenSensitiveTreePages = (nodes: SensitivePageTreeNode[]) => {
  const list: SensitivePageRow[] = [];
  const walk = (arr: SensitivePageTreeNode[]) => {
    (arr || []).forEach((node) => {
      if (node.nodeType === 'PAGE' && node.fullPath) {
        list.push({
          pageKey: node.fullPath,
          pageName: node.titleZhCn || node.fullPath,
          enabled: !!node.enabled,
        });
      }
      if (node.children?.length) walk(node.children);
    });
  };
  walk(nodes);
  return list;
};
