import { defineStore } from 'pinia';

import type { SysDictItem } from '@/api/system/dictionary';
import { fetchDictItemsByCode } from '@/api/system/dictionary';

interface DictionaryState {
  dictCache: Record<string, SysDictItem[]>;
  loadingCodes: Record<string, boolean>;
}

export const useDictionaryStore = defineStore('dictionary', {
  state: (): DictionaryState => ({
    dictCache: {},
    loadingCodes: {},
  }),
  actions: {
    async getDictItems(code: string, force = false) {
      const key = code.trim();
      if (!key) return [] as SysDictItem[];
      if (!force && this.dictCache[key]) return this.dictCache[key];
      if (this.loadingCodes[key]) return this.dictCache[key] || [];
      this.loadingCodes[key] = true;
      try {
        const items = await fetchDictItemsByCode(key);
        this.dictCache[key] = items || [];
        return this.dictCache[key];
      } finally {
        this.loadingCodes[key] = false;
      }
    },
    clearDictCache(code?: string) {
      if (code) {
        delete this.dictCache[code];
      } else {
        this.dictCache = {};
      }
    },
  },
});
