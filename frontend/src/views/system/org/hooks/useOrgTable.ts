import type { Ref } from 'vue';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';

import { request } from '@/utils/request';

import type { OrgSearchForm, OrgUnitNode } from '../types';
import { buildOrgReorderItems } from '../utils/orgTree';

interface UseOrgTableOptions {
  tableRef: Ref<{ getTreeNode?: () => OrgUnitNode[] } | null>;
  orgTree: Ref<OrgUnitNode[]>;
  filteredTree: Ref<OrgUnitNode[]>;
  filters: OrgSearchForm;
  dirty: Ref<boolean>;
  savingOrder: Ref<boolean>;
  onReload: () => Promise<void>;
}

export const useOrgTable = (options: UseOrgTableOptions) => {
  const { tableRef, orgTree, filteredTree, filters, dirty, savingOrder, onReload } = options;

  const beforeDragSort = () => {
    if (filters.keyword.trim() || filters.status != null) {
      MessagePlugin.warning('请先清空筛选条件，再进行拖拽排序');
      return false;
    }
    return true;
  };

  const onDragSort = (context: { newData?: OrgUnitNode[] }) => {
    dirty.value = true;
    if (context?.newData) {
      const tree = tableRef.value?.getTreeNode?.();
      if (tree) {
        orgTree.value = tree;
        filteredTree.value = tree;
      }
    }
  };

  const onAbnormalDragSort = (params: { code?: number; reason?: string }) => {
    if (params?.code === 1001) {
      MessagePlugin.warning('不同层级的元素，不允许直接拖拽排序');
      return;
    }
    if (params?.reason) MessagePlugin.warning(String(params.reason));
  };

  const saveOrder = async () => {
    const tree = tableRef.value?.getTreeNode?.() || orgTree.value;
    savingOrder.value = true;
    try {
      await request.put({ url: '/system/org/reorder', data: { items: buildOrgReorderItems(tree) } });
      MessagePlugin.success('排序已保存');
      dirty.value = false;
      await onReload();
    } finally {
      savingOrder.value = false;
    }
  };

  const removeRow = (row: OrgUnitNode) => {
    const dialog = DialogPlugin.confirm({
      header: '删除机构',
      body: `确认删除机构「${row.name}」？`,
      theme: 'danger',
      confirmBtn: '删除',
      onConfirm: async () => {
        await request.delete({ url: `/system/org/${row.id}` });
        MessagePlugin.success('已删除');
        dialog.hide();
        await onReload();
      },
    });
  };

  return {
    beforeDragSort,
    onDragSort,
    onAbnormalDragSort,
    saveOrder,
    removeRow,
  };
};
