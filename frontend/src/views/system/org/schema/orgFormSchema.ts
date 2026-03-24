import type { FormRule } from 'tdesign-vue-next';

import type { OrgFormModel } from '../types';

export const createOrgFormModel = (): OrgFormModel => ({
  parentId: undefined,
  name: '',
  shortName: '',
  type: 'DEPARTMENT',
  status: 1,
  leaderIds: [],
  phone: '',
  email: '',
});

export const resetOrgFormModel = (form: OrgFormModel) => {
  form.parentId = undefined;
  form.name = '';
  form.shortName = '';
  form.type = 'DEPARTMENT';
  form.status = 1;
  form.phone = '';
  form.email = '';
  form.leaderIds = [];
};

export const orgFormRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入机构名称', type: 'error' }],
  type: [{ required: true, message: '请选择机构类型', type: 'error' }],
};
