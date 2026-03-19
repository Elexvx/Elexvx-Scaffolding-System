import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { ref, type Ref } from 'vue';

import type { MenuNode } from '../types';
import { findNodeById } from '../utils/menuTree';

interface UseMenuDragSortInput {
  canUpdate: Ref<boolean>;
  keyword: Ref<string>;
  data: Ref<MenuNode[]>;
  dirTreeData: Ref<MenuNode[]>;
  treeKeys: { value: string; label: string; children: string };
  onMovePage: (page: MenuNode, destDirId: number) => Promise<void>;
}

export const useMenuDragSort = ({
  canUpdate,
  keyword,
  data,
  dirTreeData,
  treeKeys,
  onMovePage,
}: UseMenuDragSortInput) => {
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
            onChange={(value: unknown) => {
              selected.value = value == null ? null : Number(value);
            }}
          />
          <div style={{ color: 'var(--td-text-color-secondary)', fontSize: '12px' }}>提示：拖拽跨目录时也会弹出此确认框</div>
        </t-space>
      ),
      onConfirm: async () => {
        const destinationId = selected.value;
        if (!destinationId || destinationId === row.parentId) {
          dialog.hide();
          MessagePlugin.success('未变更');
          return;
        }
        dialog.hide();
        await onMovePage(row, destinationId);
      },
    });
  };

  const beforeDragSort = (context: { current?: MenuNode; target?: MenuNode }) => {
    if (!canUpdate.value) {
      MessagePlugin.warning('无修改权限');
      return false;
    }
    const current = context?.current;
    const target = context?.target;
    if (!current || !target) return true;

    if (keyword.value.trim()) {
      MessagePlugin.warning('请先清空搜索条件，再进行拖拽排序/移动');
      return false;
    }
    if (current.parentId === target.parentId) return true;
    if (current.nodeType !== 'PAGE') {
      MessagePlugin.warning('跨目录移动请使用“移动”操作（当前仅支持移动页面节点）');
      return false;
    }
    if (current.parentId == null) {
      MessagePlugin.warning('根节点移动需要同时调整 path（根节点需以 / 开头），请使用编辑完成');
      return false;
    }
    const destinationId = target.nodeType === 'DIR' ? target.id : target.parentId;
    if (destinationId == null) {
      MessagePlugin.warning('不支持拖拽移动到根节点，请使用编辑完成');
      return false;
    }
    const destinationName = findNodeById(data.value, destinationId)?.titleZhCn || '目标目录';
    const dialog = DialogPlugin.confirm({
      header: '确认移动',
      body: `将「${current.titleZhCn}」移动到「${destinationName}」下？`,
      confirmBtn: '移动',
      cancelBtn: '取消',
      theme: 'warning',
      onConfirm: async () => {
        dialog.hide();
        await onMovePage(current, destinationId);
      },
      onClose: () => dialog.hide(),
    });
    return false;
  };

  const onAbnormalDragSort = (params: { code?: number; reason?: string }) => {
    if (params?.code === 1001) {
      MessagePlugin.warning('不同层级的元素，不允许直接拖拽排序');
      return;
    }
    if (params?.reason) {
      MessagePlugin.warning(String(params.reason));
    }
  };

  return {
    openMoveDialog,
    beforeDragSort,
    onAbnormalDragSort,
  };
};
