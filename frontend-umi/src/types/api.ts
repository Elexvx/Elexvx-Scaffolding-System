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

export interface PageResult<T> {
  total: number;
  records: T[];
}

export interface OptionItem {
  label: string;
  value: string | number;
}
