import type { FormInstanceFunctions, FormRule } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref, watch } from 'vue';

import { ROLE_ACTION_LABEL_MAP } from '../constants/roleOptions';
import type { ActionMenuOption, MenuNode, PermissionCatalogItem, RoleRow } from '../types';
import {
  buildSubmitMenuActionMap,
  filterHiddenMenus,
  getAllMenuIds,
  normalizeActions,
  toActionMapForAdmin,
  toActionMapFromPermissions,
} from '../utils/roleMappers';
import { useRoleApi } from './useRoleApi';
import { useRolePermissions } from './useRolePermissions';

export const useRolePageState = () => {
  const roleApi = useRoleApi();
  const { canQuery, canCreate, canUpdate, canDelete } = useRolePermissions();

  const loading = ref(false);
  const saving = ref(false);
  const rows = ref<RoleRow[]>([]);
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
      .map((item) => ({ label: item!.title, value: item!.menuId }));
  });

  const activeActionCatalog = computed(() => {
    if (activeActionMenuId.value == null) return null;
    return catalogByMenuId.value.get(activeActionMenuId.value) || null;
  });

  const rules: Record<string, FormRule[]> = {
    name: [{ required: true, message: '请输入角色名', type: 'error' }],
  };

  const getActionLabel = (action: string) => ROLE_ACTION_LABEL_MAP[action] || action;

  const syncActiveActionMenu = () => {
    const selectableIds = actionMenuOptions.value.map((opt) => Number(opt.value));
    if (activeActionMenuId.value != null && selectableIds.includes(activeActionMenuId.value)) return;
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

  const getMenuActions = (menuId: number) => form.menuActionMap[String(menuId)] || [];

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

  const onExpandAllToggle = (val: boolean) => {
    expandedMenuIds.value = val ? getAllMenuIds(menuTree.value) : [];
  };

  const onSelectAllToggle = (val: boolean) => {
    form.menuIds = val ? getAllMenuIds(menuTree.value) : [];
  };

  const onMenuNodeClick = (context: any) => {
    const rawId = context?.node?.id ?? context?.data?.id ?? context?.id ?? context?.value;
    const menuId = Number(rawId);
    if (!Number.isFinite(menuId)) return;
    if (!form.menuIds.includes(menuId)) return;
    if (!catalogByMenuId.value.has(menuId)) return;
    activeActionMenuId.value = menuId;
  };

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

  watch(
    () => expandedMenuIds.value,
    (val) => {
      const allIds = getAllMenuIds(menuTree.value);
      expandAll.value = val.length > 0 && val.length === allIds.length;
    },
    { deep: true },
  );

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
      const [tree, catalog] = await Promise.all([roleApi.fetchMenuTree(), roleApi.fetchPermissionCatalog()]);
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
      rows.value = await roleApi.fetchRoleList();
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
    const detail = await roleApi.fetchRoleDetail(row.id);
    form.name = detail.name;
    form.description = detail.description || '';
    await loadPermissionOptions();
    form.menuIds = form.name === 'admin' ? getAllMenuIds(menuTree.value) : [...(detail.menuIds || [])];
    if (form.name === 'admin') {
      form.menuActionMap = toActionMapForAdmin(form.menuIds, permissionCatalog.value);
    } else {
      form.menuActionMap = toActionMapFromPermissions(detail.permissions || [], form.menuIds, permissionCatalog.value);
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
        menuActionMap: buildSubmitMenuActionMap(form.menuIds, form.menuActionMap, catalogByMenuId.value),
      };
      if (editingId.value) {
        await roleApi.updateRole(editingId.value, payload);
      } else {
        await roleApi.createRole(payload);
      }
      MessagePlugin.success('保存成功');
      drawerVisible.value = false;
      await reload();
    } finally {
      saving.value = false;
    }
  };

  const onSubmit = (ctx: any) => {
    if (ctx?.validateResult === true) {
      submitForm();
    }
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
        await roleApi.deleteRole(row.id);
        MessagePlugin.success('已删除');
        dialog.hide();
        await reload();
      },
    });
  };

  return {
    loading,
    saving,
    canCreate,
    canUpdate,
    rows,
    drawerVisible,
    drawerTitle,
    formRef,
    form,
    menuTree,
    treeRef,
    expandAll,
    expandedMenuIds,
    selectAll,
    checkStrictly,
    activeActionMenuId,
    actionMenuOptions,
    activeActionCatalog,
    rules,
    editingId,
    getActionLabel,
    getMenuActions,
    setMenuActions,
    onExpandAllToggle,
    onSelectAllToggle,
    onMenuNodeClick,
    reload,
    openCreate,
    openEdit,
    submitForm,
    onSubmit,
    removeRow,
  };
};
