import { MoveIcon } from 'tdesign-icons-vue-next';
import { computed, type Ref } from 'vue';

import { resolveLabel } from '@/utils/dict';

import { menuTypeLabelMap } from '../constants/menuOptions';
import type { MenuNode } from '../types';
import { computeFullPath } from '../utils/menuTree';

interface UseMenuColumnsInput {
  data: Ref<MenuNode[]>;
  nodeTypeItems: Ref<unknown[]>;
  canCreate: Ref<boolean>;
  canUpdate: Ref<boolean>;
  canDelete: Ref<boolean>;
  rowSaving: Ref<Record<number, boolean>>;
  onCreateChild: (row: MenuNode, nodeType: 'DIR' | 'PAGE' | 'BTN') => void;
  onEdit: (row: MenuNode) => void;
  onMove: (row: MenuNode) => void;
  onDelete: (row: MenuNode) => void;
  onToggleHidden: (row: MenuNode, hidden: boolean) => void;
}

export const useMenuColumns = ({
  data,
  nodeTypeItems,
  canCreate,
  canUpdate,
  canDelete,
  rowSaving,
  onCreateChild,
  onEdit,
  onMove,
  onDelete,
  onToggleHidden,
}: UseMenuColumnsInput) => {
  const getOpenTypeLabel = (row: MenuNode) => {
    if (row.nodeType === 'DIR') return '目录';
    if (row.nodeType === 'BTN') return '按钮';
    if (row.frameSrc) return row.frameBlank ? '外链' : '内嵌';
    return '内部';
  };

  return computed(
    () =>
      [
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
          cell: (_h: unknown, { row }: { row: MenuNode }) => (
            <t-space size="small" align="center">
              {row.icon ? <t-icon name={row.icon} /> : null}
              <span style={{ fontWeight: 500 }}>{row.titleZhCn}</span>
              <t-tag theme={row.nodeType === 'DIR' ? 'default' : row.nodeType === 'BTN' ? 'warning' : 'primary'} variant="light">
                {resolveLabel(row.nodeType, nodeTypeItems.value as never[], menuTypeLabelMap as Record<string, string>)}
              </t-tag>
              {row.hidden ? (
                <t-tag theme="warning" variant="light">
                  隐藏
                </t-tag>
              ) : null}
            </t-space>
          ),
        },
        {
          colKey: 'fullPath',
          title: '路由Path',
          width: 200,
          ellipsis: true,
          cell: (_h: unknown, { row }: { row: MenuNode }) => (
            <span style={{ fontFamily: 'monospace' }}>{computeFullPath(row, data.value)}</span>
          ),
        },
        {
          colKey: 'component',
          title: '打开/组件/链接',
          width: 220,
          ellipsis: true,
          cell: (_h: unknown, { row }: { row: MenuNode }) => {
            const content = row.nodeType === 'PAGE' ? (row.frameSrc ? row.frameSrc : row.component || '-') : row.component || 'LAYOUT';
            return (
              <t-space size="small">
                <t-tag theme="default" variant="light">
                  {getOpenTypeLabel(row)}
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
          cell: (_h: unknown, { row }: { row: MenuNode }) => (
            <t-switch
              size="small"
              disabled={!canUpdate.value || !!rowSaving.value[row.id]}
              modelValue={!!row.hidden}
              onChange={(value: unknown) => onToggleHidden(row, !!value)}
            />
          ),
        },
        {
          colKey: 'operate',
          title: '操作',
          width: 280,
          className: 'menu-table__operate-cell',
          cell: (_h: unknown, { row }: { row: MenuNode }) => (
            <div class="menu-table__operate-links">
              {canCreate.value ? (
                <t-link theme="primary" disabled={row.nodeType !== 'DIR'} onClick={() => onCreateChild(row, 'DIR')}>
                  +目录
                </t-link>
              ) : null}
              {canCreate.value ? (
                <t-link theme="primary" disabled={row.nodeType !== 'DIR'} onClick={() => onCreateChild(row, 'PAGE')}>
                  +页面
                </t-link>
              ) : null}
              {canCreate.value ? (
                <t-link theme="primary" disabled={row.nodeType === 'BTN'} onClick={() => onCreateChild(row, 'BTN')}>
                  +按钮
                </t-link>
              ) : null}
              {canUpdate.value ? (
                <t-link theme="primary" onClick={() => onEdit(row)}>
                  编辑
                </t-link>
              ) : null}
              {canUpdate.value ? (
                <t-link theme="primary" disabled={row.nodeType !== 'PAGE' || row.parentId == null} onClick={() => onMove(row)}>
                  移动
                </t-link>
              ) : null}
              {canDelete.value ? (
                <t-link theme="danger" onClick={() => onDelete(row)}>
                  删除
                </t-link>
              ) : null}
            </div>
          ),
        },
      ],
  );
};
