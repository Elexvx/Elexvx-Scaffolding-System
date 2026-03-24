import type { OrgUnitNode } from '../types';

const ORG_UNIT_TYPES = new Set(['UNIT']);
const DEPARTMENT_TYPES = new Set(['DEPARTMENT', 'SECTION', 'TEAM']);
const TYPE_LABEL_MAP = new Map([
  ['单位', 'UNIT'],
  ['部门', 'DEPARTMENT'],
  ['科室', 'SECTION'],
  ['班组', 'TEAM'],
  ['用户', 'USER'],
]);

export const normalizeOrgUnitType = (value?: string | null) => (value || '').toString().trim().toUpperCase();

export const resolveOrgUnitType = (node?: OrgUnitNode | null) => {
  if (!node) return '';
  const rawType = node.type == null ? '' : String(node.type).trim();
  let normalized = normalizeOrgUnitType(rawType);
  if (!normalized && rawType) {
    const mapped = TYPE_LABEL_MAP.get(rawType);
    if (mapped) normalized = mapped;
  }
  if (!normalized && node.typeLabel) {
    const label = String(node.typeLabel).trim();
    normalized = TYPE_LABEL_MAP.get(label) || normalizeOrgUnitType(label);
  }
  return normalized;
};

export const resolveOrgFilterIds = (node?: OrgUnitNode | null) => {
  if (!node) return { orgUnitId: null as number | null, departmentId: null as number | null };
  const nodeType = resolveOrgUnitType(node);
  if (DEPARTMENT_TYPES.has(nodeType)) return { orgUnitId: null, departmentId: node.id };
  if (ORG_UNIT_TYPES.has(nodeType)) return { orgUnitId: node.id, departmentId: null };
  return { orgUnitId: node.id, departmentId: null };
};
