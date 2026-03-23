export interface ApiEnvelope<T> {
  code: number;
  message: string;
  userTip?: string;
  success?: boolean;
  data: T;
}

export interface PageQuery {
  current?: number;
  pageSize?: number;
  keyword?: string;
  [key: string]: unknown;
}

export interface NormalizedPageResult<T> {
  data: T[];
  total: number;
  page: number;
  size: number;
}

export interface OptionItem {
  label: string;
  value: string | number;
}
