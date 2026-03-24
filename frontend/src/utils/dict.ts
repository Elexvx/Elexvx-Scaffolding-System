import type { SelectOption } from 'tdesign-vue-next';

import type { SysDictItem } from '@/api/system/dictionary';

export type DictValue = string | number | boolean;

const toKey = (value: unknown) => (value == null ? '' : String(value));

export const parseDictValue = (item: SysDictItem): DictValue => {
  const raw = item.value;
  const type = (item.valueType || 'string').toLowerCase();
  if (type === 'number') {
    const num = Number(raw);
    return Number.isFinite(num) ? num : raw;
  }
  if (type === 'boolean') {
    if (raw === 'true' || raw === '1') return true;
    if (raw === 'false' || raw === '0') return false;
    return raw;
  }
  return raw;
};

export const buildDictOptions = (
  items: SysDictItem[] | undefined,
  fallbackOptions: SelectOption[] = [],
  allowedValues?: Array<DictValue>,
): SelectOption[] => {
  const list = items || [];
  if (list.length === 0) return fallbackOptions;
  const allowed = allowedValues ? new Set(allowedValues.map((val) => toKey(val))) : null;
  const options = list
    .map((item) => ({
      label: item.label,
      value: parseDictValue(item),
      disabled: item.status !== 1,
    }))
    .filter((opt) => (allowed ? allowed.has(toKey(opt.value)) : true));
  return options.length > 0 ? options : fallbackOptions;
};

export const resolveLabel = (
  value: unknown,
  items: SysDictItem[] | undefined,
  fallbackMap?: Record<string, string>,
): string => {
  const key = toKey(value);
  if (items && items.length > 0) {
    const hit = items.find((item) => toKey(parseDictValue(item)) === key || toKey(item.value) === key);
    if (hit?.label) return hit.label;
  }
  if (fallbackMap && key in fallbackMap) return fallbackMap[key];
  return key || '-';
};

export const resolveTagTheme = (
  value: unknown,
  items: SysDictItem[] | undefined,
  fallbackMap?: Record<string, string>,
): string => {
  const key = toKey(value);
  if (items && items.length > 0) {
    const hit = items.find((item) => toKey(parseDictValue(item)) === key || toKey(item.value) === key);
    if (hit?.tagColor) return hit.tagColor;
  }
  if (fallbackMap && key in fallbackMap) return fallbackMap[key];
  return 'default';
};
