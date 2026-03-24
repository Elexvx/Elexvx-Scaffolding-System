import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';

type GoodsRow = {
  id: number;
  name: string;
  category: string;
  price: number;
  status: 'ON' | 'OFF';
};

const mockData: GoodsRow[] = [
  { id: 1, name: '示例商品-A', category: '演示', price: 199, status: 'ON' },
  { id: 2, name: '示例商品-B', category: '模板', price: 299, status: 'OFF' },
];

const columns: ProColumns<GoodsRow>[] = [
  { title: '商品名称', dataIndex: 'name' },
  { title: '分类', dataIndex: 'category' },
  { title: '价格', dataIndex: 'price', render: (_, row) => `¥${row.price}` },
  { title: '状态', dataIndex: 'status', render: (_, row) => <Tag color={row.status === 'ON' ? 'green' : 'default'}>{row.status === 'ON' ? '上架' : '下架'}</Tag> },
];

export default function ExampleGoodsPage() {
  return (
    <PageScaffold title="示例商品页">
      <ResponsiveListTable<GoodsRow>
        rowKey="id"
        columns={columns}
        request={async () => ({ data: mockData, total: mockData.length, success: true })}
        cardTitle={(item) => item.name}
        cardDescription={(item) => `${item.category} ｜ ¥${item.price}`}
      />
    </PageScaffold>
  );
}
