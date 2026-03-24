import { MessagePlugin } from 'tdesign-vue-next';
import { computed, type Ref } from 'vue';

import type { SensitivePageSetting } from '@/api/system/sensitive';
import { deleteSensitiveWord, fetchSensitiveSettings, fetchSensitiveWords, saveSensitiveSettings } from '@/api/system/sensitive';
import { request } from '@/utils/request';

import type { SensitiveMenuNode, SensitivePageTreeNode, SensitiveWordData } from '../types';
import {
  buildSensitivePageTree,
  collectSensitivePageKeys,
  filterSensitivePageTree,
  flattenSensitiveTreePages,
} from '../utils/sensitiveMappers';
import { useSensitiveDialog } from './useSensitiveDialog';

interface UseSensitiveTableOptions {
  wordKeyword: Ref<string>;
  pagination: { current: number; pageSize: number; total: number };
  words: Ref<SensitiveWordData[]>;
  loadingWords: Ref<boolean>;
  selectedWordIds: Ref<number[]>;
  pageTree: Ref<SensitivePageTreeNode[]>;
  pageKeyword: Ref<string>;
  loadingPages: Ref<boolean>;
  settingsEnabled: Ref<boolean>;
  savingSettings: Ref<boolean>;
}

export const useSensitiveTable = (options: UseSensitiveTableOptions) => {
  const { wordKeyword, pagination, words, loadingWords, selectedWordIds, pageTree, pageKeyword, loadingPages, settingsEnabled, savingSettings } = options;
  const { confirmDeleteWord } = useSensitiveDialog();

  const loadWords = async () => {
    loadingWords.value = true;
    try {
      const response = await fetchSensitiveWords({
        keyword: wordKeyword.value || undefined,
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      words.value = response.list || [];
      pagination.total = response.total || 0;
    } finally {
      loadingWords.value = false;
    }
  };

  const deleteWord = async (row: SensitiveWordData) => {
    const confirmed = await confirmDeleteWord(row.word);
    if (!confirmed) return;
    await deleteSensitiveWord(row.id);
    MessagePlugin.success('删除成功');
    selectedWordIds.value = selectedWordIds.value.filter((id) => id !== row.id);
    await loadWords();
  };

  const onWordsPageChange = (pageInfo: { current: number; pageSize: number }) => {
    pagination.current = pageInfo.current;
    pagination.pageSize = pageInfo.pageSize;
    loadWords();
  };

  const loadSettings = async () => {
    loadingPages.value = true;
    try {
      const [settings, menuTree] = await Promise.all([
        fetchSensitiveSettings(),
        request.get<SensitiveMenuNode[]>({ url: '/system/menu/tree' }),
      ]);

      settingsEnabled.value = settings.enabled;
      const map = new Map<string, SensitivePageSetting>();
      (settings.pages || []).forEach((item) => map.set(item.pageKey, item));
      const tree = buildSensitivePageTree(menuTree || [], map);
      const known = new Set<string>();
      collectSensitivePageKeys(tree).forEach((key) => known.add(key));
      const extras = (settings.pages || []).filter((item) => !known.has(item.pageKey));
      if (extras.length) {
        let seed = -1;
        tree.push({
          id: seed--,
          nodeType: 'DIR',
          titleZhCn: '未在菜单中的页面',
          path: '',
          routeName: '',
          fullPath: '',
          children: extras.map((item) => ({
            id: seed--,
            nodeType: 'PAGE',
            titleZhCn: item.pageName || item.pageKey,
            path: '',
            routeName: '',
            fullPath: item.pageKey,
            enabled: item.enabled ?? false,
          })),
        });
      }
      pageTree.value = tree;
    } finally {
      loadingPages.value = false;
    }
  };

  const filteredPageTree = computed(() => filterSensitivePageTree(pageTree.value, pageKeyword.value));

  const saveSettingsData = async () => {
    savingSettings.value = true;
    try {
      await saveSensitiveSettings({
        enabled: settingsEnabled.value,
        pages: flattenSensitiveTreePages(pageTree.value).map((row) => ({
          pageKey: row.pageKey,
          pageName: row.pageName,
          enabled: row.enabled,
        })),
      });
      MessagePlugin.success('配置已保存');
    } finally {
      savingSettings.value = false;
    }
  };

  return {
    loadWords,
    deleteWord,
    onWordsPageChange,
    loadSettings,
    filteredPageTree,
    saveSettingsData,
  };
};
