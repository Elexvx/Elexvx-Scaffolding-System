import type { EnhancedTableInstanceFunctions, UploadFile } from 'tdesign-vue-next';
import { reactive, ref } from 'vue';

import { sensitiveWordPaginationDefault } from '../constants/sensitiveOptions';
import type { SensitivePageTreeNode, SensitiveWordData } from '../types';

export const useSensitivePageState = () => {
  const loadingWords = ref(false);
  const savingWord = ref(false);
  const words = ref<SensitiveWordData[]>([]);
  const pagination = reactive({ ...sensitiveWordPaginationDefault });
  const selectedWordIds = ref<number[]>([]);
  const batchDeleting = ref(false);
  const importFiles = ref<UploadFile[]>([]);
  const progressPercent = ref(0);

  const settingsEnabled = ref(false);
  const loadingPages = ref(false);
  const savingSettings = ref(false);
  const expandAll = ref(false);
  const expandedPageTreeNodes = ref<Array<string | number>>([]);
  const pageTree = ref<SensitivePageTreeNode[]>([]);
  const pageTableRef = ref<EnhancedTableInstanceFunctions<SensitivePageTreeNode> | null>(null);

  return {
    loadingWords,
    savingWord,
    words,
    pagination,
    selectedWordIds,
    batchDeleting,
    importFiles,
    progressPercent,
    settingsEnabled,
    loadingPages,
    savingSettings,
    expandAll,
    expandedPageTreeNodes,
    pageTree,
    pageTableRef,
  };
};
