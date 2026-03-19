import { docTypePassport, docTypeResidentIdCard, residentIdCardChecksumCodes, residentIdCardWeights } from '../constants/profileOptions';

export const hasText = (value?: string) => Boolean(value && value.trim());

export const normalizeDocumentType = (value?: string) => {
  if (!value) return '';
  const raw = String(value).trim();
  if (!raw) return '';
  const lower = raw.toLowerCase();
  if (['resident_id_card', 'id_card', 'identity_card', 'china_id_card'].includes(lower) || raw === '居民身份证') {
    return docTypeResidentIdCard;
  }
  if (lower === 'passport' || raw === '护照') {
    return docTypePassport;
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
  for (let i = 0; i < 17; i += 1) {
    sum += Number(text[i]) * residentIdCardWeights[i];
  }
  return residentIdCardChecksumCodes[sum % 11] === text[17];
};

const isValidPassport = (value: string) => /^[A-Z0-9]{5,17}$/.test(value.trim().toUpperCase());

export const validateDocumentNumber = (docType?: string, docNo?: string) => {
  const number = (docNo || '').trim();
  if (!number) return true;
  const type = normalizeDocumentType(docType);
  if (!type) return false;
  if (type === docTypeResidentIdCard) return isValidResidentIdCard(number);
  if (type === docTypePassport) return isValidPassport(number);
  return false;
};

export const validateDocumentDateRange = (from?: string, to?: string) => !from || !to || from <= to;
