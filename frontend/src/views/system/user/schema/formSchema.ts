import type { FormRule } from 'tdesign-vue-next';
import { reactive } from 'vue';

import { normalizeDocumentType, validateDocumentNumber } from '../helpers';
import type { Mode, ResetPasswordModel, UserFormModel, UserFormRules } from '../types';

export const createUserFormModel = () =>
  reactive<UserFormModel>({
    account: '',
    name: '',
    nickname: '',
    gender: '',
    password: '',
    roles: [],
    mobile: '',
    email: '',
    idType: '',
    idCard: '',
    idValidFrom: '',
    idValidTo: '',
    joinDay: '',
    team: '',
    provinceId: null,
    province: '',
    cityId: null,
    city: '',
    districtId: null,
    district: '',
    zipCode: '',
    address: '',
    orgUnitIds: [],
    departmentIds: [],
    status: 1,
  });

export const createResetPasswordModel = () =>
  reactive<ResetPasswordModel>({
    password: '',
  });

export const resetUserFormModel = (form: UserFormModel) => {
  form.account = '';
  form.name = '';
  form.nickname = '';
  form.gender = '';
  form.password = '';
  form.roles = [];
  form.mobile = '';
  form.email = '';
  form.idType = '';
  form.idCard = '';
  form.idValidFrom = '';
  form.idValidTo = '';
  form.joinDay = '';
  form.team = '';
  form.provinceId = null;
  form.province = '';
  form.cityId = null;
  form.city = '';
  form.districtId = null;
  form.district = '';
  form.zipCode = '';
  form.address = '';
  form.orgUnitIds = [];
  form.departmentIds = [];
  form.status = 1;
};

interface BuildUserFormRulesInput {
  mode: Mode;
  form: UserFormModel;
  passwordRequired: boolean;
  passwordRequirementMessage: string;
  validatePasswordPolicy: (value: string) => boolean;
}

export const buildUserFormRules = ({
  mode,
  form,
  passwordRequired,
  passwordRequirementMessage,
  validatePasswordPolicy,
}: BuildUserFormRulesInput): UserFormRules => {
  const passwordRules: FormRule[] = [];
  if (mode === 'create' && passwordRequired) {
    passwordRules.push({ required: true, message: '请输入初始密码', type: 'error' });
  }
  passwordRules.push({
    validator: (value: string) => validatePasswordPolicy(value),
    message: `密码需满足：${passwordRequirementMessage}`,
    type: 'error',
  });

  return {
    account: [
      { required: true, message: '请输入账号', type: 'error' },
      { validator: (value: string) => /^[\w@.-]+$/.test(value), message: '账号包含非法字符', type: 'error' },
    ],
    name: [{ required: true, message: '请输入姓名', type: 'error' }],
    password: mode === 'create' ? passwordRules : [],
    orgUnitIds: [
      {
        validator: () => !(form.orgUnitIds.length > 0 && form.departmentIds.length > 0),
        message: '所属机构与所属部门不能同时选择',
        type: 'error',
      },
    ],
    departmentIds: [
      {
        validator: () => !(form.orgUnitIds.length > 0 && form.departmentIds.length > 0),
        message: '已选择机构时不能选择部门',
        type: 'error',
      },
    ],
    idType: [
      {
        validator: (value: string) => !form.idCard.trim() || Boolean(normalizeDocumentType(value)),
        message: '已填写证件号码时，请先选择证件类型',
        type: 'error',
      },
    ],
    idCard: [
      {
        validator: (value: string) => validateDocumentNumber(form.idType, value),
        message: '证件号码格式与证件类型不匹配',
        type: 'error',
      },
    ],
    idValidTo: [
      {
        validator: (value: string) => !form.idValidFrom || !value || form.idValidFrom <= value,
        message: '证件有效期止不能早于证件有效期起',
        type: 'error',
      },
    ],
    roles: [
      {
        validator: (value: string[]) => Array.isArray(value) && value.length > 0,
        message: '请选择至少一个角色',
        type: 'error',
      },
    ],
  };
};

interface BuildResetFormRulesInput {
  passwordRequired: boolean;
  passwordRequirementMessage: string;
  validatePasswordPolicy: (value: string) => boolean;
}

export const buildResetPasswordRules = ({
  passwordRequired,
  passwordRequirementMessage,
  validatePasswordPolicy,
}: BuildResetFormRulesInput): UserFormRules => {
  const passwordRules: FormRule[] = [];
  if (passwordRequired) {
    passwordRules.push({ required: true, message: '请输入新密码', type: 'error' });
  }
  passwordRules.push({
    validator: (value: string) => validatePasswordPolicy(value || ''),
    message: `密码需满足：${passwordRequirementMessage}`,
    type: 'error',
  });

  return {
    password: passwordRules,
  };
};

