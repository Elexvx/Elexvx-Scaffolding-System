import type { FormRule } from 'tdesign-vue-next';

import type { ProfileFormModel } from '../types';
import { normalizeDocumentType, validateDocumentDateRange, validateDocumentNumber } from '../utils/profileGuards';

export const createProfileFormModel = (): ProfileFormModel => ({
  name: '',
  nickname: '',
  gender: '',
  mobile: '',
  email: '',
  idType: '',
  idCard: '',
  idValidFrom: '',
  idValidTo: '',
  province: '',
  city: '',
  district: '',
  provinceId: null,
  cityId: null,
  districtId: null,
  zipCode: '',
  address: '',
  tags: '',
});

export const basicProfileRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入姓名', type: 'error' }],
  email: [{ email: true, message: '请输入正确的邮箱地址', type: 'warning' }],
};

export const createDocumentProfileRules = (profileForm: ProfileFormModel): Record<string, FormRule[]> => ({
  idType: [
    {
      validator: (val: string) => !profileForm.idCard?.trim() || Boolean(normalizeDocumentType(val)),
      message: '已填写证件号码时，请先选择证件类型',
      type: 'error',
    },
  ],
  idCard: [
    {
      validator: (val: string) => validateDocumentNumber(profileForm.idType, val),
      message: '证件号码格式与证件类型不匹配',
      type: 'error',
    },
  ],
  idValidTo: [
    {
      validator: (val: string) => validateDocumentDateRange(profileForm.idValidFrom, val),
      message: '证件有效期止不能早于证件有效期起',
      type: 'error',
    },
  ],
});
