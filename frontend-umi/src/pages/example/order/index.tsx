import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';

type OrderRow = {
  id: number;
  orderNo: string;
  customer: string;
  amount: number;
  status: 'PAID' | 'PENDING';
};

const mockData: OrderRow[] = [
  { id: 1, orderNo: 'ORD-20260324-001', customer: '内部测试用户', amount: 499, status: 'PAID' },
  { id: 2, orderNo: 'ORD-20260324-002', customer: '演示客户', amount: 99, status: 'PENDING' },
];

const columns: ProColumns<OrderRow>[] = [
  { title: '订单号', dataIndex: 'orderNo' },
  { title: '客户', dataIndex: 'customer' },
  { title: '金额', dataIndex: 'amount', render: (_, row) => `¥${row.amount}` },
  { title: '状态', dataIndex: 'status', render: (_, row) => <Tag color={row.status === 'PAID' ? 'green' : 'orange'}>{row.status === 'PAID' ? '已支付' : '待支付'}</Tag> },
];

export default function ExampleOrderPage() {
  return (
    <PageScaffold title="示例订单页">
      <ResponsiveListTable<OrderRow>
        rowKey="id"
        columns={columns}
        request={async () => ({ data: mockData, total: mockData.length, success: true })}
        cardTitle={(item) => item.orderNo}
        cardDescription={(item) => `${item.customer} ｜ ¥${item.amount}`}
      />
    </PageScaffold>
  );
}
