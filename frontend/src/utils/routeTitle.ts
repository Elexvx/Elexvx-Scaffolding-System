/**
 * 路由标题解析工具。
 *
 * - 支持 string 或 i18n 结构（Record<locale,title>）
 * - 会过滤常见“占位符标题”（例如仅由符号组成），避免渲染到页面标题/菜单里
 */
const PLACEHOLDER_TITLE_PATTERN = /^[\s?？._\-*/\\|]+$/u;

export function isPlaceholderTitle(value?: string | null): boolean {
  if (value == null) return true;
  const normalized = String(value).trim();
  if (!normalized) return true;
  return PLACEHOLDER_TITLE_PATTERN.test(normalized);
}

export function resolveRouteTitle(
  title: string | Record<string, string> | undefined,
  locale?: string,
  fallback = '',
): string {
  if (!title) return fallback;
  if (typeof title === 'string') {
    return isPlaceholderTitle(title) ? fallback : title;
  }

  const candidates: string[] = [];
  if (locale && title[locale]) candidates.push(title[locale]);
  if (title.zh_CN) candidates.push(title.zh_CN);
  if (title.en_US) candidates.push(title.en_US);
  Object.values(title).forEach((value) => {
    if (value != null) candidates.push(value);
  });

  for (const candidate of candidates) {
    if (!isPlaceholderTitle(candidate)) return candidate.trim();
  }

  return fallback;
}
