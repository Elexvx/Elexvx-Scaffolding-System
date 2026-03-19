import type { MenuNode } from './types';

export const EXPANDED_STORAGE_KEY = 'elexvx.menu.tree.expanded';
export const LEGACY_EXPANDED_STORAGE_KEY = 'tdesign.menu.tree.expanded';

export const normalizeExpanded = (values: Array<string | number>) => {
  const seen = new Set<string>();
  const out: Array<string | number> = [];
  values.forEach((val) => {
    const key = String(val);
    if (!key || seen.has(key)) return;
    seen.add(key);
    out.push(val);
  });
  return out;
};

export const collectNodeIds = (nodes: MenuNode[], out: Set<string>) => {
  (nodes || []).forEach((node) => {
    if (node?.id != null) out.add(String(node.id));
    if (node?.children?.length) collectNodeIds(node.children, out);
  });
};

export const readExpandedFromStorage = (migrateLegacyKey: () => void) => {
  if (typeof window === 'undefined') return [] as Array<string | number>;
  try {
    migrateLegacyKey();
    const raw = window.localStorage.getItem(EXPANDED_STORAGE_KEY);
    if (!raw) return [] as Array<string | number>;
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? normalizeExpanded(parsed) : [];
  } catch {
    return [] as Array<string | number>;
  }
};

export const persistExpanded = (values: Array<string | number>) => {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(EXPANDED_STORAGE_KEY, JSON.stringify(normalizeExpanded(values)));
  } catch {}
};

export const sanitizeExpanded = (values: Array<string | number>, nodes: MenuNode[]) => {
  const ids = new Set<string>();
  collectNodeIds(nodes, ids);
  return normalizeExpanded(values).filter((val) => ids.has(String(val)));
};
