<template>
  <t-card title="订单管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space>
        <t-input placeholder="请输入订单号" style="width: 200px" />
        <t-date-range-picker style="width: 300px" />
        <t-button theme="primary">查询</t-button>
        <t-button variant="outline">重置</t-button>
      </t-space>

      <t-table row-key="id" :data="data" :columns="columns" :pagination="pagination" bordered>
        <template #status="{ row }">
          <t-tag v-if="row.status === 'completed'" theme="success" variant="light">已完成</t-tag>
          <t-tag v-else-if="row.status === 'pending'" theme="warning" variant="light">待付款</t-tag>
          <t-tag v-else theme="default" variant="light">已取消</t-tag>
        </template>
        <template #op>
          <t-space>
            <t-link theme="primary">查看详情</t-link>
          </t-space>
        </template>
      </t-table>
    </t-space>
  </t-card>
</template>
<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { reactive, ref } from 'vue';

const columns: PrimaryTableCol[] = [
  { colKey: 'orderNo', title: '订单号', width: 180 },
  { colKey: 'customer', title: '客户', width: 120 },
  { colKey: 'amount', title: '总金额', width: 120 },
  { colKey: 'createTime', title: '创建时间', width: 180 },
  { colKey: 'status', title: '状态', width: 100, cell: 'status' },
  { colKey: 'op', title: '操作', width: 100, fixed: 'right' },
];

const data = ref([
  {
    id: 1,
    orderNo: 'ORD20231001001',
    customer: '张三',
    amount: '¥7999.00',
    createTime: '2023-10-01 12:00:00',
    status: 'completed',
  },
  {
    id: 2,
    orderNo: 'ORD20231001002',
    customer: '李四',
    amount: '¥299.00',
    createTime: '2023-10-01 12:30:00',
    status: 'pending',
  },
  {
    id: 3,
    orderNo: 'ORD20231001003',
    customer: '王五',
    amount: '¥4799.00',
    createTime: '2023-10-01 13:00:00',
    status: 'completed',
  },
  {
    id: 4,
    orderNo: 'ORD20231001004',
    customer: '赵六',
    amount: '¥199.00',
    createTime: '2023-10-01 14:00:00',
    status: 'cancelled',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 4,
});
</script>
