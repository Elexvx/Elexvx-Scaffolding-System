export const sanitizeTrim = (value: string) => String(value ?? '').trim();
export const sanitizeNoSpace = (value: string) => String(value ?? '').replace(/\s+/g, '');
export const sanitizeCaptcha = (value: string) => sanitizeNoSpace(value).replace(/[^a-z0-9]/gi, '');

export const normalizeAuthErrorMessage = (message: string, fallback: string) => {
  const raw = String(message || '').trim();
  if (!raw) return fallback;
  if (/network error/i.test(raw)) return '网络异常，请检查网络或稍后重试';
  if (/timeout/i.test(raw)) return '请求超时，请稍后重试';
  if (/request interface error/i.test(raw)) return '接口响应异常，请稍后重试';
  return raw;
};

export const normalizeRegisterErrorMessage = (message: string) => {
  const cleaned = String(message || '')
    .replace(/\s*\[\d{3}\]\s*$/, '')
    .trim();
  if (!cleaned) return '';
  const lower = cleaned.toLowerCase();
  if (
    lower.includes('account already exists') ||
    cleaned.includes('账号已存在') ||
    cleaned.includes('数据唯一性冲突') ||
    (lower.includes('duplicate entry') && lower.includes('account')) ||
    (lower.includes('duplicate key') && lower.includes('account')) ||
    (lower.includes('unique constraint') && lower.includes('account'))
  ) {
    return '账号已存在，请更换后重试';
  }
  if (lower.includes('register failed, please retry') || cleaned.includes('注册失败，请稍后重试')) {
    return '注册失败，请稍后重试';
  }
  if (!/^参数校验失败[:：]/.test(cleaned)) return cleaned;
  return cleaned
    .replace(/^参数校验失败[:：]\s*/, '')
    .split(/[;；]/)
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const lowerItem = item.toLowerCase();
      if (lowerItem.includes('account') && lowerItem.includes('required')) return '用户名不能为空';
      if (lowerItem.includes('password') && lowerItem.includes('6-64')) return '密码长度应为6-64位';
      return item;
    })
    .join('；');
};
