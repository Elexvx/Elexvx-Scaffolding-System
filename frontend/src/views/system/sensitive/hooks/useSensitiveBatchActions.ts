import { MessagePlugin } from 'tdesign-vue-next';
import type { Ref } from 'vue';

import { deleteSensitiveWord } from '@/api/system/sensitive';

import { useSensitiveDialog } from './useSensitiveDialog';

export const useSensitiveBatchActions = (
  selectedWordIds: Ref<number[]>,
  batchDeleting: Ref<boolean>,
  reloadWords: () => Promise<void>,
) => {
  const { confirmBatchDeleteWords } = useSensitiveDialog();

  const onSelectChange = (keys: Array<string | number>) => {
    selectedWordIds.value = keys.map((key) => Number(key)).filter((key) => Number.isFinite(key));
  };

  const handleBatchDelete = async () => {
    const ids = [...new Set(selectedWordIds.value)];
    if (!ids.length) return;
    const confirmed = await confirmBatchDeleteWords(ids.length);
    if (!confirmed) return;
    batchDeleting.value = true;
    try {
      const results = await Promise.allSettled(ids.map((id) => deleteSensitiveWord(id)));
      const failedCount = results.filter((result) => result.status === 'rejected').length;
      const successCount = ids.length - failedCount;
      if (failedCount > 0) {
        MessagePlugin.warning(`已删除 ${successCount} 个，失败 ${failedCount} 个`);
      } else {
        MessagePlugin.success(`已删除 ${successCount} 个`);
      }
    } finally {
      batchDeleting.value = false;
      selectedWordIds.value = [];
      await reloadWords();
    }
  };

  return { onSelectChange, handleBatchDelete };
};
