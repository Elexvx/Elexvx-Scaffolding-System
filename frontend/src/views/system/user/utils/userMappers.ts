import { normalizeDocumentType } from '../helpers';
import type { UserFormModel, UserRow, UserSubmitPayload } from '../types';

export const normalizeGenderValue = (value: string | undefined, hasUnknownOption: boolean) => {
  if (value === 'secret' && hasUnknownOption) {
    return 'unknown';
  }
  return value || '';
};

export const applyUserRowToForm = (form: UserFormModel, data: UserRow, normalizedGender: string) => {
  form.account = data.account || '';
  form.name = data.name || '';
  form.nickname = data.nickname || '';
  form.gender = normalizedGender;
  form.roles = [...(data.roles || [])];
  form.mobile = data.mobile || '';
  form.email = data.email || '';
  form.idType = normalizeDocumentType(data.idType);
  form.idCard = data.idCard || '';
  form.idValidFrom = data.idValidFrom || '';
  form.idValidTo = data.idValidTo || '';
  form.joinDay = data.joinDay || '';
  form.team = data.team || '';
  form.provinceId = data.provinceId ?? null;
  form.province = data.province || '';
  form.cityId = data.cityId ?? null;
  form.city = data.city || '';
  form.districtId = data.districtId ?? null;
  form.district = data.district || '';
  form.zipCode = data.zipCode || '';
  form.address = data.address || '';
  form.orgUnitIds = [...(data.orgUnitIds || [])];
  form.departmentIds = [...(data.departmentIds || [])];
  if (form.orgUnitIds.length > 0 && form.departmentIds.length > 0) {
    form.departmentIds = [];
  }
  form.status = data.status ?? 1;
};

export const createUserSubmitPayload = (form: UserFormModel, withPassword: boolean): UserSubmitPayload => ({
  account: form.account,
  name: form.name,
  nickname: form.nickname.trim(),
  gender: form.gender || '',
  password: withPassword ? form.password || undefined : undefined,
  roles: form.roles,
  mobile: form.mobile || undefined,
  email: form.email || undefined,
  idType: normalizeDocumentType(form.idType) || '',
  idCard: form.idCard.trim(),
  idValidFrom: form.idValidFrom || undefined,
  idValidTo: form.idValidTo || undefined,
  joinDay: form.joinDay || undefined,
  team: form.team || undefined,
  provinceId: form.provinceId,
  province: form.province || '',
  cityId: form.cityId,
  city: form.city || '',
  districtId: form.districtId,
  district: form.district || '',
  zipCode: form.zipCode || '',
  address: form.address.trim(),
  orgUnitIds: form.orgUnitIds,
  departmentIds: form.departmentIds,
  status: form.status,
});

export const formatOrgPath = (names?: string[]) => {
  if (!names || names.length === 0) return '-';
  return names.join(' / ');
};

