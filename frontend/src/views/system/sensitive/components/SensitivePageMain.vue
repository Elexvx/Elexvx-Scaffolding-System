<template>
  <div class="sensitive-setting">
    <t-card title="敏感词拦截" :bordered="false">
      <t-tabs v-model="activeTab">
        <t-tab-panel value="words" label="敏感词库">
          <div class="tab-container">
            <sensitive-toolbar
              v-model:word-create-form="wordCreateForm"
              v-model:word-search-form="wordSearchForm"
              v-model:import-files="importFiles"
              :saving-word="savingWord"
              :import-action="importAction"
              :upload-headers="uploadHeaders"
              :selected-count="selectedWordIds.length"
              :batch-deleting="batchDeleting"
              :progress-percent="progressPercent"
              @add-word="handleAddWord"
              @search="handleSearch"
              @download-template="handleDownloadTemplate"
              @batch-delete="handleBatchDelete"
              @import-progress="handleImportProgress"
              @import-success="handleImportSuccess"
              @import-fail="handleImportFail"
            />

            <sensitive-table
              :data="words"
              :columns="wordColumns"
              :selected-word-ids="selectedWordIds"
              :loading="loadingWords"
              :pagination="pagination"
              @page-change="onWordsPageChange"
              @select-change="onSelectChange"
              @delete-row="deleteWord"
            />
          </div>
        </t-tab-panel>

        <t-tab-panel value="pages" label="拦截设置">
          <div class="tab-container">
            <t-alert
              theme="info"
              :close="false"
              message="敏感词拦截可按页面启用，密码/邮箱/手机号/身份证字段不会参与校验。"
              style="margin-bottom: 24px"
            />

            <div class="table-action-bar">
              <div class="action-left">
                <div class="setting-switch">
                  <span class="label">全局拦截状态</span>
                  <t-switch v-model="settingsEnabled" :disabled="!canUpdate" />
                </div>
                <t-divider layout="vertical" />
                <t-input v-model="pageKeyword" placeholder="搜索页面名称/路径" clearable style="width: 280px" />
                <t-button variant="outline" @click="onExpandAllToggle">{{ expandAll ? '收起全部' : '展开全部' }}</t-button>
              </div>
              <div class="action-right">
                <t-button theme="primary" :loading="savingSettings" :disabled="!canUpdate" @click="saveSettingsData">
                  <template #icon><t-icon name="save" /></template>
                  保存配置
                </t-button>
              </div>
            </div>

            <t-enhanced-table
              ref="pageTableRef"
              v-model:expanded-tree-nodes="expandedPageTreeNodes"
              row-key="id"
              :data="filteredPageTree"
              :columns="pageColumns"
              :tree="sensitiveTreeConfig"
              :loading="loadingPages"
              hover
              stripe
              class="custom-table"
            >
              <template #enabled="{ row }">
                <t-switch v-if="row.nodeType === 'PAGE'" v-model="row.enabled" :disabled="!canUpdate" size="small" />
                <span v-else>--</span>
              </template>
            </t-enhanced-table>
          </div>
        </t-tab-panel>
      </t-tabs>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';

import { importExportApi } from '@/api/importExport';
import { useUserStore } from '@/store';

import { sensitiveTreeConfig } from '../constants/sensitiveOptions';
import { useSensitiveBatchActions } from '../hooks/useSensitiveBatchActions';
import { useSensitiveColumns } from '../hooks/useSensitiveColumns';
import { useSensitiveForm } from '../hooks/useSensitiveForm';
import { useSensitivePageState } from '../hooks/useSensitivePageState';
import { useSensitivePermissions } from '../hooks/useSensitivePermissions';
import { useSensitiveSearchForm } from '../hooks/useSensitiveSearchForm';
import { useSensitiveTable } from '../hooks/useSensitiveTable';
import SensitiveTable from './SensitiveTable.vue';
import SensitiveToolbar from './SensitiveToolbar.vue';

const userStore = useUserStore();
const uploadHeaders = computed(() => ({
  Authorization: userStore.token,
}));
const importAction = computed(() => importExportApi.sensitive.importWordsAction());
const { canQuery, canUpdate } = useSensitivePermissions();
const { activeTab, wordSearchForm, wordCreateForm, pageKeyword } = useSensitiveSearchForm();
const {
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
} = useSensitivePageState();
const { wordColumns, pageColumns } = useSensitiveColumns(pagination);
const { loadWords, deleteWord, onWordsPageChange, loadSettings, filteredPageTree, saveSettingsData } = useSensitiveTable({
  wordKeyword: computed(() => wordSearchForm.keyword),
  pagination,
  words,
  loadingWords,
  selectedWordIds,
  pageTree,
  pageKeyword,
  loadingPages,
  settingsEnabled,
  savingSettings,
});
const resetWordSelection = () => {
  pagination.current = 1;
  selectedWordIds.value = [];
};
const { handleAddWord, handleDownloadTemplate, handleImportProgress, handleImportSuccess, handleImportFail } = useSensitiveForm({
  wordCreateForm,
  importFiles,
  progressPercent,
  savingWord,
  reloadWords: loadWords,
  resetWordSelection,
});
const { onSelectChange, handleBatchDelete } = useSensitiveBatchActions(selectedWordIds, batchDeleting, loadWords);

const handleSearch = async () => {
  pagination.current = 1;
  selectedWordIds.value = [];
  await loadWords();
};

const onExpandAllToggle = () => {
  expandAll.value = !expandAll.value;
  if (expandAll.value) {
    pageTableRef.value?.expandAll();
  } else {
    pageTableRef.value?.foldAll();
  }
};

onMounted(async () => {
  if (!canQuery.value) return;
  await Promise.all([loadWords(), loadSettings()]);
});
</script>
<style lang="less" scoped src="./SensitivePageMain.less"></style>
