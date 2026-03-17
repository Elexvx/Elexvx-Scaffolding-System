<template>
  <div class="sensitive-setting">
    <t-card title="敏感词拦截" :bordered="false">
      <t-tabs v-model="activeTab">
        <t-tab-panel value="words" label="敏感词库">
          <div class="tab-container">
            <div class="table-action-bar">
              <div class="action-left">
                <t-input
                  v-model="newWord"
                  placeholder="输入敏感词后回车或点击添加"
                  clearable
                  style="width: 320px"
                  @enter="handleAddWord"
                />
                <t-button theme="primary" :loading="savingWord" @click="handleAddWord">添加</t-button>
                <t-button variant="outline" theme="default" @click="handleDownloadTemplate">
                  <template #icon><t-icon name="download" /></template>
                  下载模板
                </t-button>
                <t-button
                  theme="danger"
                  variant="outline"
                  :disabled="selectedWordIds.length === 0"
                  :loading="batchDeleting"
                  @click="handleBatchDelete"
                >
                  <template #icon><t-icon name="delete" /></template>
                  批量删除（{{ selectedWordIds.length }}）
                </t-button>
                <t-upload
                  v-model="importFiles"
                  :action="importAction"
                  :headers="uploadHeaders"
                  theme="file"
                  :auto-upload="true"
                  :use-mock-progress="true"
                  :mock-progress-duration="80"
                  :max="1"
                  accept=".xlsx,.xls,.csv,.txt"
                  @progress="handleImportProgress"
                  @success="handleImportSuccess"
                  @fail="handleImportFail"
                >
                  <t-button variant="outline">
                    <template #icon><t-icon name="upload" /></template>
                    批量导入
                  </t-button>
                </t-upload>
                <span class="import-tips">支持 Excel/CSV/TXT</span>
                <span v-if="progressPercent > 0 && progressPercent < 100" class="import-progress">
                  {{ progressPercent }}%
                </span>
              </div>
              <div class="action-right">
                <t-input
                  v-model="keyword"
                  placeholder="搜索敏感词"
                  clearable
                  style="width: 240px"
                  @enter="handleSearch"
                >
                  <template #suffixIcon>
                    <t-icon name="search" style="cursor: pointer" @click="handleSearch" />
                  </template>
                </t-input>
              </div>
            </div>

            <t-table
              row-key="id"
              :data="words"
              :columns="wordColumns"
              :selected-row-keys="selectedWordIds"
              :loading="loadingWords"
              hover
              stripe
              size="medium"
              :pagination="pagination"
              class="custom-table"
              @page-change="onPageChange"
              @select-change="handleWordSelectChange"
            >
              <template #updatedAt="{ row }">
                {{ formatTime(row.updatedAt || row.createdAt) }}
              </template>
              <template #op="{ row }">
                <t-link theme="danger" hover="color" @click="handleDeleteWord(row)">删除</t-link>
              </template>
            </t-table>
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
                  <t-switch v-model="settingsEnabled" />
                </div>
                <t-divider layout="vertical" />
                <t-input v-model="pageKeyword" placeholder="搜索页面名称/路径" clearable style="width: 280px" />
                <t-button variant="outline" @click="onExpandAllToggle">
                  {{ expandAll ? '收起全部' : '展开全部' }}
                </t-button>
              </div>
              <div class="action-right">
                <t-button theme="primary" :loading="savingSettings" @click="saveSettings">
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
              :tree="pageTreeConfig"
              :loading="loadingPages"
              hover
              stripe
              class="custom-table"
            >
              <template #enabled="{ row }">
                <t-switch v-if="row.nodeType === 'PAGE'" v-model="row.enabled" size="small" />
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
import dayjs from 'dayjs';
import type {
  EnhancedTableInstanceFunctions,
  PrimaryTableCol,
  ProgressContext,
  SuccessContext,
  UploadFile,
} from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { importExportApi } from '@/api/importExport';
import type { SensitiveImportResult, SensitivePageSetting, SensitiveWord } from '@/api/system/sensitive';
import {
  createSensitiveWord,
  deleteSensitiveWord,
  fetchSensitiveSettings,
  fetchSensitiveWords,
  saveSensitiveSettings,
} from '@/api/system/sensitive';
import { useUserStore } from '@/store';
import { request } from '@/utils/request';

type NodeType = 'DIR' | 'PAGE' | 'BTN';

interface MenuNode {
  id: number;
  nodeType: NodeType;
  path: string;
  routeName: string;
  component?: string;
  titleZhCn: string;
  titleEnUs?: string;
  hidden?: boolean;
  children?: MenuNode[];
}

interface PageRow {
  pageKey: string;
  pageName: string;
  enabled: boolean;
}

const userStore = useUserStore();
const uploadHeaders = computed(() => ({
  Authorization: userStore.token,
}));
const importAction = computed(() => importExportApi.sensitive.importWordsAction());

const route = useRoute();
const router = useRouter();
const activeTab = ref<'words' | 'pages'>((route.query.tab as 'words' | 'pages') || 'words');

watch(activeTab, (val) => {
  router.replace({ query: { ...route.query, tab: val } });
});

const newWord = ref('');
const keyword = ref('');
const words = ref<SensitiveWord[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});
const loadingWords = ref(false);
const savingWord = ref(false);
const importFiles = ref<UploadFile[]>([]);
const progressPercent = ref(0);
const selectedWordIds = ref<number[]>([]);
const batchDeleting = ref(false);

const settingsEnabled = ref(false);
const pageKeyword = ref('');
const loadingPages = ref(false);
const savingSettings = ref(false);
const expandAll = ref(false);
const expandedPageTreeNodes = ref<Array<string | number>>([]);
const pageTableRef = ref<EnhancedTableInstanceFunctions<PageTreeNode> | null>(null);
const pageTreeConfig = reactive({
  childrenKey: 'children',
  treeNodeColumnIndex: 0,
  indent: 24,
  expandTreeNodeOnClick: true,
});

const wordColumns: PrimaryTableCol[] = [
  { colKey: 'row-select', type: 'multiple', width: 48, fixed: 'left' },
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    fixed: 'left',
    cell: (_h, { rowIndex }) => String((pagination.current - 1) * pagination.pageSize + rowIndex + 1),
  },
  { colKey: 'word', title: '敏感词', ellipsis: true },
  { colKey: 'updatedAt', title: '更新时间', width: 200, cell: 'updatedAt' },
  { colKey: 'op', title: '操作', width: 100, fixed: 'right' },
];

interface PageTreeNode {
  id: number;
  nodeType: NodeType;
  titleZhCn: string;
  titleEnUs?: string;
  path: string;
  routeName: string;
  component?: string;
  fullPath: string;
  enabled?: boolean;
  children?: PageTreeNode[];
}

const pageColumns: PrimaryTableCol[] = [
  { colKey: 'titleZhCn', title: '页面名称', ellipsis: true },
  { colKey: 'fullPath', title: '页面路径', ellipsis: true, width: 300 },
  { colKey: 'enabled', title: '启用拦截', width: 100, align: 'center', cell: 'enabled' },
];

const formatTime = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const loadWords = async () => {
  loadingWords.value = true;
  try {
    const res = await fetchSensitiveWords({
      keyword: keyword.value || undefined,
      page: pagination.current - 1,
      size: pagination.pageSize,
    });
    words.value = res.list || [];
    pagination.total = res.total || 0;
  } finally {
    loadingWords.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  selectedWordIds.value = [];
  loadWords();
};

const handleAddWord = async () => {
  const value = newWord.value.trim();
  if (!value) {
    MessagePlugin.warning('请输入敏感词');
    return;
  }
  savingWord.value = true;
  try {
    await createSensitiveWord(value);
    newWord.value = '';
    MessagePlugin.success('添加成功');
    pagination.current = 1;
    await loadWords();
  } finally {
    savingWord.value = false;
  }
};

const handleDeleteWord = async (row: SensitiveWord) => {
  const dialog = DialogPlugin.confirm({
    header: '确认删除',
    body: `确定删除敏感词 “${row.word}” 吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    onConfirm: async () => {
      dialog.hide();
      await deleteSensitiveWord(row.id);
      MessagePlugin.success('删除成功');
      selectedWordIds.value = selectedWordIds.value.filter((id) => id !== row.id);
      loadWords();
    },
  });
};

const handleWordSelectChange = (keys: Array<string | number>) => {
  selectedWordIds.value = keys.map((k) => Number(k)).filter((k) => Number.isFinite(k));
};

const handleBatchDelete = () => {
  const ids = [...new Set(selectedWordIds.value)];
  if (!ids.length) return;

  const dialog = DialogPlugin.confirm({
    header: '确认批量删除',
    body: `确定删除已选 ${ids.length} 个敏感词吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    onConfirm: async () => {
      dialog.hide();
      batchDeleting.value = true;
      try {
        const results = await Promise.allSettled(ids.map((id) => deleteSensitiveWord(id)));
        const failedCount = results.filter((r) => r.status === 'rejected').length;
        const successCount = ids.length - failedCount;

        if (failedCount > 0) {
          MessagePlugin.warning(`已删除 ${successCount} 个，失败 ${failedCount} 个`);
        } else {
          MessagePlugin.success(`已删除 ${successCount} 个`);
        }
      } finally {
        batchDeleting.value = false;
        selectedWordIds.value = [];
        loadWords();
      }
    },
  });
};

const handleDownloadTemplate = async () => {
  try {
    const res = await importExportApi.sensitive.downloadTemplate();
    await importExportApi.utils.downloadBlobResponse(res as any, 'sensitive_words_template.xlsx');
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '模板下载失败'));
  }
};

const handleImportProgress = (ctx: ProgressContext) => {
  if (ctx?.currentFiles) {
    importFiles.value = ctx.currentFiles;
  }
  const rawPercent = (ctx as any)?.percent ?? ctx?.currentFiles?.[0]?.percent ?? 0;
  progressPercent.value = Math.max(0, Math.min(100, Math.round(rawPercent)));
};

const handleImportSuccess = (context: SuccessContext) => {
  const response = (context?.response || {}) as any;
  const result: SensitiveImportResult | undefined = response?.data || response?.result || response?.data?.data;
  if (response?.code !== undefined && response.code !== 0) {
    MessagePlugin.error(response?.message || '导入失败');
    importFiles.value = [];
    progressPercent.value = 0;
    return;
  }
  if (!result) {
    MessagePlugin.warning('导入完成，但未获取到统计结果');
    importFiles.value = [];
    progressPercent.value = 0;
    loadWords();
    return;
  }

  const summary = `导入完成：读取 ${result.total} 条，成功 ${result.imported} 条，跳过 ${result.skipped} 条，失败 ${result.failed} 条`;
  MessagePlugin.success(summary);

  if (result.errors && result.errors.length > 0) {
    DialogPlugin.alert({
      header: '导入异常提示',
      body: result.errors.join('\n'),
    });
  }
  importFiles.value = [];
  progressPercent.value = 0;
  loadWords();
};

const handleImportFail = () => {
  MessagePlugin.error('导入失败，请检查文件格式');
  importFiles.value = [];
  progressPercent.value = 0;
};

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  loadWords();
};

const loadSettings = async () => {
  loadingPages.value = true;
  try {
    const [settings, menuTree] = await Promise.all([
      fetchSensitiveSettings(),
      request.get<MenuNode[]>({ url: '/system/menu/tree' }),
    ]);

    settingsEnabled.value = settings.enabled;
    const map = new Map<string, SensitivePageSetting>();
    (settings.pages || []).forEach((p) => map.set(p.pageKey, p));
    const tree = buildPageTree(menuTree || [], map);
    const known = new Set<string>();
    collectPageKeys(tree).forEach((k) => known.add(k));
    const extras = (settings.pages || []).filter((p) => !known.has(p.pageKey));
    if (extras.length) {
      let seed = -1;
      tree.push({
        id: seed--,
        nodeType: 'DIR',
        titleZhCn: '未在菜单中的页面',
        path: '',
        routeName: '',
        fullPath: '',
        children: extras.map((p) => ({
          id: seed--,
          nodeType: 'PAGE',
          titleZhCn: p.pageName || p.pageKey,
          path: '',
          routeName: '',
          fullPath: p.pageKey,
          enabled: p.enabled ?? false,
        })),
      });
    }
    pageTree.value = tree;
  } finally {
    loadingPages.value = false;
  }
};

const pageTree = ref<PageTreeNode[]>([]);

const resolvePagePath = (parentPath: string, nodePath: string) => {
  const segment = String(nodePath || '').trim();
  if (!segment) return String(parentPath || '').trim();
  if (segment.startsWith('/')) return segment;
  const base = String(parentPath || '').trim();
  if (!base || base === '/') return `/${segment}`;
  const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base;
  return `${normalizedBase}/${segment}`;
};

const buildPageTree = (
  nodes: MenuNode[],
  enabledMap: Map<string, SensitivePageSetting>,
  parentPath = '',
): PageTreeNode[] => {
  const out: PageTreeNode[] = [];
  (nodes || []).forEach((node) => {
    // 跳过已禁用的菜单项
    if (node.hidden) return;

    const isBtn = node.nodeType === 'BTN';
    const nextPath = isBtn ? parentPath : resolvePagePath(parentPath, node.path);
    const children = node.children?.length ? buildPageTree(node.children, enabledMap, nextPath) : [];
    const isDir = node.nodeType === 'DIR';
    const isPage = node.nodeType === 'PAGE' && node.component && node.component !== 'IFRAME';
    if (isDir) {
      if (children.length) {
        out.push({
          id: node.id,
          nodeType: node.nodeType,
          titleZhCn: node.titleZhCn,
          titleEnUs: node.titleEnUs,
          path: node.path,
          routeName: node.routeName,
          component: node.component,
          fullPath: nextPath,
          children,
        });
      }
      return;
    }
    if (isPage) {
      out.push({
        id: node.id,
        nodeType: node.nodeType,
        titleZhCn: node.titleZhCn,
        titleEnUs: node.titleEnUs,
        path: node.path,
        routeName: node.routeName,
        component: node.component,
        fullPath: nextPath,
        enabled: enabledMap.get(nextPath)?.enabled ?? false,
        children: children.length ? children : undefined,
      });
      return;
    }
    out.push(...children);
  });
  return out;
};

const collectPageKeys = (nodes: PageTreeNode[]) => {
  const keys: string[] = [];
  const walk = (list: PageTreeNode[]) => {
    (list || []).forEach((n) => {
      if (n.nodeType === 'PAGE' && n.fullPath) keys.push(n.fullPath);
      if (n.children?.length) walk(n.children);
    });
  };
  walk(nodes);
  return keys;
};

const filterPageTree = (nodes: PageTreeNode[], keywordRaw: string): PageTreeNode[] => {
  const kw = keywordRaw.trim().toLowerCase();
  if (!kw) return nodes;
  const hit = (n: PageTreeNode) =>
    (n.titleZhCn || '').toLowerCase().includes(kw) || (n.fullPath || '').toLowerCase().includes(kw);
  const walk = (list: PageTreeNode[]): PageTreeNode[] => {
    const out: PageTreeNode[] = [];
    for (const n of list) {
      const children = n.children?.length ? walk(n.children) : [];
      if (hit(n) || children.length) out.push({ ...n, children: children.length ? children : undefined });
    }
    return out;
  };
  return walk(nodes);
};

const filteredPageTree = computed(() => filterPageTree(pageTree.value, pageKeyword.value));

const onExpandAllToggle = () => {
  expandAll.value = !expandAll.value;
  expandAll.value ? pageTableRef.value?.expandAll() : pageTableRef.value?.foldAll();
};

const flattenTreePages = (nodes: PageTreeNode[]) => {
  const list: PageRow[] = [];
  const walk = (arr: PageTreeNode[]) => {
    (arr || []).forEach((n) => {
      if (n.nodeType === 'PAGE' && n.fullPath) {
        list.push({
          pageKey: n.fullPath,
          pageName: n.titleZhCn || n.fullPath,
          enabled: !!n.enabled,
        });
      }
      if (n.children?.length) walk(n.children);
    });
  };
  walk(nodes);
  return list;
};

const saveSettings = async () => {
  savingSettings.value = true;
  try {
    await saveSensitiveSettings({
      enabled: settingsEnabled.value,
      pages: flattenTreePages(pageTree.value).map((row) => ({
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

onMounted(() => {
  loadWords();
  loadSettings();
});
</script>
<style lang="less" scoped>
.sensitive-setting {
  :deep(.t-card__body) {
    padding: 8px 24px 24px;
  }

  .tab-container {
    padding-top: 24px;
  }

  .table-action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .action-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .import-tips {
        font-size: 12px;
        color: var(--td-text-color-placeholder);
      }

      .import-progress {
        font-size: 12px;
        font-weight: 500;
        color: var(--td-brand-color);
      }

      .setting-switch {
        display: flex;
        align-items: center;
        gap: 12px;

        .label {
          font-weight: 500;
          color: var(--td-text-color-primary);
        }
      }
    }

    .action-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .custom-table {
    :deep(.t-table__header) {
      background-color: var(--td-bg-color-secondarycontainer);
    }
  }

  @media (max-width: @screen-sm-max) {
    .table-action-bar {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;

      .action-left {
        flex-wrap: wrap;
        gap: 12px;

        :deep(.t-input),
        :deep(.t-upload),
        :deep(.t-divider) {
          width: 100% !important;
        }

        :deep(.t-divider) {
          height: 1px;
        }

        .import-tips,
        .import-progress {
          width: 100%;
        }
      }

      .action-right {
        justify-content: flex-start;
      }
    }
  }
}
</style>
