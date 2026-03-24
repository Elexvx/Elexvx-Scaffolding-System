<template>
  <div class="org-management">
    <t-card title="机构管理" :bordered="false">
      <org-toolbar
        v-model:filters="filters"
        :expand-all="expandAll"
        :dirty="dirty"
        :saving-order="savingOrder"
        :status-options="statusOptions"
        @search="handleSearch"
        @reset="handleReset"
        @create="openCreate"
        @toggle-expand="toggleExpand"
        @save-order="saveOrder"
      />

      <org-table
        :data="filteredTree"
        :columns="columns"
        :loading="loading"
        :tree-config="orgTableTreeConfig"
        :expanded-tree-nodes="expandedTreeNodes"
        :before-drag-sort="beforeDragSort"
        @update:expanded-tree-nodes="expandedTreeNodes = $event"
        @table-ready="tableRef = $event"
        @abnormal-drag-sort="onAbnormalDragSort"
        @drag-sort="onDragSort"
        @create="openCreate"
        @edit="openEdit"
        @delete="removeRow"
      />
    </t-card>

    <org-form-dialog
      :visible="dialogVisible"
      :title="dialogTitle"
      :form="form"
      :rules="rules"
      :org-tree="orgTree"
      :org-tree-keys="orgTreeKeys"
      :type-options="typeOptions"
      :editing-id="editingId"
      :leader-display="leaderDisplay"
      :member-display="memberDisplay"
      :saving="saving"
      @update:visible="dialogVisible = $event"
      @submit="onSubmit"
      @open-leader-dialog="openLeaderDialog"
      @open-member-dialog="openMemberDialog"
    />

    <org-user-selector-dialog
      :visible="leaderDialogVisible"
      title="用户选择"
      :keyword="leaderFilters.keyword"
      :org-keyword="leaderFilters.orgKeyword"
      :tree-data="leaderFilteredTree"
      :tree-keys="orgTreeKeys"
      :rows="leaderRows"
      :columns="userSelectColumns"
      :pagination="leaderPagination"
      :selection="leaderSelection"
      confirm-text="确定"
      @update:visible="leaderDialogVisible = $event"
      @update:keyword="leaderFilters.keyword = $event"
      @update:org-keyword="leaderFilters.orgKeyword = $event"
      @search="loadLeaders"
      @reset="resetLeaderFilters"
      @org-select="handleLeaderOrgSelect"
      @select-change="handleLeaderSelectChange"
      @page-change="onLeaderPageChange"
      @remove="removeLeader"
      @confirm="confirmLeaderSelection"
    />

    <org-user-selector-dialog
      :visible="memberDialogVisible"
      title="选择用户加入机构"
      :keyword="memberFilters.keyword"
      :org-keyword="memberFilters.orgKeyword"
      :tree-data="memberFilteredTree"
      :tree-keys="orgTreeKeys"
      :rows="memberRows"
      :columns="userSelectColumns"
      :pagination="memberPagination"
      :selection="memberSelection"
      confirm-text="加入机构"
      :confirm-loading="addingMembers"
      @update:visible="memberDialogVisible = $event"
      @update:keyword="memberFilters.keyword = $event"
      @update:org-keyword="memberFilters.orgKeyword = $event"
      @search="loadMembers"
      @reset="resetMemberFilters"
      @org-select="handleMemberOrgSelect"
      @select-change="handleMemberSelectChange"
      @page-change="onMemberPageChange"
      @remove="removeMember"
      @confirm="confirmMemberSelection"
    />
  </div>
</template>

<script setup lang="ts">
import type { SelectOption } from 'tdesign-vue-next';
import { computed, onMounted } from 'vue';

import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions } from '@/utils/dict';

import { fallbackStatusOptions, fallbackTypeOptions, orgTableTreeConfig, orgTreeKeys, orgTypeValues, userSelectColumns } from '../constants/orgOptions';
import { useOrgColumns } from '../hooks/useOrgColumns';
import { useOrgDialog } from '../hooks/useOrgDialog';
import { useOrgForm } from '../hooks/useOrgForm';
import { useOrgPageState } from '../hooks/useOrgPageState';
import { useOrgPermissions } from '../hooks/useOrgPermissions';
import { useOrgSearchForm } from '../hooks/useOrgSearchForm';
import { useOrgTable } from '../hooks/useOrgTable';
import { useOrgTree } from '../hooks/useOrgTree';
import { applyOrgFilters } from '../utils/orgTree';
import OrgFormDialog from './OrgFormDialog.vue';
import OrgTable from './OrgTable.vue';
import OrgToolbar from './OrgToolbar.vue';
import OrgUserSelectorDialog from './OrgUserSelectorDialog.vue';

const { canQuery } = useOrgPermissions();
const { filters, resetFilters } = useOrgSearchForm();
const { tableRef, loading, saving, savingOrder, addingMembers, dirty, expandAll, expandedTreeNodes, orgTree, filteredTree } = useOrgPageState();
const { dialogVisible, leaderDialogVisible, memberDialogVisible, editingId, dialogTitle } = useOrgDialog();

const statusDict = useDictionary('org_status');
const typeDict = useDictionary('org_type');

const statusOptions = computed<SelectOption[]>(() => buildDictOptions(statusDict.items.value, fallbackStatusOptions, [1, 0]));
const typeOptions = computed<SelectOption[]>(() => buildDictOptions(typeDict.items.value, fallbackTypeOptions, orgTypeValues));

const { reload } = useOrgTree({
  loading,
  expandAll,
  tableRef,
  orgTree,
  filteredTree,
  filters,
  dirty,
});

const {
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
} = useOrgForm({
  editingId,
  dialogVisible,
  leaderDialogVisible,
  memberDialogVisible,
  saving,
  addingMembers,
  orgTree,
  onReload: reload,
});

const { beforeDragSort, onDragSort, onAbnormalDragSort, saveOrder, removeRow } = useOrgTable({
  tableRef,
  orgTree,
  filteredTree,
  filters,
  dirty,
  savingOrder,
  onReload: reload,
});

const columns = useOrgColumns();

const handleSearch = () => {
  filteredTree.value = applyOrgFilters(orgTree.value, filters);
};

const handleReset = () => {
  resetFilters();
  filteredTree.value = [...orgTree.value];
};

const toggleExpand = () => {
  expandAll.value = !expandAll.value;
  if (expandAll.value) {
    tableRef.value?.expandAll?.();
  } else {
    tableRef.value?.foldAll?.();
  }
};

onMounted(async () => {
  if (!canQuery.value) return;
  void statusDict.load();
  void typeDict.load();
  await reload();
});
</script>
<style scoped lang="less" src="./OrgPageMain.less"></style>
