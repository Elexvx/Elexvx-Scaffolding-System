import type { UserProfile, UserProfileUpdate } from '@/api/user';

import type { AreaOption, ProfileFormModel } from '../types';
import { docTypePassport, docTypeResidentIdCard } from '../constants/profileOptions';
import { normalizeDocumentType } from './profileGuards';

export const maskMiddle = (value: string, prefixKeep: number, suffixKeep: number, maskChar = '*') => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  const keepHead = Math.max(0, prefixKeep);
  const keepTail = Math.max(0, suffixKeep);
  if (text.length <= keepHead + keepTail) return maskChar.repeat(Math.max(4, text.length));
  const maskedLen = Math.max(4, text.length - keepHead - keepTail);
  return `${text.slice(0, keepHead)}${maskChar.repeat(maskedLen)}${text.slice(text.length - keepTail)}`;
};

export const maskPhone = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  const digits = text.replace(/\D/g, '');
  if (digits.length === 11) return maskMiddle(digits, 3, 4);
  return maskMiddle(text, 3, 2);
};

export const maskEmail = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  const at = text.indexOf('@');
  if (at <= 1) return text;
  return `${text.slice(0, 1)}****${text.slice(at)}`;
};

export const maskIdCard = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  if (text.length >= 8) return maskMiddle(text, 4, 4);
  return maskMiddle(text, 1, 1);
};

export const maskAddress = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  if (text.length <= 6) return `${text[0]}****`;
  return `${text.slice(0, 6)}****`;
};

export const maskAccount = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  if (text.length <= 4) return maskMiddle(text, 1, 0);
  return maskMiddle(text, 2, 2);
};

export const toAreaDictValue = (raw: unknown): string | number => {
  if (typeof raw === 'number' && Number.isFinite(raw)) return raw;
  if (typeof raw === 'string') return raw;
  if (typeof raw === 'boolean') return raw ? 'true' : 'false';
  if (raw == null) return '';
  return String(raw);
};

export const toNumericId = (value: unknown) => (typeof value === 'number' && Number.isFinite(value) ? value : null);

export const normalizeGender = (value: string | undefined, hasUnknown: boolean) =>
  value === 'secret' && hasUnknown ? 'unknown' : value || '';

export const resolveDocumentNoPlaceholder = (idType?: string) => {
  const type = normalizeDocumentType(idType);
  if (type === docTypeResidentIdCard) return '请输入18位居民身份证号码';
  if (type === docTypePassport) return '请输入护照号码（5-17位字母数字）';
  return '请先选择证件类型，再输入证件号码';
};

export const buildFullAddress = (profile: UserProfile) => {
  const province = (profile.province || '').trim();
  const city = (profile.city || '').trim();
  const district = (profile.district || '').trim();
  const address = (profile.address || '').trim();
  const parts = [province, city, district, address].filter(Boolean);
  return parts.filter((part, idx) => idx === 0 || part !== parts[idx - 1]).join('');
};

export const applyProfileToForm = (form: ProfileFormModel, profile: UserProfile, normalizedGender: string) => {
  Object.assign(form, {
    name: profile.name || '',
    nickname: profile.nickname || '',
    gender: normalizedGender,
    mobile: profile.mobile || '',
    email: profile.email || '',
    idType: normalizeDocumentType(profile.idType),
    idCard: profile.idCard || '',
    idValidFrom: profile.idValidFrom || '',
    idValidTo: profile.idValidTo || '',
    provinceId: profile.provinceId ?? null,
    province: profile.province || '',
    cityId: profile.cityId ?? null,
    city: profile.city || '',
    districtId: profile.districtId ?? null,
    district: profile.district || '',
    zipCode: profile.zipCode || '',
    address: profile.address || '',
    tags: profile.tags || '',
  });
};

export const createBasicProfilePayload = (form: ProfileFormModel): UserProfileUpdate => ({
  name: form.name?.trim() || '',
  nickname: form.nickname?.trim() || '',
  gender: form.gender || '',
  mobile: form.mobile?.trim() || '',
  email: form.email?.trim() || '',
  provinceId: form.provinceId,
  province: form.province || '',
  cityId: form.cityId,
  city: form.city || '',
  districtId: form.districtId,
  district: form.district || '',
  zipCode: form.zipCode?.trim() || '',
  address: form.address?.trim() || '',
});

export const createDocumentProfilePayload = (form: ProfileFormModel): UserProfileUpdate => ({
  idType: normalizeDocumentType(form.idType) || '',
  idCard: form.idCard?.trim() || '',
  idValidFrom: form.idValidFrom || undefined,
  idValidTo: form.idValidTo || undefined,
});

export const resolveAreaPathFromProfile = (data: UserProfile, options: AreaOption[]) => {
  const province = options.find((item) => item.label === data.province || String(item.value) === data.province);
  const cities = Array.isArray(province?.children) ? province.children : [];
  const city = cities.find((item) => item.label === data.city || String(item.value) === data.city);
  const districts = Array.isArray(city?.children) ? city.children : [];
  const districtFromCity = districts.find((item) => item.label === data.district || String(item.value) === data.district);
  const districtFromProvince = cities.find((item) => item.label === data.district || String(item.value) === data.district);
  return (districtFromCity ? [province, city, districtFromCity] : [province, districtFromProvince]).filter(Boolean) as AreaOption[];
};
