<template>
  <t-card title="商品管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space>
        <t-input placeholder="请输入商品名称" style="width: 200px" />
        <t-button theme="primary">查询</t-button>
        <t-button variant="outline">重置</t-button>
        <t-button theme="primary" variant="dashed">新增商品</t-button>
      </t-space>

      <t-table row-key="id" :data="data" :columns="columns" :pagination="pagination" bordered>
        <template #status="{ row }">
          <t-tag v-if="row.status === 1" theme="success" variant="light">上架</t-tag>
          <t-tag v-else theme="danger" variant="light">下架</t-tag>
        </template>
        <template #op="{ rowIndex, row }">
          <t-space break-line>
            <t-link theme="primary">编辑</t-link>
            <t-link theme="danger" @click="handleDelete(rowIndex, row)">删除</t-link>
          </t-space>
        </template>
      </t-table>
    </t-space>
  </t-card>
</template>
<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { reactive, ref } from 'vue';

const columns: PrimaryTableCol[] = [
  { colKey: 'id', title: 'ID', width: 80 },
  { colKey: 'name', title: '商品名称', width: 200 },
  { colKey: 'price', title: '价格', width: 120 },
  { colKey: 'stock', title: '库存', width: 100 },
  { colKey: 'status', title: '状态', width: 100, cell: 'status' },
  { colKey: 'op', title: '操作', width: 150, fixed: 'right' },
];

const data = ref([
  { id: 1, name: 'iPhone 15 Pro', price: '¥7999', stock: 100, status: 1 },
  { id: 2, name: 'MacBook Air M2', price: '¥8999', stock: 50, status: 1 },
  { id: 3, name: 'AirPods Pro', price: '¥1899', stock: 200, status: 0 },
  { id: 4, name: 'iPad Air', price: '¥4799', stock: 80, status: 1 },
  { id: 5, name: 'Apple Watch S9', price: '¥2999', stock: 120, status: 1 },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 5,
});

const handleDelete = (rowIndex: number, row: { name: string }) => {
  const dialog = DialogPlugin.confirm({
    header: '确认删除',
    body: `确认删除「${row.name}」吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    onConfirm: () => {
      dialog.hide();
      data.value.splice(rowIndex, 1);
      pagination.total = data.value.length;
      MessagePlugin.success('已删除');
    },
  });
};
</script>
