export type NodeType = 'DIR' | 'PAGE' | 'BTN';

export interface MenuNode {
  id: number;
  parentId: number | null;
  nodeType: NodeType;
  path: string;
  routeName: string;
  component?: string;
  redirect?: string;
  titleZhCn: string;
  titleEnUs?: string;
  icon?: string;
  hidden?: boolean;
  frameSrc?: string;
  frameBlank?: boolean;
  enabled?: boolean;
  orderNo?: number;
  version?: number;
  actions?: string;
  children?: MenuNode[];
}

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

export const treeFilter = (nodes: MenuNode[], kw: string): MenuNode[] => {
  const keyword = kw.trim().toLowerCase();
  if (!keyword) return nodes;
  const hit = (n: MenuNode) =>
    (n.titleZhCn || '').toLowerCase().includes(keyword) ||
    (n.titleEnUs || '').toLowerCase().includes(keyword) ||
    (n.path || '').toLowerCase().includes(keyword) ||
    (n.routeName || '').toLowerCase().includes(keyword);
  const walk = (list: MenuNode[]): MenuNode[] => {
    const out: MenuNode[] = [];
    for (const n of list) {
      const children = n.children?.length ? walk(n.children) : [];
      if (hit(n) || children.length) {
        out.push({ ...n, children });
      }
    }
    return out;
  };
  return walk(nodes);
};

export const buildReorderItems = (tree: MenuNode[]) => {
  const items: Array<{ id: number; parentId: number | null; orderNo: number; version?: number }> = [];
  const walk = (nodes: MenuNode[], parentId: number | null) => {
    nodes.forEach((n, idx) => {
      items.push({ id: n.id, parentId, orderNo: idx, version: n.version });
      if (n.children?.length) walk(n.children, n.id);
    });
  };
  walk(tree, null);
  return items;
};

export const normalizeParentId = (v: unknown): number | null => {
  if (v == null || v === '') return null;
  const n = typeof v === 'number' ? v : Number(v);
  return Number.isFinite(n) ? n : null;
};

export const toSlug = (input: string) => {
  const raw = String(input || '')
    .trim()
    .toLowerCase();
  if (!raw) return '';
  return raw
    .replace(/\s+/g, '-')
    .replace(/[^a-z0-9-_]/g, '')
    .replace(/-+/g, '-')
    .replace(/^[-_]+|[-_]+$/g, '');
};

export const toRouteName = (slug: string) => {
  const parts = String(slug || '')
    .split(/[-_]/)
    .filter(Boolean);
  if (!parts.length) return '';
  return parts.map((s) => s.charAt(0).toUpperCase() + s.slice(1)).join('');
};

export const buildPageComponentOptions = () => {
  const viewModules = import.meta.glob('/src/pages/**/*.vue');
  return Object.keys(viewModules)
    .map((k) => k.replace(/^\.\.\//, '/').replace(/\.vue$/, ''))
    .filter((k) => k !== '/system/menu/index')
    .sort()
    .map((k) => ({ label: k, value: k }));
};
