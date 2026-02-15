<template>
  <t-card title="角色管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space>
        <t-button v-perm="'system:SystemRole:create'" theme="primary" @click="openCreate">新增角色</t-button>
        <t-button variant="outline" @click="reload">刷新</t-button>
      </t-space>

      <t-table row-key="id" :data="rows" :columns="columns" :loading="loading">
        <template #op="{ row }">
          <t-space>
            <t-link v-perm:disable="'system:SystemRole:update'" theme="primary" @click="openEdit(row)">编辑</t-link>
            <t-link v-perm:disable="'system:SystemRole:delete'" theme="danger" :disabled="row.name === 'admin'" @click="removeRow(row)"
              >删除</t-link
            >
          </t-space>
        </template>
      </t-table>
    </t-space>

    <confirm-drawer v-model:visible="drawerVisible" :header="drawerTitle" size="760px">
      <t-form
        ref="formRef"
        class="drawer-form--single"
        :data="form"
        :rules="rules"
        label-width="120px"
        layout="vertical"
        label-align="right"
        @submit="onSubmit"
      >
        <t-row :gutter="[24, 24]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="角色名" name="name">
              <t-input v-model="form.name" :disabled="form.name === 'admin'" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="描述" name="description">
              <t-input v-model="form.description" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="14">
            <t-form-item label="菜单权限">
              <t-space direction="vertical" style="width: 100%">
                <t-space>
                  <t-checkbox v-model="expandAll" @change="onExpandAllToggle">展开/折叠</t-checkbox>
                  <t-checkbox v-model="selectAll" @change="onSelectAllToggle">全选/全不选</t-checkbox>
                  <t-checkbox v-model="checkStrictly">父子联动</t-checkbox>
                </t-space>
                <div class="menu-tree-container">
                  <t-tree
                    ref="treeRef"
                    v-model="form.menuIds"
                    v-model:expanded="expandedMenuIds"
                    :data="menuTree"
                    :keys="{ value: 'id', label: 'titleZhCn' }"
                    checkable
                    :check-strictly="!checkStrictly"
                    :disabled="form.name === 'admin'"
                    hover
                    @click="onMenuNodeClick"
                  />
                </div>
              </t-space>
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="10">
            <t-form-item label="页面动作权限">
              <t-space direction="vertical" style="width: 100%">
                <t-select
                  v-model="activeActionMenuId"
                  :options="actionMenuOptions"
                  clearable
                  placeholder="请选择已勾选的页面"
                  :disabled="form.name === 'admin'"
                />
                <div class="menu-action-container">
                  <template v-if="activeActionCatalog">
                    <div class="menu-action-title">{{ activeActionCatalog.title }}</div>
                    <t-checkbox-group
                      :model-value="getMenuActions(activeActionCatalog.menuId)"
                      :disabled="form.name === 'admin'"
                      @change="(value) => setMenuActions(activeActionCatalog.menuId, value as string[])"
                    >
                      <t-space wrap>
                        <t-checkbox v-for="action in activeActionCatalog.actions" :key="action" :value="action">
                          {{ getActionLabel(action) }}
                        </t-checkbox>
                      </t-space>
                    </t-checkbox-group>
                  </template>
                  <span v-else class="menu-action-empty">请先勾选页面菜单，再选择页面配置动作权限</span>
                </div>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>

      <template #footer>
        <t-space class="tdesign-starter-action-bar">
          <t-button variant="outline" @click="drawerVisible = false">取消</t-button>
          <t-button
            v-perm:disable="editingId ? 'system:SystemRole:update' : 'system:SystemRole:create'"
            theme="primary"
            :loading="saving"
            @click="submitForm"
            >保存</t-button
          >
        </t-space>
      </template>
    </confirm-drawer>
  </t-card>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, PrimaryTableCol } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { hasPerm } from '@/utils/permission';
import { request } from '@/utils/request';

interface RoleRow {
  id: number;
  name: string;
  description?: string;
  menuIds?: number[];
  permissions?: string[];
}

interface MenuNode {
  id: number;
  titleZhCn: string;
  path?: string | null;
  routeName?: string | null;
  nodeType?: string | null;
  actions?: string;
  hidden?: boolean;
  children?: MenuNode[];
}

interface PermissionCatalogItem {
  menuId: number;
  routeName: string;
  title: string;
  actions: string[];
  permissionCodes: Record<string, string>;
}

const loading = ref(false);
const saving = ref(false);
const canQuery = computed(() => hasPerm('system:SystemRole:query'));
const canCreate = computed(() => hasPerm('system:SystemRole:create'));
const canUpdate = computed(() => hasPerm('system:SystemRole:update'));
const canDelete = computed(() => hasPerm('system:SystemRole:delete'));
const rows = ref<RoleRow[]>([]);

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    fixed: 'left',
    cell: (_h, { rowIndex }) => String(rowIndex + 1),
  },
  { colKey: 'name', title: '角色名', width: 160, ellipsis: true },
  { colKey: 'description', title: '描述', width: 220, ellipsis: true },
  { colKey: 'op', title: '操作', width: 160, fixed: 'right' },
];

const drawerVisible = ref(false);
const editingId = ref<number | null>(null);
const drawerTitle = computed(() => (editingId.value ? '编辑角色' : '新增角色'));

const formRef = ref<FormInstanceFunctions>();
const form = reactive({
  name: '',
  description: '',
  menuIds: [] as number[],
  menuActionMap: {} as Record<string, string[]>,
});

const menuTree = ref<MenuNode[]>([]);
const permissionCatalog = ref<PermissionCatalogItem[]>([]);
const treeRef = ref();
const expandAll = ref(false);
const expandedMenuIds = ref<number[]>([]);
const selectAll = ref(false);
const checkStrictly = ref(false);
const activeActionMenuId = ref<number | null>(null);
type ActionMenuOption = { label: string; value: number };

const catalogByMenuId = computed(() => {
  const map = new Map<number, PermissionCatalogItem>();
  permissionCatalog.value.forEach((item) => {
    if (item?.menuId == null) return;
    map.set(Number(item.menuId), item);
  });
  return map;
});

const actionMenuOptions = computed<ActionMenuOption[]>(() => {
  return form.menuIds
    .map((menuId) => catalogByMenuId.value.get(menuId))
    .filter(Boolean)
    .map((item) => ({
      label: item!.title,
      value: item!.menuId,
    }));
});

const activeActionCatalog = computed(() => {
  if (activeActionMenuId.value == null) return null;
  return catalogByMenuId.value.get(activeActionMenuId.value) || null;
});

const ACTION_LABEL_MAP: Record<string, string> = {
  query: '查询',
  create: '新增',
  update: '修改',
  delete: '删除',
  import: '导入',
  export: '导出',
  approve: '审批',
};

const getActionLabel = (action: string) => ACTION_LABEL_MAP[action] || action;

const normalizeActions = (actions?: string[]) => {
  if (!actions?.length) return [] as string[];
  const set = new Set<string>();
  actions.forEach((action) => {
    const value = String(action || '').trim();
    if (value) set.add(value);
  });
  return Array.from(set);
};

const syncActiveActionMenu = () => {
  const selectableIds = actionMenuOptions.value.map((opt) => Number(opt.value));
  if (activeActionMenuId.value != null && selectableIds.includes(activeActionMenuId.value)) {
    return;
  }
  activeActionMenuId.value = selectableIds.length ? selectableIds[0] : null;
};

const syncMenuActionMap = () => {
  const selectedMenuSet = new Set(form.menuIds.map((id) => String(id)));
  Object.keys(form.menuActionMap).forEach((menuId) => {
    if (!selectedMenuSet.has(menuId)) {
      delete form.menuActionMap[menuId];
    }
  });

  form.menuIds.forEach((menuId) => {
    const catalog = catalogByMenuId.value.get(menuId);
    if (!catalog) return;
    const key = String(menuId);
    const validActions = new Set(catalog.actions);
    const current = normalizeActions(form.menuActionMap[key]).filter((action) => validActions.has(action));
    if (current.length > 0) {
      form.menuActionMap[key] = current;
      return;
    }
    if (catalog.actions.includes('query')) {
      form.menuActionMap[key] = ['query'];
    }
  });
};

const getMenuActions = (menuId: number) => {
  return form.menuActionMap[String(menuId)] || [];
};

const setMenuActions = (menuId: number, actions: string[]) => {
  const key = String(menuId);
  const catalog = catalogByMenuId.value.get(menuId);
  if (!catalog) {
    delete form.menuActionMap[key];
    return;
  }
  const validActions = new Set(catalog.actions);
  const selected = normalizeActions(actions).filter((action) => validActions.has(action));
  if (selected.length > 0) {
    form.menuActionMap[key] = selected;
  } else {
    delete form.menuActionMap[key];
  }
};

const toActionMapFromPermissions = (permissions: string[], menuIds: number[]) => {
  const selectedMenuSet = new Set(menuIds.map((id) => String(id)));
  const permissionSet = new Set((permissions || []).map((item) => String(item || '').trim()).filter(Boolean));
  const map: Record<string, string[]> = {};
  permissionCatalog.value.forEach((catalog) => {
    const key = String(catalog.menuId);
    if (!selectedMenuSet.has(key)) return;
    const actions = catalog.actions.filter((action) => {
      const code = catalog.permissionCodes[action];
      return !!code && permissionSet.has(code);
    });
    if (actions.length > 0) {
      map[key] = actions;
    }
  });
  return map;
};

const toActionMapForAdmin = (menuIds: number[]) => {
  const selectedMenuSet = new Set(menuIds.map((id) => String(id)));
  const map: Record<string, string[]> = {};
  permissionCatalog.value.forEach((catalog) => {
    const key = String(catalog.menuId);
    if (!selectedMenuSet.has(key)) return;
    if (catalog.actions.length > 0) {
      map[key] = [...catalog.actions];
    }
  });
  return map;
};

const buildSubmitMenuActionMap = () => {
  const selectedMenuSet = new Set(form.menuIds.map((id) => String(id)));
  const map: Record<number, string[]> = {};
  Object.entries(form.menuActionMap).forEach(([menuId, actions]) => {
    if (!selectedMenuSet.has(menuId)) return;
    const menuIdValue = Number(menuId);
    if (!Number.isFinite(menuIdValue)) return;
    const catalog = catalogByMenuId.value.get(menuIdValue);
    if (!catalog) return;
    const validActions = new Set(catalog.actions);
    const selected = normalizeActions(actions).filter((action) => validActions.has(action));
    if (selected.length > 0) {
      map[menuIdValue] = selected;
    }
  });
  return map;
};

const onExpandAllToggle = (val: boolean) => {
  if (val) {
    expandedMenuIds.value = getAllMenuIds(menuTree.value);
  } else {
    expandedMenuIds.value = [];
  }
};

const getAllMenuIds = (nodes: MenuNode[]): number[] => {
  let ids: number[] = [];
  nodes.forEach((n) => {
    ids.push(n.id);
    if (n.children?.length) {
      ids = ids.concat(getAllMenuIds(n.children));
    }
  });
  return ids;
};

const onSelectAllToggle = (val: boolean) => {
  if (val) {
    form.menuIds = getAllMenuIds(menuTree.value);
  } else {
    form.menuIds = [];
  }
};

const filterHiddenMenus = (nodes: MenuNode[]): MenuNode[] => {
  return nodes
    .filter((node) => !node.hidden)
    .map((node) => ({
      ...node,
      children: node.children ? filterHiddenMenus(node.children) : [],
    }));
};

const onMenuNodeClick = (context: any) => {
  const rawId = context?.node?.id ?? context?.data?.id ?? context?.id ?? context?.value;
  const menuId = Number(rawId);
  if (!Number.isFinite(menuId)) return;
  if (!form.menuIds.includes(menuId)) return;
  if (!catalogByMenuId.value.has(menuId)) return;
  activeActionMenuId.value = menuId;
};

// 监听菜单选择变化，更新全选状态
watch(
  () => form.menuIds,
  (val) => {
    const allIds = getAllMenuIds(menuTree.value);
    selectAll.value = val.length > 0 && val.length === allIds.length;
    syncMenuActionMap();
    syncActiveActionMenu();
  },
  { deep: true },
);

// 监听展开状态变化，更新展开/折叠状态
watch(
  () => expandedMenuIds.value,
  (val) => {
    const allIds = getAllMenuIds(menuTree.value);
    expandAll.value = val.length > 0 && val.length === allIds.length;
  },
  { deep: true },
);

const rules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入角色名', type: 'error' }],
};

const resetForm = () => {
  form.name = '';
  form.description = '';
  form.menuIds = [];
  form.menuActionMap = {};
  expandAll.value = false;
  expandedMenuIds.value = [];
  selectAll.value = false;
  checkStrictly.value = true;
  activeActionMenuId.value = null;
};

const loadPermissionOptions = async () => {
  try {
    const [tree, catalog] = await Promise.all([
      request.get<MenuNode[]>({ url: '/system/menu/tree' }),
      request.get<PermissionCatalogItem[]>({ url: '/system/permission/catalog' }),
    ]);
    menuTree.value = filterHiddenMenus(tree || []);
    permissionCatalog.value = (catalog || []).map((item) => ({
      ...item,
      actions: normalizeActions(item.actions),
      permissionCodes: item.permissionCodes || {},
    }));
    syncMenuActionMap();
    syncActiveActionMenu();
  } catch {
    menuTree.value = [];
    permissionCatalog.value = [];
  }
};

const reload = async () => {
  if (!canQuery.value) {
    rows.value = [];
    return;
  }
  loading.value = true;
  try {
    rows.value = await request.get<RoleRow[]>({ url: '/system/role/list' });
    await loadPermissionOptions();
  } finally {
    loading.value = false;
  }
};

const openCreate = async () => {
  if (!canCreate.value) {
    MessagePlugin.warning('无创建权限');
    return;
  }
  editingId.value = null;
  resetForm();
  await loadPermissionOptions();
  drawerVisible.value = true;
};

const openEdit = async (row: RoleRow) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无编辑权限');
    return;
  }
  editingId.value = row.id;
  resetForm();
  const detail = await request.get<RoleRow>({ url: `/system/role/${row.id}` });
  form.name = detail.name;
  form.description = detail.description || '';
  await loadPermissionOptions();

  form.menuIds = form.name === 'admin' ? getAllMenuIds(menuTree.value) : [...(detail.menuIds || [])];
  if (form.name === 'admin') {
    form.menuActionMap = toActionMapForAdmin(form.menuIds);
  } else {
    form.menuActionMap = toActionMapFromPermissions(detail.permissions || [], form.menuIds);
  }
  syncMenuActionMap();
  syncActiveActionMenu();
  drawerVisible.value = true;
};

const submitForm = async () => {
  const vr = await formRef.value?.validate();
  if (vr !== true) return;
  if (editingId.value && !canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  if (!editingId.value && !canCreate.value) {
    MessagePlugin.warning('无创建权限');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      name: form.name,
      description: form.description || undefined,
      menuIds: form.menuIds,
      menuActionMap: buildSubmitMenuActionMap(),
    };
    if (editingId.value) {
      await request.put({ url: `/system/role/${editingId.value}`, data: payload });
    } else {
      await request.post({ url: '/system/role', data: payload });
    }
    MessagePlugin.success('保存成功');
    drawerVisible.value = false;
    reload();
  } finally {
    saving.value = false;
  }
};

const onSubmit = (ctx: any) => {
  if (ctx?.validateResult === true) submitForm();
};

const removeRow = (row: RoleRow) => {
  if (!canDelete.value) {
    MessagePlugin.warning('无删除权限');
    return;
  }
  if (row.name === 'admin') {
    MessagePlugin.warning('不允许删除管理员角色');
    return;
  }
  const dialog = DialogPlugin.confirm({
    header: '删除角色',
    body: `确认删除角色「${row.name}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await request.delete({ url: `/system/role/${row.id}` });
      MessagePlugin.success('已删除');
      dialog.hide();
      reload();
    },
  });
};

onMounted(async () => {
  await reload();
});
</script>
<style lang="less" scoped>
.menu-tree-container {
  margin-top: 8px;
  padding: 8px;
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  max-height: 400px;
  overflow-y: auto;
}

.menu-action-container {
  margin-top: 8px;
  padding: 12px;
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  min-height: 120px;
}

.menu-action-title {
  margin-bottom: 8px;
  color: var(--td-text-color-primary);
  font-weight: 600;
}

.menu-action-empty {
  color: var(--td-text-color-placeholder);
}
</style>
