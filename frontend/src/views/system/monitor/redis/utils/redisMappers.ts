import type { RedisInfo, RedisMonitorResponse } from '../types';

export const applyRedisResponse = (response: RedisMonitorResponse) => {
  const info: RedisInfo = { ...(response.info || {}) };
  if (response.timestamp != null) info.timestamp = response.timestamp;
  if (response.usedMemory != null) info.usedMemory = response.usedMemory;
  if (response.instantaneousOpsPerSec != null && typeof info.instantaneous_ops_per_sec !== 'number') info.instantaneous_ops_per_sec = response.instantaneousOpsPerSec;
  if (response.keyCount != null) info.keyCount = response.keyCount;
  if (response.hitCount != null) info.hitCount = response.hitCount;
  if (response.missCount != null) info.missCount = response.missCount;
  return info;
};

export const replaceIfChanged = <T>(currentValue: T, nextValue: T) => {
  try {
    return JSON.stringify(currentValue) === JSON.stringify(nextValue) ? currentValue : nextValue;
  } catch {
    return nextValue;
  }
};
