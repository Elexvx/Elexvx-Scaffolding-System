<template>
  <t-card title="目录/页面管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <menu-toolbar
        v-model:keyword="search.keyword"
        :expand-all="expandAll"
        :dirty="dirty"
        :can-update="canUpdate"
        :saving-order="savingOrder"
        @create-root="openCreateRoot"
        @reload="reload"
        @toggle-expand="onExpandAllToggle"
        @save-order="saveOrder"
      />

      <menu-tree-table
        v-model:table-ref="tableRef"
        v-model:expanded-nodes="expandedTreeNodes"
        :data="filteredData"
        :columns="columns"
        :tree-config="treeConfig"
        :loading="loading"
        :before-drag-sort="beforeDragSort"
        @abnormal-drag-sort="onAbnormalDragSort"
        @drag-sort="onDragSort"
      />
    </t-space>

    <menu-form-dialog
      v-model:visible="drawerVisible"
      :title="drawerTitle"
      :form="form"
      :saving="savingNode"
      :is-external-link="isExternalLink"
      :parent-options="parentOptions"
      :tree-keys="menuTreeKeys"
      :node-type-radio-options="nodeTypeRadioOptions"
      :icon-options="iconOptions"
      :page-component-options="pageComponentOptions"
      :render-icon-value="renderIconValue"
      :on-parent-id-change="onParentIdChange"
      :on-parent-id-clear="onParentIdClear"
      @update:external-link="(value) => (isExternalLink = value)"
      @cancel="drawerVisible = false"
      @confirm="submitNode"
    />
  </t-card>
</template>

<script setup lang="tsx">
import { manifest as iconManifest } from 'tdesign-icons-vue-next';
import type { SelectOption } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, ref, watch } from 'vue';

import { useDictionary } from '@/hooks/useDictionary';
import { getPermissionStore, useUserStore } from '@/store';
import { buildDictOptions } from '@/utils/dict';
import { request } from '@/utils/request';
import { migrateLocalStorageKey } from '@/utils/storage/compat';

import { actionValues, fallbackActionOptions, fallbackMenuTypeOptions, menuTreeKeys, menuTypeValues } from '../constants/menuOptions';
import { EXPANDED_STORAGE_KEY, LEGACY_EXPANDED_STORAGE_KEY, persistExpanded, readExpandedFromStorage, sanitizeExpanded } from '../helpers';
import { useMenuColumns } from '../hooks/useMenuColumns';
import { useMenuDialog } from '../hooks/useMenuDialog';
import { useMenuDragSort } from '../hooks/useMenuDragSort';
import { useMenuForm } from '../hooks/useMenuForm';
import { useMenuPageState } from '../hooks/useMenuPageState';
import { useMenuPermissions } from '../hooks/useMenuPermissions';
import { resetMenuFormModel } from '../schema/menuFormSchema';
import type { MenuNode, MenuSubmitPayload, MenuType, RoleRow } from '../types';
import { buildPageComponentOptions } from '../utils/componentPathCompat';
import { applyMenuNodeToForm, createMenuSubmitPayload, ensureRouteFields, normalizeParentId } from '../utils/menuMappers';
import { buildReorderItems, findNodeById, updateTreeData } from '../utils/menuTree';
import MenuFormDialog from './MenuFormDialog.vue';
import MenuToolbar from './MenuToolbar.vue';
import MenuTreeTable from './MenuTreeTable.vue';

const {
  tableRef,
  loading,
  savingOrder,
  savingNode,
  data,
  expandedTreeNodes,
  expandAll,
  dirty,
  rowSaving,
  search,
  treeConfig,
  filteredData,
  dirTreeData,
  allParentTree,
} = useMenuPageState();
const { canQuery, canCreate, canUpdate, canDelete } = useMenuPermissions();
const { drawerVisible, mode, drawerTitle } = useMenuDialog();
const { form, isExternalLink, onParentIdChange, onParentIdClear } = useMenuForm();

const nodeTypeDict = useDictionary('menu_node_type');
const actionDict = useDictionary('menu_action');
const roles = ref<RoleRow[]>([]);

const nodeTypeOptions = computed(() => buildDictOptions(nodeTypeDict.items.value, fallbackMenuTypeOptions, menuTypeValues));
const nodeTypeRadioOptions = computed(
  () =>
    nodeTypeOptions.value.filter((opt) => opt && typeof opt === 'object' && 'value' in opt && 'label' in opt) as Array<{
      label: string;
      value: string | number | boolean;
      disabled?: boolean;
    }>,
);
const _actionOptions = computed(() => buildDictOptions(actionDict.items.value, fallbackActionOptions, actionValues));
const _roleOptions = computed<SelectOption[]>(() => roles.value.map((role) => ({ label: role.name, value: role.name })));
const _permissionOptions = computed<SelectOption[]>(() => {
  const set = new Set<string>();
  roles.value.forEach((role) => (role.permissions || []).forEach((permission) => permission && set.add(permission)));
  return Array.from(set)
    .sort()
    .map((permission) => ({ label: permission, value: permission }));
});

const iconOptions = computed<SelectOption[]>(() => {
  const list = Array.isArray(iconManifest) ? iconManifest : [];
  return list
    .map((item: { stem?: string }) => item?.stem)
    .filter((value): value is string => typeof value === 'string' && value.length > 0)
    .sort((left, right) => left.localeCompare(right))
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
const renderIconValue = (_h: unknown, context: { value?: unknown }) => {
  const name = Array.isArray(context?.value) ? context?.value[0] : context?.value;
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
const parentOptions = computed(() => (form.nodeType === 'BTN' ? allParentTree.value : dirTreeData.value));

watch(
  expandedTreeNodes,
  (value) => {
    persistExpanded(value);
  },
  { deep: true },
);

const loadExpandedFromStorage = () =>
  readExpandedFromStorage(() => migrateLocalStorageKey(EXPANDED_STORAGE_KEY, [LEGACY_EXPANDED_STORAGE_KEY]));

const permissionStore = getPermissionStore();
const userStore = useUserStore();
let refreshingSidebar = false;
let refreshSidebarTimer: number | null = null;
const refreshSidebarNow = async () => {
  if (refreshingSidebar) return;
  refreshingSidebar = true;
  try {
    await permissionStore.refreshAsyncRoutes(userStore.userInfo);
  } catch (error: unknown) {
    MessagePlugin.warning(String((error as { message?: string })?.message || '导航栏刷新失败'));
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
    expandedTreeNodes.value = sanitizeExpanded(loadExpandedFromStorage(), data.value);
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

const onMovePage = async (page: MenuNode, destinationId: number) => {
  const destination = findNodeById(data.value, destinationId);
  if (!destination || destination.nodeType !== 'DIR') {
    MessagePlugin.warning('目标父节点必须为目录');
    return;
  }
  await request.put({
    url: `/system/menu/${page.id}`,
    data: { version: page.version, parentId: destinationId, orderNo: destination.children?.length || 0 },
  });
  MessagePlugin.success('已移动');
  await reload();
  scheduleRefreshSidebar();
};
const { openMoveDialog, beforeDragSort, onAbnormalDragSort } = useMenuDragSort({
  canUpdate,
  keyword: computed(() => search.keyword),
  data,
  dirTreeData,
  treeKeys: menuTreeKeys,
  onMovePage,
});

const saveOrder = async () => {
  if (!canUpdate.value) return MessagePlugin.warning('无修改权限');
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
const onDragSort = (context: { newData?: MenuNode[] }) => {
  dirty.value = true;
  if (context?.newData) {
    const tree = tableRef.value?.getTreeNode?.();
    if (tree) data.value = tree;
  }
};

const updateRow = async (row: MenuNode, patch: Partial<MenuNode>) => {
  if (!canUpdate.value) return MessagePlugin.warning('无修改权限');
  rowSaving.value[row.id] = true;
  try {
    if (typeof patch.hidden === 'boolean') patch.enabled = !patch.hidden;
    const result = await request.put<MenuNode>({ url: `/system/menu/${row.id}`, data: { version: row.version, ...patch } });
    updateTreeData(data.value, row.id, result);
    scheduleRefreshSidebar();
  } finally {
    rowSaving.value[row.id] = false;
  }
};

const openCreateRoot = () => {
  if (!canCreate.value) return MessagePlugin.warning('无新增权限');
  mode.value = 'create';
  resetMenuFormModel(form);
  form.component = 'LAYOUT';
  drawerVisible.value = true;
};
const openCreateChild = (parent: MenuNode, nodeType: MenuType) => {
  if (!canCreate.value) return MessagePlugin.warning('无新增权限');
  mode.value = 'create';
  resetMenuFormModel(form);
  form.parentId = parent.id;
  form.nodeType = nodeType;
  drawerVisible.value = true;
};
const openEdit = (node: MenuNode) => {
  if (!canUpdate.value) return MessagePlugin.warning('无修改权限');
  mode.value = 'edit';
  resetMenuFormModel(form);
  applyMenuNodeToForm(form, node);
  drawerVisible.value = true;
};

const submitNode = async () => {
  if (mode.value === 'create' && !canCreate.value) return MessagePlugin.warning('无新增权限');
  if (mode.value === 'edit' && !canUpdate.value) return MessagePlugin.warning('无修改权限');
  if (!form.titleZhCn.trim()) return MessagePlugin.warning('请输入菜单名称');
  ensureRouteFields(form);
  if (form.nodeType === 'PAGE') {
    if (form.openType === 'internal') {
      form.frameSrc = '';
      if (!form.component.trim()) form.component = form.path.replace(/^\/+/, '');
    } else {
      if (!form.frameSrc.trim()) return MessagePlugin.warning('请输入链接URL');
      form.component = 'IFRAME';
    }
  } else if (form.nodeType === 'DIR') {
    form.openType = 'internal';
    form.frameSrc = '';
    if (form.parentId == null && !form.component.trim()) form.component = 'LAYOUT';
  } else {
    form.openType = 'internal';
    form.component = '';
    form.frameSrc = '';
  }
  savingNode.value = true;
  try {
    const payload: MenuSubmitPayload = createMenuSubmitPayload(form);
    payload.parentId = normalizeParentId(form.parentId);
    if (mode.value === 'create') {
      await request.post({ url: '/system/menu', data: payload });
      MessagePlugin.success('创建成功');
    } else {
      await request.put({ url: `/system/menu/${form.id}`, data: { version: form.version, ...payload } });
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
  if (!canDelete.value) return MessagePlugin.warning('无删除权限');
  const confirm = DialogPlugin.confirm({
    header: '确认删除',
    body: `确定删除「${node.titleZhCn}」吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    theme: 'warning',
    onConfirm: async () => {
      confirm.hide();
      try {
        await request.delete({ url: `/system/menu/${node.id}`, params: { cascade: false } }, { joinParamsToUrl: true });
        MessagePlugin.success('已删除');
        await reload();
        scheduleRefreshSidebar();
      } catch (error: unknown) {
        const message = String((error as { message?: string })?.message || '').replace(/\s*\[\d+\]$/, '');
        if (message.includes('目录下存在子节点')) {
          const cascade = DialogPlugin.confirm({
            header: '目录下存在子节点',
            body: '是否级联删除整个子树？此操作不可恢复。',
            confirmBtn: '级联删除',
            cancelBtn: '取消',
            theme: 'danger',
            onConfirm: async () => {
              cascade.hide();
              await request.delete({ url: `/system/menu/${node.id}`, params: { cascade: true } }, { joinParamsToUrl: true });
              MessagePlugin.success('已级联删除');
              await reload();
              scheduleRefreshSidebar();
            },
          });
          return;
        }
        MessagePlugin.error(message || '删除失败');
      }
    },
  });
};

const columns = useMenuColumns({
  data,
  nodeTypeItems: computed(() => nodeTypeDict.items.value as unknown as Array<{ label: string; value: string | number | boolean }>),
  canCreate,
  canUpdate,
  canDelete,
  rowSaving,
  onCreateChild: openCreateChild,
  onEdit: openEdit,
  onMove: openMoveDialog,
  onDelete: removeNode,
  onToggleHidden: (row, hidden) => updateRow(row, { hidden }),
});

onMounted(async () => {
  void nodeTypeDict.load();
  void actionDict.load();
  roles.value = await request.get<RoleRow[]>({ url: '/system/role/list' });
  await reload();
});
</script>
<style scoped lang="less" src="./MenuPageMain.less"></style>
