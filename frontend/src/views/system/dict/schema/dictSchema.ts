import type { FormRule, PrimaryTableCol } from 'tdesign-vue-next';

import type { DictFormModel, DictItemFormModel } from '../types';

export const createDictForm = (): DictFormModel => ({
  name: '',
  code: '',
  status: 1,
  sort: 0,
  remark: '',
});

export const createItemForm = (): DictItemFormModel => ({
  label: '',
  value: '',
  valueType: 'string',
  status: 1,
  sort: 0,
  tagColor: '',
  province: '',
  city: '',
  district: '',
});

export const dictRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入字典名称', type: 'error' }],
  code: [{ required: true, message: '请输入字典编码', type: 'error' }],
};

export const itemRules: Record<string, FormRule[]> = {
  label: [{ required: true, message: '请输入名称', type: 'error' }],
  value: [{ required: true, message: '请输入数据值', type: 'error' }],
};

export const buildItemColumns = (isAddressDistrictDict: boolean): PrimaryTableCol[] => {
  const base: PrimaryTableCol[] = [
    { colKey: 'sort', title: '排序', width: 100 },
    { colKey: 'label', title: '名称', minWidth: 120 },
    { colKey: 'valueType', title: '数据值类型', width: 120 },
    { colKey: 'value', title: '数据值', minWidth: 120 },
  ];
  if (isAddressDistrictDict) {
    base.push(
      { colKey: 'province', title: '省', minWidth: 120 },
      { colKey: 'city', title: '市', minWidth: 120 },
      { colKey: 'district', title: '区', minWidth: 120 },
    );
  }
  base.push(
    { colKey: 'status', title: '状态', width: 100 },
    { colKey: 'tagColor', title: '标签颜色', width: 120 },
    { colKey: 'op', title: '操作', width: 140, fixed: 'right' },
  );
  return base;
};
