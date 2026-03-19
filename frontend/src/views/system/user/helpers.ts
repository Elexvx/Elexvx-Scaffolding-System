export interface OrgUnitNode {
  id: number;
  parentId?: number | null;
  name: string;
  type?: string;
  typeLabel?: string;
  disabled?: boolean;
  children?: OrgUnitNode[];
}

export interface AreaOption {
  label: string;
  value: number | string;
  level?: number;
  zipCode?: string | null;
  children?: AreaOption[] | boolean;
}

const ORG_UNIT_TYPES = new Set(['UNIT']);
const DEPARTMENT_TYPES = new Set(['DEPARTMENT', 'SECTION', 'TEAM']);
const TYPE_LABEL_MAP = new Map([
  ['单位', 'UNIT'],
  ['部门', 'DEPARTMENT'],
  ['科室', 'SECTION'],
  ['班组', 'TEAM'],
  ['用户', 'USER'],
]);

const DOC_TYPE_RESIDENT_ID_CARD = 'resident_id_card';
const DOC_TYPE_PASSPORT = 'passport';
const RESIDENT_ID_CARD_WEIGHTS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
const RESIDENT_ID_CARD_CHECKSUM_CODES = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];

export const documentTypeFallbackOptions = [
  { label: '居民身份证', value: DOC_TYPE_RESIDENT_ID_CARD },
  { label: '护照', value: DOC_TYPE_PASSPORT },
];

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

export const buildOrgUnitTree = (nodes: OrgUnitNode[]): OrgUnitNode[] => {
  const result = nodes
    .map((node) => {
      const isOrg = ORG_UNIT_TYPES.has(resolveOrgUnitType(node));
      if (!isOrg) return null;
      const children = node.children ? buildOrgUnitTree(node.children) : [];
      return { ...node, children };
    })
    .filter((node) => node !== null);
  return result as OrgUnitNode[];
};

export const findNodeById = (nodes: OrgUnitNode[], targetId: number | string): OrgUnitNode | null => {
  for (const node of nodes) {
    if (String(node.id) === String(targetId)) return node;
    if (node.children && node.children.length) {
      const found = findNodeById(node.children, targetId);
      if (found) return found;
    }
  }
  return null;
};

const buildDepartmentSubtree = (node: OrgUnitNode): OrgUnitNode | null => {
  const children = node.children
    ? node.children.map((child) => buildDepartmentSubtree(child)).filter((child): child is OrgUnitNode => !!child)
    : [];
  const resolvedType = resolveOrgUnitType(node);
  const isDept = DEPARTMENT_TYPES.has(resolvedType);
  const isOrg = ORG_UNIT_TYPES.has(resolvedType);
  if (!isDept && !isOrg && children.length === 0) return null;
  return { ...node, children, disabled: !isDept };
};

export const buildDepartmentTree = (nodes: OrgUnitNode[]): OrgUnitNode[] =>
  nodes.map((node) => buildDepartmentSubtree(node)).filter((node): node is OrgUnitNode => !!node);

export const collectSelectableIds = (
  nodes: OrgUnitNode[],
  predicate: (node: OrgUnitNode) => boolean,
  bucket: Set<number> = new Set(),
): Set<number> => {
  nodes.forEach((node) => {
    if (predicate(node)) bucket.add(node.id);
    if (node.children && node.children.length) collectSelectableIds(node.children, predicate, bucket);
  });
  return bucket;
};

export const isOrgNode = (node: OrgUnitNode) => ORG_UNIT_TYPES.has(resolveOrgUnitType(node));

export const isDepartmentNode = (node: OrgUnitNode) => DEPARTMENT_TYPES.has(resolveOrgUnitType(node));

export const normalizeDocumentType = (value?: string) => {
  if (!value) return '';
  const raw = String(value).trim();
  if (!raw) return '';
  const lower = raw.toLowerCase();
  if (['resident_id_card', 'id_card', 'identity_card', 'china_id_card'].includes(lower) || raw === '居民身份证') {
    return DOC_TYPE_RESIDENT_ID_CARD;
  }
  if (lower === 'passport' || raw === '护照') {
    return DOC_TYPE_PASSPORT;
  }
  return lower;
};

const isValidResidentIdCard = (value: string) => {
  const text = value.trim().toUpperCase();
  if (!/^[1-9]\d{16}[0-9X]$/.test(text)) return false;
  const birth = text.slice(6, 14);
  if (!/^\d{8}$/.test(birth)) return false;
  const y = Number(birth.slice(0, 4));
  const m = Number(birth.slice(4, 6));
  const d = Number(birth.slice(6, 8));
  const date = new Date(y, m - 1, d);
  if (date.getFullYear() !== y || date.getMonth() !== m - 1 || date.getDate() !== d) return false;
  let sum = 0;
  for (let i = 0; i < 17; i += 1) sum += Number(text[i]) * RESIDENT_ID_CARD_WEIGHTS[i];
  return RESIDENT_ID_CARD_CHECKSUM_CODES[sum % 11] === text[17];
};

const isValidPassport = (value: string) => /^[A-Z0-9]{5,17}$/.test(value.trim().toUpperCase());

export const validateDocumentNumber = (docType?: string, docNo?: string) => {
  const number = (docNo || '').trim();
  if (!number) return true;
  const type = normalizeDocumentType(docType);
  if (!type) return false;
  if (type === DOC_TYPE_RESIDENT_ID_CARD) return isValidResidentIdCard(number);
  if (type === DOC_TYPE_PASSPORT) return isValidPassport(number);
  return false;
};

export const resolveDocumentNoPlaceholder = (idType?: string) => {
  const type = normalizeDocumentType(idType);
  if (type === DOC_TYPE_RESIDENT_ID_CARD) return '请输入18位居民身份证号码';
  if (type === DOC_TYPE_PASSPORT) return '请输入护照号码（5-17位字母数字）';
  return '请先选择证件类型，再输入证件号码';
};

export const filterTree = (nodes: OrgUnitNode[], keywordValue: string): OrgUnitNode[] => {
  const matched: OrgUnitNode[] = [];
  nodes.forEach((node) => {
    const children = node.children ? filterTree(node.children, keywordValue) : [];
    if (node.name.includes(keywordValue) || children.length > 0) {
      matched.push({ ...node, children });
    }
  });
  return matched;
};

export const flattenOrgIds = (nodes: OrgUnitNode[]): number[] => {
  const ids: number[] = [];
  nodes.forEach((node) => {
    ids.push(node.id);
    if (node.children && node.children.length) {
      ids.push(...flattenOrgIds(node.children));
    }
  });
  return ids;
};
