import type { MetricPoint, RedisInfo } from '../types';

export const safeInt = (v?: string | number) => {
  if (v == null) return 0;
  if (typeof v === 'number' && Number.isFinite(v)) return v;
  const n = Number.parseInt(String(v), 10);
  return Number.isFinite(n) ? n : 0;
};

export const parseHumanToMb = (v?: string) => {
  const raw = String(v || '').trim();
  if (!raw) return 0;
  const m = raw.match(/^(-?\d+(?:\.\d+)?)\s*([KMGTP]?)B?$/i);
  if (!m) return 0;
  const num = Number(m[1]);
  const unit = String(m[2] || '').toUpperCase();
  const factor = unit === 'K' ? 1 / 1024 : unit === 'M' ? 1 : unit === 'G' ? 1024 : unit === 'T' ? 1024 * 1024 : unit === 'P' ? 1024 * 1024 * 1024 : 1 / (1024 * 1024);
  return Number((num * factor).toFixed(2));
};

export const createMetricPoint = (info: RedisInfo): MetricPoint => ({
  timestamp: Number(info.timestamp || Date.now()),
  memory: Number(info.usedMemory || 0) / 1024 / 1024,
  ops: Number(info.instantaneous_ops_per_sec || 0),
});
