const migrateStorageKey = (storage: Storage, nextKey: string, legacyKeys: string[]) => {
  const existing = storage.getItem(nextKey);
  if (existing !== null) return existing;
  for (const legacyKey of legacyKeys) {
    const legacyValue = storage.getItem(legacyKey);
    if (legacyValue !== null) {
      // 新旧键兼容，后续版本再移除旧键。
      storage.setItem(nextKey, legacyValue);
      storage.removeItem(legacyKey);
      return legacyValue;
    }
  }
  return null;
};

export const migrateLocalStorageKey = (nextKey: string, legacyKeys: string[]) => {
  if (typeof window === 'undefined') return null;
  return migrateStorageKey(window.localStorage, nextKey, legacyKeys);
};

export const migrateSessionStorageKey = (nextKey: string, legacyKeys: string[]) => {
  if (typeof window === 'undefined') return null;
  return migrateStorageKey(window.sessionStorage, nextKey, legacyKeys);
};
