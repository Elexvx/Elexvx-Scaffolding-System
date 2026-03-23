import { ProList, ProTable } from '@ant-design/pro-components';
import { App, Grid } from 'antd';
import type { ProColumns } from '@ant-design/pro-components';
import type { ReactNode } from 'react';

interface ResponsiveListTableProps<T extends object> {
  rowKey: string;
  columns: ProColumns<T>[];
  request: (params: Record<string, unknown>) => Promise<{ data: T[]; success: boolean; total: number }>;
  cardTitle: (item: T) => ReactNode;
  cardDescription: (item: T) => ReactNode;
}

export function ResponsiveListTable<T extends object>({
  rowKey,
  columns,
  request,
  cardTitle,
  cardDescription,
}: ResponsiveListTableProps<T>) {
  const screens = Grid.useBreakpoint();
  const { message } = App.useApp();
  const isMobile = !screens.md;

  if (isMobile) {
    return (
      <ProList<T>
        rowKey={rowKey}
        pagination={{ pageSize: 10 }}
        request={async (params) => {
          try {
            return await request(params);
          } catch (error) {
            message.error(error instanceof Error ? error.message : '加载失败');
            return { data: [], success: false, total: 0 };
          }
        }}
        metas={{
          title: {
            render: (_, row) => cardTitle(row),
          },
          description: {
            render: (_, row) => cardDescription(row),
          },
        }}
      />
    );
  }

  return <ProTable<T> rowKey={rowKey} columns={columns} request={request} search={false} />;
}
