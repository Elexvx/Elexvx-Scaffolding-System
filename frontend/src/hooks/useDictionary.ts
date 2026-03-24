import { computed, ref } from 'vue';

import type { SysDictItem } from '@/api/system/dictionary';
import { useDictionaryStore } from '@/store';

/**
 * 字典 Hook：按 code 读取字典项，并转换为下拉 options。
 *
 * - items/options 直接由 dictionary store 的缓存派生
 * - load(force) 触发（或强制触发）后端拉取并刷新缓存
 */
export const useDictionary = (code: string) => {
  const store = useDictionaryStore();
  const loading = ref(false);

  const items = computed<SysDictItem[]>(() => store.dictCache[code] || []);
  const options = computed(() =>
    items.value.map((item) => ({ label: item.label, value: item.value, disabled: item.status !== 1 })),
  );

  const load = async (force = false) => {
    loading.value = true;
    try {
      await store.getDictItems(code, force);
    } finally {
      loading.value = false;
    }
  };

  return {
    items,
    options,
    load,
    loading,
  };
};
