import type { PageInfo, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref, type Ref } from 'vue';

import { request } from '@/utils/request';

import { createOrgFormModel, orgFormRules, resetOrgFormModel } from '../schema/orgFormSchema';
import type { OrgUnitNode, PageResult, UserRow } from '../types';
import { applyOrgRowToForm, createOrgSubmitPayload, mapSelectedUsers } from '../utils/orgMappers';
import { resolveOrgFilterIds } from '../utils/orgGuards';
import { filterOrgTreeByKeyword } from '../utils/orgTree';

interface UseOrgFormOptions {
  editingId: Ref<number | null>;
  dialogVisible: Ref<boolean>;
  leaderDialogVisible: Ref<boolean>;
  memberDialogVisible: Ref<boolean>;
  saving: Ref<boolean>;
  addingMembers: Ref<boolean>;
  orgTree: Ref<OrgUnitNode[]>;
  onReload: () => Promise<void>;
}

const resolveTreeNode = (context: { node?: unknown }) => {
  const node = context?.node as { data?: OrgUnitNode; getModel?: () => OrgUnitNode; id?: number; value?: number } | undefined;
  if (!node) return null;
  const rawNode = node.data || node.getModel?.() || node;
  const rawId = rawNode?.id ?? node.value ?? node.id;
  const nodeId = Number(rawId);
  if (Number.isNaN(nodeId)) return null;
  return { ...rawNode, id: nodeId } as OrgUnitNode;
};

export const useOrgForm = (options: UseOrgFormOptions) => {
  const { editingId, dialogVisible, leaderDialogVisible, memberDialogVisible, saving, addingMembers, orgTree, onReload } = options;

  const form = reactive(createOrgFormModel());
  const selectedLeaderNames = ref<string[]>([]);

  const leaderFilters = reactive({
    keyword: '',
    orgKeyword: '',
    orgUnitId: null as number | null,
    departmentId: null as number | null,
  });
  const leaderRows = ref<UserRow[]>([]);
  const leaderSelection = ref<UserRow[]>([]);
  const leaderPagination = reactive({ current: 1, pageSize: 8, total: 0 });

  const memberFilters = reactive({
    keyword: '',
    orgKeyword: '',
    orgUnitId: null as number | null,
    departmentId: null as number | null,
  });
  const memberRows = ref<UserRow[]>([]);
  const memberSelection = ref<UserRow[]>([]);
  const memberPagination = reactive({ current: 1, pageSize: 8, total: 0 });

  const rules = orgFormRules;
  const leaderDisplay = computed(() => (selectedLeaderNames.value.length > 0 ? selectedLeaderNames.value.join(' / ') : '-'));
  const memberDisplay = computed(() => (memberSelection.value.length ? `已选择 ${memberSelection.value.length} 人` : ''));

  const leaderFilteredTree = computed(() => {
    const keyword = leaderFilters.orgKeyword.trim();
    if (!keyword) return orgTree.value;
    return filterOrgTreeByKeyword(orgTree.value, keyword);
  });

  const memberFilteredTree = computed(() => {
    const keyword = memberFilters.orgKeyword.trim();
    if (!keyword) return orgTree.value;
    return filterOrgTreeByKeyword(orgTree.value, keyword);
  });

  const resetForm = () => {
    resetOrgFormModel(form);
    selectedLeaderNames.value = [];
    memberSelection.value = [];
  };

  const openCreate = (parent?: OrgUnitNode) => {
    editingId.value = null;
    resetForm();
    if (parent) form.parentId = parent.id;
    dialogVisible.value = true;
  };

  const openEdit = (row: OrgUnitNode) => {
    editingId.value = row.id;
    applyOrgRowToForm(form, row);
    selectedLeaderNames.value = [...(row.leaderNames || [])];
    memberSelection.value = [];
    dialogVisible.value = true;
  };

  const submitForm = async () => {
    saving.value = true;
    try {
      const payload = createOrgSubmitPayload(form);
      if (editingId.value) {
        await request.put({ url: `/system/org/${editingId.value}`, data: payload });
        MessagePlugin.success('保存成功');
      } else {
        await request.post({ url: '/system/org', data: payload });
        MessagePlugin.success('创建成功');
      }
      dialogVisible.value = false;
      await onReload();
    } finally {
      saving.value = false;
    }
  };

  const onSubmit = (context: SubmitContext) => {
    if (context.validateResult === true) submitForm();
  };

  const loadLeaders = async () => {
    const response = await request.get<PageResult<UserRow>>({
      url: '/system/user/page',
      params: {
        keyword: leaderFilters.keyword || undefined,
        orgUnitId: leaderFilters.orgUnitId || undefined,
        departmentId: leaderFilters.departmentId || undefined,
        page: leaderPagination.current - 1,
        size: leaderPagination.pageSize,
      },
    });
    leaderRows.value = response.list;
    leaderPagination.total = response.total;
  };

  const onLeaderPageChange = (pageInfo: PageInfo) => {
    leaderPagination.current = pageInfo.current;
    leaderPagination.pageSize = pageInfo.pageSize;
    loadLeaders();
  };

  const resetLeaderFilters = () => {
    leaderFilters.keyword = '';
    leaderFilters.orgKeyword = '';
    leaderFilters.orgUnitId = null;
    leaderFilters.departmentId = null;
    leaderPagination.current = 1;
    loadLeaders();
  };

  const handleLeaderOrgSelect = (context: { node?: unknown }) => {
    const targetNode = resolveTreeNode(context);
    if (!targetNode) return;
    const { orgUnitId, departmentId } = resolveOrgFilterIds(targetNode);
    leaderFilters.orgUnitId = orgUnitId;
    leaderFilters.departmentId = departmentId;
    leaderPagination.current = 1;
    loadLeaders();
  };

  const removeLeader = (id: number) => {
    leaderSelection.value = leaderSelection.value.filter((user) => user.id !== id);
  };

  const handleLeaderSelectChange = (selectedKeys: Array<string | number>, context: { selectedRowData?: UserRow[] }) => {
    const selectedIds = selectedKeys.map((key) => Number(key)).filter((key) => !Number.isNaN(key));
    if (context?.selectedRowData) {
      leaderSelection.value = context.selectedRowData;
      return;
    }
    leaderSelection.value = leaderRows.value.filter((row) => selectedIds.includes(row.id));
  };

  const openLeaderDialog = () => {
    leaderDialogVisible.value = true;
    leaderSelection.value = mapSelectedUsers(form.leaderIds, selectedLeaderNames.value);
    loadLeaders();
  };

  const confirmLeaderSelection = () => {
    form.leaderIds = leaderSelection.value.map((user) => user.id);
    selectedLeaderNames.value = leaderSelection.value.map((user) => user.name);
    leaderDialogVisible.value = false;
  };

  const loadMembers = async () => {
    const response = await request.get<PageResult<UserRow>>({
      url: '/system/user/page',
      params: {
        keyword: memberFilters.keyword || undefined,
        orgUnitId: memberFilters.orgUnitId || undefined,
        departmentId: memberFilters.departmentId || undefined,
        page: memberPagination.current - 1,
        size: memberPagination.pageSize,
      },
    });
    memberRows.value = response.list;
    memberPagination.total = response.total;
  };

  const onMemberPageChange = (pageInfo: PageInfo) => {
    memberPagination.current = pageInfo.current;
    memberPagination.pageSize = pageInfo.pageSize;
    loadMembers();
  };

  const resetMemberFilters = () => {
    memberFilters.keyword = '';
    memberFilters.orgKeyword = '';
    memberFilters.orgUnitId = null;
    memberFilters.departmentId = null;
    memberPagination.current = 1;
    loadMembers();
  };

  const handleMemberOrgSelect = (context: { node?: unknown }) => {
    const targetNode = resolveTreeNode(context);
    if (!targetNode) return;
    const { orgUnitId, departmentId } = resolveOrgFilterIds(targetNode);
    memberFilters.orgUnitId = orgUnitId;
    memberFilters.departmentId = departmentId;
    memberPagination.current = 1;
    loadMembers();
  };

  const removeMember = (id: number) => {
    memberSelection.value = memberSelection.value.filter((user) => user.id !== id);
  };

  const handleMemberSelectChange = (selectedKeys: Array<string | number>, context: { selectedRowData?: UserRow[] }) => {
    const selectedIds = selectedKeys.map((key) => Number(key)).filter((key) => !Number.isNaN(key));
    if (context?.selectedRowData) {
      memberSelection.value = context.selectedRowData;
      return;
    }
    memberSelection.value = memberRows.value.filter((row) => selectedIds.includes(row.id));
  };

  const openMemberDialog = () => {
    memberDialogVisible.value = true;
    memberSelection.value = [];
    memberPagination.current = 1;
    loadMembers();
  };

  const confirmMemberSelection = async () => {
    if (!editingId.value) return;
    if (memberSelection.value.length === 0) {
      MessagePlugin.warning('请选择用户');
      return;
    }
    addingMembers.value = true;
    try {
      await request.put({
        url: `/system/org/${editingId.value}/users`,
        data: { userIds: memberSelection.value.map((user) => user.id) },
      });
      MessagePlugin.success('已加入机构');
      memberDialogVisible.value = false;
      memberSelection.value = [];
      await onReload();
    } finally {
      addingMembers.value = false;
    }
  };

  return {
    form,
    rules,
    leaderFilters,
    leaderRows,
    leaderSelection,
    leaderPagination,
    memberFilters,
    memberRows,
    memberSelection,
    memberPagination,
    leaderDisplay,
    memberDisplay,
    leaderFilteredTree,
    memberFilteredTree,
    openCreate,
    openEdit,
    resetForm,
    submitForm,
    onSubmit,
    openLeaderDialog,
    loadLeaders,
    onLeaderPageChange,
    resetLeaderFilters,
    handleLeaderOrgSelect,
    removeLeader,
    handleLeaderSelectChange,
    confirmLeaderSelection,
    openMemberDialog,
    loadMembers,
    onMemberPageChange,
    resetMemberFilters,
    handleMemberOrgSelect,
    removeMember,
    handleMemberSelectChange,
    confirmMemberSelection,
  };
};
