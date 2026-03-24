import { DialogPlugin } from 'tdesign-vue-next';

export const useSensitiveDialog = () => {
  const confirmDeleteWord = (word: string) =>
    new Promise<boolean>((resolve) => {
      const dialog = DialogPlugin.confirm({
        header: '确认删除',
        body: `确定删除敏感词 “${word}” 吗？`,
        confirmBtn: '删除',
        cancelBtn: '取消',
        onConfirm: () => {
          dialog.hide();
          resolve(true);
        },
        onClose: () => resolve(false),
      });
    });

  const confirmBatchDeleteWords = (count: number) =>
    new Promise<boolean>((resolve) => {
      const dialog = DialogPlugin.confirm({
        header: '确认批量删除',
        body: `确定删除已选 ${count} 个敏感词吗？`,
        confirmBtn: '删除',
        cancelBtn: '取消',
        onConfirm: () => {
          dialog.hide();
          resolve(true);
        },
        onClose: () => resolve(false),
      });
    });

  const showImportErrors = (messages: string[]) => {
    if (!messages.length) return;
    DialogPlugin.alert({
      header: '导入异常提示',
      body: messages.join('\n'),
    });
  };

  return { confirmDeleteWord, confirmBatchDeleteWords, showImportErrors };
};
