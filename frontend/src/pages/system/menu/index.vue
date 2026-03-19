<template>
  <t-card title="目录/页面管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space class="menu-toolbar">
        <t-input v-model="keyword" clearable placeholder="按名称/路由搜索" style="width: 260px" />
        <t-button v-perm="'system:SystemMenu:create'" theme="primary" @click="openCreateRoot">添加根节点</t-button>
        <t-button v-perm:disable="'system:SystemMenu:query'" variant="outline" @click="reload">重置/更新数据</t-button>
        <t-button variant="outline" @click="onExpandAllToggle">{{ expandAll ? '收起全部' : '展开全部' }}</t-button>
        <t-button
          v-perm:disable="'system:SystemMenu:update'"
          theme="primary"
          :disabled="!dirty || !canUpdate"
          :loading="savingOrder"
          @click="saveOrder"
          >保存排序</t-button
        >
      </t-space>

      <div class="menu-table-wrapper">
        <t-enhanced-table
          ref="tableRef"
          v-model:expanded-tree-nodes="expandedTreeNodes"
          row-key="id"
          drag-sort="row-handler"
          :data="filteredData"
          :columns="columns"
          :tree="treeConfig"
          :loading="loading"
          :before-drag-sort="beforeDragSort"
          @abnormal-drag-sort="onAbnormalDragSort"
          @drag-sort="onDragSort"
        />
      </div>
    </t-space>

    <confirm-drawer v-model:visible="drawerVisible" :header="drawerTitle" size="760px">
      <t-form
        ref="formRef"
        :data="form"
        label-align="right"
        label-width="120px"
        layout="vertical"
        class="menu-drawer-form drawer-form--single"
      >
        <t-form-item label="上级菜单" name="parentId">
          <t-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :keys="treeKeys"
            clearable
            filterable
            placeholder="根节点请留空"
            :popup-props="{ attach: 'body' }"
            @change="onParentIdChange"
            @clear="onParentIdClear"
          />
        </t-form-item>

        <t-form-item label="菜单类型" name="nodeType">
          <t-radio-group v-model="form.nodeType">
            <t-radio v-for="opt in nodeTypeRadioOptions" :key="String(opt.value)" :value="opt.value">
              {{ opt.label }}
            </t-radio>
          </t-radio-group>
        </t-form-item>

        <t-form-item label="菜单图标" name="icon">
          <t-select
            v-model="form.icon"
            :options="iconOptions"
            clearable
            filterable
            placeholder="点击选择图标"
            :popup-props="{ attach: 'body', zIndex: 3000, overlayClassName: 'menu-icon-select__popup' }"
            :value-display="renderIconValue"
          />
        </t-form-item>

        <t-form-item label="显示排序" name="orderNo">
          <t-input-number v-model="form.orderNo" :min="0" theme="column" style="width: 100%" />
        </t-form-item>

        <t-form-item label="菜单名称" name="titleZhCn" required>
          <t-input v-model="form.titleZhCn" placeholder="请输入菜单名称" />
        </t-form-item>

        <t-form-item v-if="form.nodeType === 'PAGE'" label="是否外链">
          <t-radio-group v-model="isExternalLink">
            <t-radio :value="true">是</t-radio>
            <t-radio :value="false">否</t-radio>
          </t-radio-group>
        </t-form-item>

        <t-form-item
          label="路由地址"
          name="path"
          required
          :help="
            form.nodeType === 'PAGE'
              ? '访问的路由地址，如：`user`，外链如 `https://...` '
              : '访问的路由地址，如：`user` '
          "
        >
          <t-input v-if="!isExternalLink" v-model="form.path" placeholder="请输入路由地址" />
          <t-input v-else v-model="form.frameSrc" placeholder="请输入外链地址" />
        </t-form-item>

        <t-form-item v-if="form.nodeType === 'PAGE'" label="组件路径" name="component" required>
          <t-select
            v-model="form.component"
            :options="pageComponentOptions"
            filterable
            creatable
            placeholder="选择组件（如 /system/user/index）或直接输入"
            :popup-props="{ attach: 'body' }"
          />
        </t-form-item>

        <t-form-item label="显示状态">
          <t-radio-group :model-value="!form.hidden" @update:model-value="(v) => (form.hidden = !v)">
            <t-radio :value="true">显示</t-radio>
            <t-radio :value="false">隐藏</t-radio>
          </t-radio-group>
        </t-form-item>
      </t-form>

      <template #footer>
        <t-space>
          <t-button variant="outline" @click="drawerVisible = false">取消</t-button>
          <t-button theme="primary" :loading="savingNode" @click="submitNode">确定</t-button>
        </t-space>
      </template>
    </confirm-drawer>
  </t-card>
</template>
<script setup lang="tsx">
import { manifest as iconManifest, MoveIcon } from 'tdesign-icons-vue-next';
import type { EnhancedTableInstanceFunctions, SelectOption } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useDictionary } from '@/hooks/useDictionary';
import {
  EXPANDED_STORAGE_KEY,
  LEGACY_EXPANDED_STORAGE_KEY,
  buildPageComponentOptions,
  buildReorderItems,
  normalizeParentId,
  persistExpanded,
  readExpandedFromStorage as readExpandedStateFromStorage,
  sanitizeExpanded,
  toRouteName,
  toSlug,
  treeFilter,
  type MenuNode,
  type NodeType,
} from '@/pages/system/menu/helpers';
import { getPermissionStore, useUserStore } from '@/store';
import { buildDictOptions, resolveLabel } from '@/utils/dict';
import { hasPerm } from '@/utils/permission';
import { request } from '@/utils/request';
import { migrateLocalStorageKey } from '@/utils/storage/compat';

type OpenType = 'internal' | 'iframe' | 'external';

interface RoleRow {
  id: number;
  name: string;
  permissions?: string[];
}

const tableRef = ref<EnhancedTableInstanceFunctions<MenuNode> | null>(null);

const treeConfig = reactive({
  childrenKey: 'children',
  treeNodeColumnIndex: 1,
  indent: 24,
  expandTreeNodeOnClick: true,
});

const treeKeys = { value: 'id', label: 'titleZhCn', children: 'children' };

const keyword = ref('');
const loading = ref(false);
const savingOrder = ref(false);
const savingNode = ref(false);
const dirty = ref(false);
const canQuery = computed(() => hasPerm('system:SystemMenu:query'));
const canCreate = computed(() => hasPerm('system:SystemMenu:create'));
const canUpdate = computed(() => hasPerm('system:SystemMenu:update'));
const canDelete = computed(() => hasPerm('system:SystemMenu:delete'));

const nodeTypeDict = useDictionary('menu_node_type');
const actionDict = useDictionary('menu_action');

const fallbackNodeTypeOptions = [
  { label: '目录', value: 'DIR' },
  { label: '页面', value: 'PAGE' },
  { label: '按钮', value: 'BTN' },
];
const nodeTypeLabelMap: Record<string, string> = {
  DIR: '目录',
  PAGE: '页面',
  BTN: '按钮',
};
const NODE_TYPE_VALUES = ['DIR', 'PAGE', 'BTN'];
const nodeTypeOptions = computed(() =>
  buildDictOptions(nodeTypeDict.items.value, fallbackNodeTypeOptions, NODE_TYPE_VALUES),
);
const nodeTypeRadioOptions = computed(
  () =>
    nodeTypeOptions.value.filter((opt) => opt && typeof opt === 'object' && 'value' in opt && 'label' in opt) as Array<{
      label: string;
      value: string | number | boolean;
      disabled?: boolean;
    }>,
);

const fallbackActionOptions = [
  { label: '查询 (Query)', value: 'query' },
  { label: '新增 (Create)', value: 'create' },
  { label: '修改 (Update)', value: 'update' },
  { label: '删除 (Delete)', value: 'delete' },
];
const ACTION_VALUES = ['query', 'create', 'update', 'delete'];
const _actionOptions = computed(() => buildDictOptions(actionDict.items.value, fallbackActionOptions, ACTION_VALUES));

const permissionStore = getPermissionStore();
const userStore = useUserStore();
let refreshingSidebar = false;
let refreshSidebarTimer: number | null = null;
const refreshSidebarNow = async () => {
  if (refreshingSidebar) return;
  refreshingSidebar = true;
  try {
    await permissionStore.refreshAsyncRoutes(userStore.userInfo);
  } catch (e: any) {
    MessagePlugin.warning(String(e?.message || '导航栏刷新失败'));
  } finally {
    refreshingSidebar = false;
  }
};
const scheduleRefreshSidebar = () => {
  if (refreshSidebarTimer) window.clearTimeout(refreshSidebarTimer);
  refreshSidebarTimer = window.setTimeout(() => {
    refreshSidebarTimer = null;
    void refreshSidebarNow();
  }, 400);
};

const data = ref<MenuNode[]>([]);
const expandedTreeNodes = ref<Array<string | number>>([]);
const expandAll = ref(false);

watch(
  expandedTreeNodes,
  (value) => {
    persistExpanded(value);
  },
  { deep: true },
);

const loadExpandedFromStorage = () => {
  return readExpandedStateFromStorage(() =>
    migrateLocalStorageKey(EXPANDED_STORAGE_KEY, [LEGACY_EXPANDED_STORAGE_KEY]),
  );
};

const roles = ref<RoleRow[]>([]);
const _roleOptions = computed<SelectOption[]>(() => roles.value.map((r) => ({ label: r.name, value: r.name })));
const _permissionOptions = computed<SelectOption[]>(() => {
  const set = new Set<string>();
  roles.value.forEach((r) => (r.permissions || []).forEach((p) => p && set.add(p)));
  return Array.from(set)
    .sort()
    .map((p) => ({ label: p, value: p }));
});

const iconOptions = computed<SelectOption[]>(() => {
  const list = Array.isArray(iconManifest) ? iconManifest : [];
  return list
    .map((item: any) => item?.stem)
    .filter((v: unknown): v is string => typeof v === 'string' && v.length > 0)
    .sort((a, b) => a.localeCompare(b))
    .map((name) => ({
      label: name,
      value: name,
      title: name,
      content: () => (
        <span class="menu-icon-option">
          <t-icon name={name} />
          <span class="menu-icon-option__label">{name}</span>
        </span>
      ),
    }));
});

const renderIconValue = (_h: any, ctx: { value?: unknown }) => {
  const name = Array.isArray(ctx?.value) ? ctx?.value[0] : ctx?.value;
  if (!name) return null;
  const label = String(name);
  return (
    <span class="menu-icon-value" title={label}>
      <t-icon name={label} />
      <span class="menu-icon-option__label">{label}</span>
    </span>
  );
};

const pageComponentOptions = computed<SelectOption[]>(() => buildPageComponentOptions());

const _layoutOptions: SelectOption[] = [
  { label: 'LAYOUT（标准布局）', value: 'LAYOUT' },
  { label: 'BLANK（空白布局）', value: 'BLANK' },
];

const findById = (id: number) => {
  const stack = [...data.value];
  while (stack.length) {
    const n = stack.shift();
    if (!n) continue;
    if (n.id === id) return n;
    if (n.children?.length) stack.unshift(...n.children);
  }
  return null;
};

const computeFullPath = (node: MenuNode) => {
  const segments: string[] = [];
  let cur: MenuNode | null = node;
  const visited = new Set<number>();
  while (cur && !visited.has(cur.id)) {
    visited.add(cur.id);
    segments.unshift(cur.path || '');
    cur = cur.parentId ? findById(cur.parentId) : null;
  }
  const path = segments.join('/').replace(/\/+/g, '/');
  return path.startsWith('/') ? path : `/${path}`;
};

const getOpenTypeLabel = (row: MenuNode) => {
  if (row.nodeType === 'DIR') return '目录';
  if (row.nodeType === 'BTN') return '按钮';
  if (row.frameSrc) return row.frameBlank ? '外链' : '内嵌';
  return '内部';
};

const filteredData = computed(() => treeFilter(data.value, keyword.value));

const dirTreeData = computed<MenuNode[]>(() => {
  const walk = (nodes: MenuNode[]): MenuNode[] => {
    return (nodes || [])
      .filter((n) => n.nodeType === 'DIR')
      .map((n) => ({
        ...n,
        children: n.children?.length ? walk(n.children) : [],
      }));
  };
  return walk(data.value);
});

const allParentTree = computed<MenuNode[]>(() => {
  const walk = (nodes: MenuNode[]): MenuNode[] => {
    return (nodes || [])
      .filter((n) => n.nodeType === 'DIR' || n.nodeType === 'PAGE')
      .map((n) => ({
        ...n,
        children: n.children?.length ? walk(n.children) : [],
      }));
  };
  return walk(data.value);
});

const parentOptions = computed(() => {
  if (form.nodeType === 'BTN') return allParentTree.value;
  return dirTreeData.value;
});

const reload = async () => {
  if (!canQuery.value) {
    data.value = [];
    expandedTreeNodes.value = [];
    dirty.value = false;
    return;
  }
  loading.value = true;
  try {
    data.value = await request.get<MenuNode[]>({ url: '/system/menu/tree' });
    const storedExpanded = loadExpandedFromStorage();
    expandedTreeNodes.value = sanitizeExpanded(storedExpanded, data.value);
    expandAll.value = false;
    dirty.value = false;
  } finally {
    loading.value = false;
  }
};

const onExpandAllToggle = () => {
  expandAll.value = !expandAll.value;
  expandAll.value ? tableRef.value?.expandAll() : tableRef.value?.foldAll();
};

const saveOrder = async () => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  const tree = tableRef.value?.getTreeNode?.() || data.value;
  savingOrder.value = true;
  try {
    await request.put({ url: '/system/menu/reorder', data: { items: buildReorderItems(tree) } });
    MessagePlugin.success('排序已保存');
    await reload();
    scheduleRefreshSidebar();
  } finally {
    savingOrder.value = false;
  }
};

const onDragSort = (ctx: any) => {
  dirty.value = true;
  if (ctx?.newData) {
    // newData 可能为扁平数据，统一以 getTreeNode() 为准
    const tree = tableRef.value?.getTreeNode?.();
    if (tree) data.value = tree;
  }
};

const onAbnormalDragSort = (params: any) => {
  if (params?.code === 1001) {
    MessagePlugin.warning('不同层级的元素，不允许直接拖拽排序');
    return;
  }
  if (params?.reason) MessagePlugin.warning(String(params.reason));
};

const nextOrderNo = (parentId: number) => {
  const parent = findById(parentId);
  const siblings = parent?.children || [];
  let max = -1;
  for (const s of siblings) max = Math.max(max, s.orderNo == null ? 0 : s.orderNo);
  return max + 1;
};

const movePageToDir = async (page: MenuNode, destDirId: number) => {
  const dest = findById(destDirId);
  if (!dest || dest.nodeType !== 'DIR') {
    MessagePlugin.warning('目标父节点必须为目录');
    return;
  }
  await request.put({
    url: `/system/menu/${page.id}`,
    data: {
      version: page.version,
      parentId: destDirId,
      orderNo: nextOrderNo(destDirId),
    },
  });
  MessagePlugin.success('已移动');
  await reload();
  scheduleRefreshSidebar();
};

const openMoveDialog = (row: MenuNode) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  if (row.nodeType !== 'PAGE') {
    MessagePlugin.warning('当前仅支持移动页面节点');
    return;
  }
  if (row.parentId == null) {
    MessagePlugin.warning('根节点移动需要同时调整 path（根节点需以 / 开头），请使用编辑完成');
    return;
  }

  const selected = ref<number | null>(row.parentId);
  const dialog = DialogPlugin.confirm({
    header: `移动页面：${row.titleZhCn}`,
    confirmBtn: '移动',
    cancelBtn: '取消',
    closeOnOverlayClick: false,
    body: () => (
      <t-space direction="vertical" style={{ width: '100%' }}>
        <div style={{ color: 'var(--td-text-color-secondary)' }}>选择要移动到的目录</div>
        <t-tree-select
          data={dirTreeData.value}
          keys={treeKeys}
          filterable
          clearable={false}
          placeholder="请选择目录"
          modelValue={selected.value}
          onChange={(v: any) => {
            selected.value = v == null ? null : Number(v);
          }}
        />
        <div style={{ color: 'var(--td-text-color-secondary)', fontSize: '12px' }}>
          提示：拖拽跨目录时也会弹出此确认框
        </div>
      </t-space>
    ),
    onConfirm: async () => {
      const destDirId = selected.value;
      if (!destDirId || destDirId === row.parentId) {
        dialog.hide();
        MessagePlugin.success('未变更');
        return;
      }
      dialog.hide();
      await movePageToDir(row, destDirId);
    },
  });
};

const beforeDragSort = (ctx: any) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return false;
  }
  const current = ctx?.current as MenuNode | undefined;
  const target = ctx?.target as MenuNode | undefined;
  if (!current || !target) return true;

  // 搜索展示为子树裁剪，不建议在此状态下拖拽，避免误操作
  if (keyword.value.trim()) {
    MessagePlugin.warning('请先清空搜索条件，再进行拖拽排序/移动');
    return false;
  }

  // 同一父节点：正常排序
  if (current.parentId === target.parentId) return true;

  // 跨目录：拦截默认逻辑（默认只允许同层级交换），改为“移动页面到目录”
  if (current.nodeType !== 'PAGE') {
    MessagePlugin.warning('跨目录移动请使用“移动”操作（当前仅支持移动页面节点）');
    return false;
  }
  if (current.parentId == null) {
    MessagePlugin.warning('根节点移动需要同时调整 path（根节点需以 / 开头），请使用编辑完成');
    return false;
  }

  const destDirId = target.nodeType === 'DIR' ? target.id : target.parentId;
  if (destDirId == null) {
    MessagePlugin.warning('不支持拖拽移动到根节点，请使用编辑完成');
    return false;
  }

  const dialog = DialogPlugin.confirm({
    header: '确认移动',
    body: `将「${current.titleZhCn}」移动到「${findById(destDirId)?.titleZhCn || '目标目录'}」下？`,
    confirmBtn: '移动',
    cancelBtn: '取消',
    theme: 'warning',
    onConfirm: async () => {
      dialog.hide();
      await movePageToDir(current, destDirId);
    },
    onClose: () => dialog.hide(),
  });
  return false;
};

const rowSaving = ref<Record<number, boolean>>({});

const updateTreeData = (list: MenuNode[], id: number, patch: Partial<MenuNode>) => {
  for (let i = 0; i < list.length; i++) {
    const item = list[i];
    if (item.id === id) {
      const children = item.children;
      Object.assign(item, patch);
      if (children && patch.children == null) item.children = children;
      return true;
    }
    if (item.children?.length) {
      if (updateTreeData(item.children, id, patch)) return true;
    }
  }
  return false;
};

const updateRow = async (row: MenuNode, patch: Partial<MenuNode>) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  rowSaving.value[row.id] = true;
  try {
    if (typeof patch.hidden === 'boolean') {
      patch.enabled = !patch.hidden;
    }
    const res = await request.put<MenuNode>({
      url: `/system/menu/${row.id}`,
      data: {
        version: row.version,
        ...patch,
      },
    });
    updateTreeData(data.value, row.id, res);
    scheduleRefreshSidebar();
  } finally {
    rowSaving.value[row.id] = false;
  }
};

const columns = computed(() => {
  return [
    {
      colKey: 'drag',
      title: '排序',
      width: 46,
      className: 't-table__drag-col',
      cell: () => <MoveIcon class="t-table__handle-draggable menu-table__drag-handle" />,
    },
    {
      colKey: 'titleZhCn',
      title: '名称',
      width: 240,
      ellipsis: true,
      cell: (_h: any, { row }: any) => {
        const r = row as MenuNode;
        return (
          <t-space size="small" align="center">
            {r.icon ? <t-icon name={r.icon} /> : null}
            <span style={{ fontWeight: 500 }}>{r.titleZhCn}</span>
            <t-tag
              theme={r.nodeType === 'DIR' ? 'default' : r.nodeType === 'BTN' ? 'warning' : 'primary'}
              variant="light"
            >
              {resolveLabel(r.nodeType, nodeTypeDict.items.value, nodeTypeLabelMap)}
            </t-tag>
            {r.hidden ? (
              <t-tag theme="warning" variant="light">
                隐藏
              </t-tag>
            ) : null}
          </t-space>
        );
      },
    },
    {
      colKey: 'fullPath',
      title: '路由Path',
      width: 200,
      ellipsis: true,
      cell: (_h: any, { row }: any) => <span style={{ fontFamily: 'monospace' }}>{computeFullPath(row)}</span>,
    },
    {
      colKey: 'component',
      title: '打开/组件/链接',
      width: 220,
      ellipsis: true,
      cell: (_h: any, { row }: any) => {
        const r = row as MenuNode;
        const label = getOpenTypeLabel(r);
        const content =
          r.nodeType === 'PAGE' ? (r.frameSrc ? r.frameSrc : r.component || '-') : r.component || 'LAYOUT';
        return (
          <t-space size="small">
            <t-tag theme="default" variant="light">
              {label}
            </t-tag>
            <span style={{ fontFamily: 'monospace' }}>{content}</span>
          </t-space>
        );
      },
    },
    {
      colKey: 'hidden',
      title: '隐藏',
      width: 90,
      cell: (_h: any, { row }: any) => {
        const r = row as MenuNode;
        return (
          <t-switch
            size="small"
            disabled={!canUpdate.value || !!rowSaving.value[r.id]}
            modelValue={!!r.hidden}
            onChange={(v: any) => updateRow(r, { hidden: !!v })}
          />
        );
      },
    },
    {
      colKey: 'operate',
      title: '操作',
      width: 280,
      className: 'menu-table__operate-cell',
      cell: (_h: any, { row }: any) => {
        const r = row as MenuNode;
        return (
          <div class="menu-table__operate-links">
            {canCreate.value ? (
              <t-link theme="primary" disabled={r.nodeType !== 'DIR'} onClick={() => openCreateChild(r, 'DIR')}>
                +目录
              </t-link>
            ) : null}
            {canCreate.value ? (
              <t-link theme="primary" disabled={r.nodeType !== 'DIR'} onClick={() => openCreateChild(r, 'PAGE')}>
                +页面
              </t-link>
            ) : null}
            {canCreate.value ? (
              <t-link
                theme="primary"
                disabled={r.nodeType !== 'DIR' && r.nodeType !== 'PAGE'}
                onClick={() => openCreateChild(r, 'BTN')}
              >
                +按钮
              </t-link>
            ) : null}
            {canUpdate.value ? (
              <t-link theme="primary" onClick={() => openEdit(r)}>
                编辑
              </t-link>
            ) : null}
            {canUpdate.value ? (
              <t-link
                theme="primary"
                disabled={r.nodeType !== 'PAGE' || r.parentId == null}
                onClick={() => openMoveDialog(r)}
              >
                移动
              </t-link>
            ) : null}
            {canDelete.value ? (
              <t-link theme="danger" onClick={() => removeNode(r)}>
                删除
              </t-link>
            ) : null}
          </div>
        );
      },
    },
  ] as any;
});

type Mode = 'create' | 'edit';
const mode = ref<Mode>('create');
const drawerVisible = ref(false);
const drawerTitle = computed(() => (mode.value === 'create' ? '添加菜单' : '编辑菜单'));

const formRef = ref<any>();
const form = reactive({
  id: null as number | null,
  version: null as number | null,
  parentId: null as number | null,
  nodeType: 'DIR' as NodeType,
  titleZhCn: '',
  titleEnUs: '',
  path: '',
  routeName: '',
  icon: '',
  redirect: '',
  openType: 'internal' as OpenType,
  component: '',
  frameSrc: '',
  enabled: true,
  hidden: false,
  orderNo: 0,
  actions: [] as string[],
});

const isExternalLink = computed({
  get: () => form.openType === 'external' || !!form.frameSrc,
  set: (val: boolean) => {
    if (val) {
      form.openType = 'external';
    } else {
      form.openType = 'internal';
      form.frameSrc = '';
    }
  },
});

const resetForm = () => {
  form.id = null;
  form.version = null;
  form.parentId = null;
  form.nodeType = 'DIR';
  form.titleZhCn = '';
  form.titleEnUs = '';
  form.path = '';
  form.routeName = '';
  form.icon = '';
  form.redirect = '';
  form.openType = 'internal';
  form.component = '';
  form.frameSrc = '';
  form.hidden = false;
  form.orderNo = 0;
  form.actions = ['query', 'create', 'update', 'delete'];
};

const onParentIdChange = (v: unknown) => {
  form.parentId = normalizeParentId(v);
};

const onParentIdClear = () => {
  form.parentId = null;
};

const openCreateRoot = () => {
  if (!canCreate.value) {
    MessagePlugin.warning('无新增权限');
    return;
  }
  mode.value = 'create';
  resetForm();
  form.parentId = null;
  form.nodeType = 'DIR';
  form.component = 'LAYOUT';
  drawerVisible.value = true;
};

const openCreateChild = (parent: MenuNode, nodeType: NodeType) => {
  if (!canCreate.value) {
    MessagePlugin.warning('无新增权限');
    return;
  }
  mode.value = 'create';
  resetForm();
  form.parentId = parent.id;
  form.nodeType = nodeType;
  drawerVisible.value = true;
};

const openEdit = (node: MenuNode) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  mode.value = 'edit';
  resetForm();
  form.id = node.id;
  form.version = node.version ?? 0;
  form.parentId = node.parentId ?? null;
  form.nodeType = node.nodeType;
  form.titleZhCn = node.titleZhCn || '';
  form.titleEnUs = node.titleEnUs || '';
  form.path = node.path || '';
  form.routeName = node.routeName || '';
  form.icon = node.icon || '';
  form.redirect = node.redirect || '';
  form.component = node.component || '';
  form.frameSrc = node.frameSrc || '';
  form.hidden = !!node.hidden;
  form.orderNo = node.orderNo ?? 0;
  if (node.frameSrc) {
    form.openType = node.frameBlank ? 'external' : 'iframe';
  } else {
    form.openType = 'internal';
  }
  drawerVisible.value = true;
};

const normalizePathByParent = () => {
  if (!form.path) return;
  const p = form.path.trim();
  if (!p) return;
  if (form.parentId == null) {
    form.path = p.startsWith('/') ? p : `/${p.replace(/^\/+/, '')}`;
  } else {
    form.path = p.replace(/^\/+/, '');
  }
};

const ensureRouteFields = () => {
  // Always ensure path and routeName are generated if missing
  const seed = form.titleEnUs || form.titleZhCn || `menu-${Date.now()}`;
  const fallback = `menu-${Date.now().toString(36)}`;
  const slug = toSlug(seed) || fallback;

  if (!form.path.trim()) {
    // If button, path might not be relevant but required. use slug.
    form.path = form.parentId == null ? `/${slug}` : slug;
  }

  if (!form.routeName.trim()) {
    const name = toRouteName(slug);
    form.routeName = name || `Menu${Date.now().toString(36)}`;
  }

  normalizePathByParent();
};

const submitNode = async () => {
  if (mode.value === 'create' && !canCreate.value) {
    MessagePlugin.warning('无新增权限');
    return;
  }
  if (mode.value === 'edit' && !canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  if (!form.titleZhCn.trim()) {
    MessagePlugin.warning('请输入菜单名称');
    return;
  }

  // Auto-fill path and routeName if missing
  ensureRouteFields();

  if (form.nodeType === 'PAGE') {
    if (form.openType === 'internal') {
      // Internal page
      form.frameSrc = '';
      // If component is missing, default to path
      if (!form.component.trim()) {
        // remove leading slash for component path convention
        form.component = form.path.replace(/^\/+/, '');
      }
    } else {
      // External
      if (!form.frameSrc.trim()) {
        MessagePlugin.warning('请输入链接URL');
        return;
      }
      form.component = 'IFRAME';
    }
  } else if (form.nodeType === 'DIR') {
    form.openType = 'internal';
    form.frameSrc = '';
    if (form.parentId == null && !form.component.trim()) form.component = 'LAYOUT';
  } else if (form.nodeType === 'BTN') {
    // Button type
    form.openType = 'internal';
    form.component = '';
    form.frameSrc = '';
  }

  savingNode.value = true;
  try {
    const parentId = normalizeParentId(form.parentId);
    const hidden = !!form.hidden;

    const dataPayload = {
      parentId,
      nodeType: form.nodeType,
      path: form.path,
      routeName: form.routeName,
      component: form.component || undefined,
      redirect: form.redirect || undefined,
      titleZhCn: form.titleZhCn,
      titleEnUs: form.titleEnUs || undefined,
      icon: form.icon || undefined,
      enabled: !hidden,
      hidden,
      frameSrc: form.frameSrc || undefined,
      frameBlank: form.openType === 'external',
      orderNo: form.orderNo,
      actions: form.actions.join(','),
    };

    if (mode.value === 'create') {
      await request.post({
        url: '/system/menu',
        data: dataPayload,
      });
      MessagePlugin.success('创建成功');
    } else {
      await request.put({
        url: `/system/menu/${form.id}`,
        data: {
          version: form.version,
          ...dataPayload,
        },
      });
      MessagePlugin.success('保存成功');
    }
    drawerVisible.value = false;
    await reload();
    scheduleRefreshSidebar();
  } finally {
    savingNode.value = false;
  }
};

const removeNode = async (node: MenuNode) => {
  if (!canDelete.value) {
    MessagePlugin.warning('无删除权限');
    return;
  }
  const confirm = DialogPlugin.confirm({
    header: '\u786E\u8BA4\u5220\u9664',
    body: `\u786E\u5B9A\u5220\u9664\u300C${node.titleZhCn}\u300D\u5417\uFF1F`,
    confirmBtn: '\u5220\u9664',
    cancelBtn: '\u53D6\u6D88',
    theme: 'warning',
    onConfirm: async () => {
      confirm.hide();
      try {
        await request.delete({ url: `/system/menu/${node.id}`, params: { cascade: false } }, { joinParamsToUrl: true });
        MessagePlugin.success('\u5DF2\u5220\u9664');
        await reload();
        scheduleRefreshSidebar();
      } catch (e: any) {
        const msg = String(e?.message || '').replace(/\s*\[\d+\]$/, '');
        if (msg.includes('\u76EE\u5F55\u4E0B\u5B58\u5728\u5B50\u8282\u70B9')) {
          const confirmCascade = DialogPlugin.confirm({
            header: '\u76EE\u5F55\u4E0B\u5B58\u5728\u5B50\u8282\u70B9',
            body: '\u662F\u5426\u7EA7\u8054\u5220\u9664\u6574\u4E2A\u5B50\u6811\uFF1F\u6B64\u64CD\u4F5C\u4E0D\u53EF\u6062\u590D\u3002',
            confirmBtn: '\u7EA7\u8054\u5220\u9664',
            cancelBtn: '\u53D6\u6D88',
            theme: 'danger',
            onConfirm: async () => {
              confirmCascade.hide();
              try {
                await request.delete(
                  { url: `/system/menu/${node.id}`, params: { cascade: true } },
                  { joinParamsToUrl: true },
                );
                MessagePlugin.success('\u5DF2\u7EA7\u8054\u5220\u9664');
                await reload();
                scheduleRefreshSidebar();
              } catch (err: any) {
                const errMsg = String(err?.message || '').replace(/\s*\[\d+\]$/, '');
                MessagePlugin.error(errMsg || '\u5220\u9664\u5931\u8D25');
              }
            },
          });
        } else {
          MessagePlugin.error(msg || '\u5220\u9664\u5931\u8D25');
        }
      }
    },
  });
};

watch(
  () => [form.nodeType, form.openType, form.parentId] as const,
  ([nt, ot, pid], [_oldNt, _oldOt, oldPid]) => {
    // 当父节点从有变为无（取消父目录，提升为一级目录）
    if (oldPid != null && pid == null && form.path && !form.path.startsWith('/')) {
      form.path = `/${form.path.replace(/^\/+/, '')}`;
    }
    // 当父节点从无变为有（从一级目录降级为子节点）
    if (oldPid == null && pid != null && form.path && form.path.startsWith('/')) {
      form.path = form.path.replace(/^\/+/, '');
    }

    if (nt === 'BTN') {
      form.openType = 'internal';
      form.component = '';
      return;
    }

    if (nt !== 'PAGE') {
      form.openType = 'internal';
      form.frameSrc = '';
      if (pid == null && !form.component.trim()) form.component = 'LAYOUT';
      return;
    }
    if (ot === 'internal') {
      form.frameSrc = '';
      if (form.component === 'IFRAME') form.component = '';
    } else {
      form.component = 'IFRAME';
    }
  },
);

onMounted(async () => {
  void nodeTypeDict.load();
  void actionDict.load();
  roles.value = await request.get<RoleRow[]>({ url: '/system/role/list' });
  await reload();
});
</script>
<style scoped lang="less">
.menu-toolbar {
  flex-wrap: wrap;
  gap: 12px;
}

.menu-table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.menu-table-wrapper :deep(.t-enhanced-table) {
  min-width: 1080px;
}

.menu-table-wrapper :deep(.menu-table__operate-cell) {
  white-space: normal;
}

.menu-table-wrapper :deep(.menu-table__operate-links) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 10px;
}

@media (width <= 768px) {
  .menu-toolbar {
    align-items: stretch;
  }

  .menu-toolbar :deep(.t-input) {
    width: 100% !important;
  }

  .menu-toolbar :deep(.t-button) {
    width: 100%;
  }

  .menu-table-wrapper {
    overflow-x: auto;
  }

  .menu-table-wrapper :deep(.t-enhanced-table) {
    min-width: 880px;
  }
}
</style>
<style scoped>
.menu-table__drag-handle {
  cursor: grab;
}

.menu-table__drag-handle:active {
  cursor: grabbing;
}

.menu-advanced-toggle {
  margin: 6px 0 12px;
}

.menu-icon-value {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

:deep(.menu-icon-select__popup .t-select__list) {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px 12px;
  max-width: 552px;
}

:deep(.menu-icon-select__popup .t-select-option) {
  justify-content: flex-start;
  height: 36px;
  padding: 0 8px;
}

:deep(.menu-icon-select__popup .t-select-option > span) {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
}

:deep(.menu-icon-select__popup .t-select-option + .t-select-option) {
  margin-top: 0;
}

.menu-icon-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.menu-icon-option__label {
  display: inline-block;
  font-size: 12px;
  color: var(--td-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.menu-icon-select__popup .t-icon),
.menu-icon-value :deep(.t-icon) {
  font-size: 16px;
}

.menu-drawer-form {
  padding: 16px 24px;
}

.menu-drawer-form :deep(.t-form__item) {
  margin-bottom: 16px;
}

.menu-drawer-form :deep(.t-form__item-label) {
  color: var(--td-text-color-primary);
  font-weight: 500;
}

.menu-drawer-form :deep(.t-form__controls),
.menu-drawer-form :deep(.t-input),
.menu-drawer-form :deep(.t-select),
.menu-drawer-form :deep(.t-tree-select),
.menu-drawer-form :deep(.t-input-number),
.menu-drawer-form :deep(.t-textarea) {
  width: 100%;
}

.menu-drawer-form :deep(.t-radio-group) {
  gap: 16px;
}
</style>
