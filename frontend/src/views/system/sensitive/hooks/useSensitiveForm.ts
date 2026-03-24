import type { ProgressContext, SuccessContext, UploadFile } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import type { Ref } from 'vue';

import { importExportApi } from '@/api/importExport';
import { createSensitiveWord } from '@/api/system/sensitive';

import type { SensitiveWordCreateForm } from '../types';
import { parseSensitiveImportResponse, trimSensitiveWord } from '../utils/sensitiveGuards';
import { resetSensitiveWordCreateForm } from '../schema/sensitiveFormSchema';
import { useSensitiveDialog } from './useSensitiveDialog';

interface UseSensitiveFormOptions {
  wordCreateForm: SensitiveWordCreateForm;
  importFiles: Ref<UploadFile[]>;
  progressPercent: Ref<number>;
  savingWord: Ref<boolean>;
  reloadWords: () => Promise<void>;
  resetWordSelection: () => void;
}

export const useSensitiveForm = (options: UseSensitiveFormOptions) => {
  const { wordCreateForm, importFiles, progressPercent, savingWord, reloadWords, resetWordSelection } = options;
  const { showImportErrors } = useSensitiveDialog();

  const handleAddWord = async () => {
    const value = trimSensitiveWord(wordCreateForm.word);
    if (!value) {
      MessagePlugin.warning('请输入敏感词');
      return;
    }
    savingWord.value = true;
    try {
      await createSensitiveWord(value);
      resetSensitiveWordCreateForm(wordCreateForm);
      MessagePlugin.success('添加成功');
      resetWordSelection();
      await reloadWords();
    } finally {
      savingWord.value = false;
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      const response = await importExportApi.sensitive.downloadTemplate();
      await importExportApi.utils.downloadBlobResponse(response as any, 'sensitive_words_template.xlsx');
    } catch (error: any) {
      MessagePlugin.error(String(error?.message || '模板下载失败'));
    }
  };

  const handleImportProgress = (context: ProgressContext) => {
    if (context?.currentFiles) importFiles.value = context.currentFiles;
    const rawPercent = (context as any)?.percent ?? context?.currentFiles?.[0]?.percent ?? 0;
    progressPercent.value = Math.max(0, Math.min(100, Math.round(rawPercent)));
  };

  const resetImportState = () => {
    importFiles.value = [];
    progressPercent.value = 0;
  };

  const handleImportSuccess = async (context: SuccessContext) => {
    const result = parseSensitiveImportResponse((context?.response || {}) as Record<string, unknown>);
    if (result.hasErrorCode) {
      MessagePlugin.error(result.message);
      resetImportState();
      return;
    }
    if (result.message.includes('未获取到统计结果')) {
      MessagePlugin.warning(result.message);
      resetImportState();
      await reloadWords();
      return;
    }
    MessagePlugin.success(result.message);
    showImportErrors(result.errors);
    resetImportState();
    await reloadWords();
  };

  const handleImportFail = () => {
    MessagePlugin.error('导入失败，请检查文件格式');
    resetImportState();
  };

  return {
    handleAddWord,
    handleDownloadTemplate,
    handleImportProgress,
    handleImportSuccess,
    handleImportFail,
  };
};
