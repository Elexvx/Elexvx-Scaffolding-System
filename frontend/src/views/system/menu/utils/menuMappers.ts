import type { MenuFormModel, MenuNode, MenuSubmitPayload } from '../types';

export const normalizeParentId = (value: unknown): number | null => {
  if (value == null || value === '') return null;
  const normalized = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(normalized) ? normalized : null;
};

export const toSlug = (input: string) =>
  String(input || '')
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^a-z0-9-_]/g, '')
    .replace(/-+/g, '-')
    .replace(/^[-_]+|[-_]+$/g, '');

export const toRouteName = (slug: string) => {
  const parts = String(slug || '')
    .split(/[-_]/)
    .filter(Boolean);
  if (!parts.length) return '';
  return parts.map((part) => part.charAt(0).toUpperCase() + part.slice(1)).join('');
};

export const applyMenuNodeToForm = (form: MenuFormModel, node: MenuNode) => {
  form.id = node.id;
  form.version = node.version ?? 0;
  form.parentId = node.parentId ?? null;
  form.nodeType = node.nodeType;
  form.titleZhCn = node.titleZhCn || '';
  form.titleEnUs = node.titleEnUs || '';
  form.path = node.path || '';
  form.routeName = node.routeName || '';
  form.icon = node.icon || '';
  form.redirect = node.redirect || '';
  form.component = node.component || '';
  form.frameSrc = node.frameSrc || '';
  form.hidden = !!node.hidden;
  form.orderNo = node.orderNo ?? 0;
  form.openType = node.frameSrc ? (node.frameBlank ? 'external' : 'iframe') : 'internal';
};

export const normalizePathByParent = (path: string, parentId: number | null) => {
  const normalizedPath = path.trim();
  if (!normalizedPath) return '';
  if (parentId == null) {
    return normalizedPath.startsWith('/') ? normalizedPath : `/${normalizedPath.replace(/^\/+/, '')}`;
  }
  return normalizedPath.replace(/^\/+/, '');
};

export const ensureRouteFields = (form: MenuFormModel) => {
  const seed = form.titleEnUs || form.titleZhCn || `menu-${Date.now()}`;
  const fallback = `menu-${Date.now().toString(36)}`;
  const slug = toSlug(seed) || fallback;
  if (!form.path.trim()) {
    form.path = form.parentId == null ? `/${slug}` : slug;
  }
  if (!form.routeName.trim()) {
    const name = toRouteName(slug);
    form.routeName = name || `Menu${Date.now().toString(36)}`;
  }
  form.path = normalizePathByParent(form.path, form.parentId);
};

export const createMenuSubmitPayload = (form: MenuFormModel): MenuSubmitPayload => {
  const parentId = normalizeParentId(form.parentId);
  const hidden = !!form.hidden;
  return {
    parentId,
    nodeType: form.nodeType,
    path: form.path,
    routeName: form.routeName,
    component: form.component || undefined,
    redirect: form.redirect || undefined,
    titleZhCn: form.titleZhCn,
    titleEnUs: form.titleEnUs || undefined,
    icon: form.icon || undefined,
    enabled: !hidden,
    hidden,
    frameSrc: form.frameSrc || undefined,
    frameBlank: form.openType === 'external',
    orderNo: form.orderNo,
    actions: form.actions.join(','),
  };
};

