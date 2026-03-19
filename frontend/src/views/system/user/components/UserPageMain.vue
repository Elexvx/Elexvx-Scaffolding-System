<template>
  <div class="user-management">
    <t-card title="用户管理" :bordered="false" class="user-panel">
      <div class="user-panel__content">
        <user-org-tree
          v-model:keyword="orgKeyword"
          :tree-data="filteredOrgTree"
          :expanded-ids="expandedOrgIds"
          :tree-keys="userOrgTreeKeys"
          @filter="filterOrgTree"
          @select="handleOrgSelect"
        />

        <div class="user-panel__table">
          <user-toolbar :filters="filters" :status-options="statusOptions" @search="reload" @reset="resetFiltersAndReload" @create="openCreate" />

          <user-table
            :rows="rows"
            :columns="columns"
            :pagination="pagination"
            :loading="loading"
            :can-update="canUpdate"
            :is-edit-disabled="isEditDisabled"
            :is-reset-disabled="isResetDisabled"
            :is-delete-disabled="isDeleteDisabled"
            @page-change="onPageChange"
            @edit="openEdit"
            @reset-password="openResetPasswordDialog"
            @delete="removeRow"
            @toggle-status="toggleStatus"
          />
        </div>
      </div>
    </t-card>

    <user-form-dialog
      v-model:visible="drawerVisible"
      v-model:area-value="areaValue"
      :title="drawerTitle"
      :mode="mode"
      :form="form"
      :rules="rules"
      :saving="saving"
      :gender-options="genderOptions"
      :document-type-options="documentTypeOptions"
      :role-options="roleOptions"
      :status-radio-options="statusRadioOptions"
      :org-unit-tree="orgUnitTree"
      :department-tree="departmentTree"
      :org-tree-keys="userOrgTreeKeys"
      :area-options="areaOptions"
      :area-loading="areaLoading"
      :password-help="passwordHelp"
      :password-placeholder="passwordPlaceholder"
      :document-no-placeholder="documentNoPlaceholder"
      @cancel="drawerVisible = false"
      @confirm="submitForm"
      @area-change="handleAreaChange"
    />

    <user-reset-password-dialog
      v-model:visible="resetDialogVisible"
      :form="resetPasswordForm"
      :rules="resetRules"
      :submitting="resetSubmitting"
      :password-help="resetPasswordHelp"
      :password-placeholder="resetPasswordPlaceholder"
      @cancel="closeResetDialog"
      @confirm="submitResetPassword"
    />
  </div>
</template>

<script setup lang="ts">
import type { PageInfo, SelectOption } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, watch } from 'vue';

import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions } from '@/utils/dict';
import { request } from '@/utils/request';

import { userOrgTreeKeys, userStatusFallbackOptions } from '../constants/userOptions';
import { normalizeDocumentType, resolveDocumentNoPlaceholder } from '../helpers';
import { useUserArea } from '../hooks/useUserArea';
import { useUserBatchActions } from '../hooks/useUserBatchActions';
import { useUserDialog } from '../hooks/useUserDialog';
import { useUserForm } from '../hooks/useUserForm';
import { useUserOrgFilter } from '../hooks/useUserOrgFilter';
import { useUserPageState } from '../hooks/useUserPageState';
import { useUserPermissions } from '../hooks/useUserPermissions';
import { useUserSearchForm } from '../hooks/useUserSearchForm';
import { useUserTableColumns } from '../hooks/useUserTableColumns';
import { resetUserFormModel } from '../schema/formSchema';
import type { OrgUnitNode, PageResult, PasswordPolicy, RoleOption, UserRow } from '../types';
import { canDeleteRow, canEditRow, canResetPasswordForRow } from '../utils/userGuards';
import { applyUserRowToForm, createUserSubmitPayload, normalizeGenderValue } from '../utils/userMappers';
import UserFormDialog from './UserFormDialog.vue';
import UserOrgTree from './UserOrgTree.vue';
import UserResetPasswordDialog from './UserResetPasswordDialog.vue';
import UserTable from './UserTable.vue';
import UserToolbar from './UserToolbar.vue';

const {
  rows,
  roles,
  orgTree,
  filteredOrgTree,
  orgKeyword,
  selectedOrgUnitId,
  selectedDepartmentId,
  expandedOrgIds,
  loading,
  saving,
  pagination,
  orgUnitTree,
  departmentTree,
  orgSelectableIds,
  departmentSelectableIds,
  applyOrgTree,
} = useUserPageState();
const { filters, resetFilters } = useUserSearchForm();
const columns = useUserTableColumns();
const { drawerVisible, mode, editingId, resetDialogVisible, resetSubmitting, resetTarget, drawerTitle } = useUserDialog();
const { currentUserId, canQuery, canCreate, canUpdate, canDelete, canReset } = useUserPermissions();
const { removeUsers, updateUsersStatus } = useUserBatchActions();

const statusDict = useDictionary('user_status');
const genderDict = useDictionary('gender');
const areaDict = useDictionary('address_district');
const documentTypeDict = useDictionary('id_document_type');

const statusOptions = computed(() => buildDictOptions(statusDict.items.value, userStatusFallbackOptions, [1, 0]));
const statusRadioOptions = computed(
  () =>
    statusOptions.value.filter((opt) => opt && typeof opt === 'object' && 'value' in opt && 'label' in opt) as Array<{
      label: string;
      value: string | number | boolean;
      disabled?: boolean;
    }>,
);
const genderOptions = computed(() => buildDictOptions(genderDict.items.value));
const documentTypeOptions = computed(() => buildDictOptions(documentTypeDict.items.value));
const roleOptions = computed<RoleOption[]>(() => (roles.value || []).map((role) => ({ label: role.name, value: role.name })));

const passwordPolicy = reactive<PasswordPolicy>({
  minLength: 6,
  requireUppercase: false,
  requireLowercase: false,
  requireSpecial: false,
  allowSequential: true,
});

const { form, resetPasswordForm, rules, resetRules, passwordHelp, passwordPlaceholder, resetPasswordHelp, resetPasswordPlaceholder } =
  useUserForm(mode, computed(() => passwordPolicy));
const documentNoPlaceholder = computed(() => resolveDocumentNoPlaceholder(form.idType));

const { areaOptions, areaValue, areaLoading, loadRootAreas, resetAreaFields, syncAreaFromUser, handleAreaChange } = useUserArea(
  form,
  areaDict.items,
  areaDict.loading,
  '地址字典未完善，请先在系统字典中完善 address_district 的 province/city/district 字段',
);
const { filterOrgTree, handleOrgSelect } = useUserOrgFilter({
  orgTree,
  filteredOrgTree,
  orgKeyword,
  selectedOrgUnitId,
  selectedDepartmentId,
  onSelectionChanged: () => {
    pagination.current = 1;
    reload();
  },
});

const syncOrgSelection = () => {
  if (orgTree.value.length === 0 || form.orgUnitIds.length === 0) return;
  const next = form.orgUnitIds.filter((id) => orgSelectableIds.value.has(id));
  if (next.length !== form.orgUnitIds.length) form.orgUnitIds = next;
};

const syncDepartmentSelection = () => {
  if (orgTree.value.length === 0 || form.departmentIds.length === 0) return;
  const next = form.departmentIds.filter((id) => departmentSelectableIds.value.has(id));
  if (next.length !== form.departmentIds.length) form.departmentIds = next;
};

watch(orgSelectableIds, syncOrgSelection, { immediate: true });
watch(departmentSelectableIds, syncDepartmentSelection, { immediate: true });
watch(
  () => form.orgUnitIds,
  () => {
    if (form.orgUnitIds.length > 0 && form.departmentIds.length > 0) form.departmentIds = [];
    syncOrgSelection();
    syncDepartmentSelection();
  },
  { deep: true },
);
watch(
  () => form.departmentIds,
  () => {
    if (form.departmentIds.length > 0 && form.orgUnitIds.length > 0) form.departmentIds = [];
    syncOrgSelection();
    syncDepartmentSelection();
  },
  { deep: true },
);

const loadDictionaries = async (force = false) =>
  Promise.all([genderDict.load(force), areaDict.load(force), documentTypeDict.load(force)]);

const loadRoles = async () => {
  roles.value = await request.get({ url: '/system/role/list' });
};

const loadOrgTree = async () => {
  const data = await request.get<OrgUnitNode[]>({ url: '/system/org/tree' });
  applyOrgTree(data);
};

const loadPasswordPolicy = async () => {
  try {
    const settings = await request.get<
      Partial<PasswordPolicy> & {
        passwordMinLength?: number;
        passwordRequireUppercase?: boolean;
        passwordRequireLowercase?: boolean;
        passwordRequireSpecial?: boolean;
        passwordAllowSequential?: boolean;
      }
    >({ url: '/system/ui' });
    if (!settings) return;
    passwordPolicy.minLength = settings.minLength ?? settings.passwordMinLength ?? 6;
    passwordPolicy.requireUppercase = settings.requireUppercase ?? settings.passwordRequireUppercase ?? false;
    passwordPolicy.requireLowercase = settings.requireLowercase ?? settings.passwordRequireLowercase ?? false;
    passwordPolicy.requireSpecial = settings.requireSpecial ?? settings.passwordRequireSpecial ?? false;
    passwordPolicy.allowSequential = settings.allowSequential ?? settings.passwordAllowSequential ?? true;
  } catch {
    MessagePlugin.warning('密码策略加载失败，已使用默认策略');
  }
};

const reload = async () => {
  if (!canQuery.value) {
    rows.value = [];
    pagination.total = 0;
    return;
  }
  loading.value = true;
  try {
    const [startDate, endDate] = filters.createdRange || [];
    const response = await request.get<PageResult<UserRow>>({
      url: '/system/user/page',
      params: {
        keyword: filters.keyword || undefined,
        mobile: filters.mobile || undefined,
        orgUnitId: selectedOrgUnitId.value || undefined,
        departmentId: selectedDepartmentId.value || undefined,
        status: filters.status ?? undefined,
        startTime: startDate ? `${startDate} 00:00:00` : undefined,
        endTime: endDate ? `${endDate} 23:59:59` : undefined,
        page: pagination.current - 1,
        size: pagination.pageSize,
      },
    });
    rows.value = response.list;
    pagination.total = response.total;
  } finally {
    loading.value = false;
  }
};

const resetFiltersAndReload = () => {
  resetFilters();
  selectedOrgUnitId.value = null;
  selectedDepartmentId.value = null;
  pagination.current = 1;
  reload();
};

const openCreate = async () => {
  if (!canCreate.value) return MessagePlugin.warning('无创建权限');
  mode.value = 'create';
  editingId.value = null;
  resetUserFormModel(form);
  resetAreaFields();
  drawerVisible.value = true;
  await loadDictionaries(true);
  await loadRootAreas(false);
};

const openEdit = async (row: UserRow) => {
  if (!canUpdate.value) return MessagePlugin.warning('无编辑权限');
  if (isEditDisabled(row)) return;
  mode.value = 'edit';
  editingId.value = row.id;
  resetUserFormModel(form);
  resetAreaFields();
  applyUserRowToForm(form, row, normalizeGenderValue(row.gender, genderDict.items.value.some((item) => item.value === 'unknown')));
  drawerVisible.value = true;
  try {
    await loadDictionaries(true);
    const detail = await request.get<UserRow>({ url: `/system/user/${row.id}` });
    applyUserRowToForm(form, detail, normalizeGenderValue(detail.gender, genderDict.items.value.some((item) => item.value === 'unknown')));
    await syncAreaFromUser(detail);
  } catch {
    MessagePlugin.error('加载用户详情失败');
  }
};

const submitForm = async () => {
  saving.value = true;
  try {
    if (mode.value === 'create') {
      await request.post({ url: '/system/user', data: createUserSubmitPayload(form, true) });
      MessagePlugin.success('创建成功');
    } else {
      await request.put({ url: `/system/user/${editingId.value}`, data: createUserSubmitPayload(form, false) });
      MessagePlugin.success('保存成功');
    }
    drawerVisible.value = false;
    await reload();
  } finally {
    saving.value = false;
  }
};

const openResetPasswordDialog = (row: UserRow) => {
  if (!canReset.value) return MessagePlugin.warning('无重置权限');
  if (isResetDisabled(row)) return;
  resetTarget.value = row;
  resetPasswordForm.password = '';
  resetDialogVisible.value = true;
};

const closeResetDialog = () => {
  resetDialogVisible.value = false;
  resetPasswordForm.password = '';
  resetTarget.value = null;
};

const submitResetPassword = async () => {
  if (!resetTarget.value) return;
  resetSubmitting.value = true;
  try {
    const payload = resetPasswordForm.password ? { password: resetPasswordForm.password } : undefined;
    await request.post({ url: `/system/user/${resetTarget.value.id}/reset-password`, data: payload });
    MessagePlugin.success('已重置');
    closeResetDialog();
  } finally {
    resetSubmitting.value = false;
  }
};

const removeRow = (row: UserRow) => {
  if (!canDelete.value) return MessagePlugin.warning('无删除权限');
  if (isDeleteDisabled(row)) return;
  const dialog = DialogPlugin.confirm({
    header: '删除用户',
    body: `确认删除用户「${row.account}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await removeUsers([row.id]);
      MessagePlugin.success('已删除');
      dialog.hide();
      await reload();
    },
  });
};

const toggleStatus = async (row: UserRow, enabled: boolean) => {
  if (!canUpdate.value) return;
  const targetStatus = enabled ? 1 : 0;
  await updateUsersStatus([row.id], targetStatus);
  row.status = targetStatus;
};

const onPageChange = (pageInfo: PageInfo) => {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  reload();
};

const isEditDisabled = (row: UserRow) => !canEditRow(row);
const isResetDisabled = (row: UserRow) => !canResetPasswordForRow(row, currentUserId.value);
const isDeleteDisabled = (row: UserRow) => !canDeleteRow(row, currentUserId.value);

onMounted(async () => {
  void statusDict.load();
  await loadDictionaries();
  await loadRoles();
  await loadOrgTree();
  await loadPasswordPolicy();
  await reload();
});
</script>
<style scoped lang="less" src="./UserPageMain.less"></style>
