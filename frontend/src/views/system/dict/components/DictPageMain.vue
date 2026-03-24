<template>
  <div class="dict-page">
    <t-card title="字典管理" :bordered="false">
      <div class="table-action-bar">
        <div class="action-left">
          <t-input v-model="filters.keyword" clearable placeholder="关键词" style="width: 200px" />
          <t-input v-model="filters.name" clearable placeholder="字典名称" style="width: 200px" />
          <t-select v-model="filters.status" clearable placeholder="状态" style="width: 160px" :options="statusOptions" />
          <t-button theme="primary" @click="loadDicts">查询</t-button>
          <t-button variant="outline" @click="resetFilters">重置</t-button>
        </div>
        <div class="action-right">
          <t-button theme="primary" @click="openCreate">新增</t-button>
        </div>
      </div>

      <t-table row-key="id" :data="dictList" :columns="dictColumns" :pagination="pagination" :loading="loading" @page-change="onPageChange">
        <template #serial="{ rowIndex }">
          {{ (pagination.current - 1) * pagination.pageSize + rowIndex + 1 }}
        </template>
        <template #status="{ row }">
          <t-switch :value="row.status === 1" @change="(val) => toggleStatus(row, Boolean(val))" />
        </template>
        <template #op="{ row }">
          <t-space>
            <t-link theme="primary" @click="openEdit(row)">编辑</t-link>
            <t-link theme="danger" @click="removeDict(row)">删除</t-link>
            <t-link theme="primary" @click="openConfig(row)">字典配置</t-link>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <t-dialog v-model:visible="dictDialogVisible" :header="dictDialogTitle" width="520px" :close-on-overlay-click="false">
      <t-form ref="dictFormRef" :data="dictForm" :rules="dictRules" label-width="90px" layout="vertical">
        <t-form-item label="字典名称" name="name"><t-input v-model="dictForm.name" placeholder="例如：性别" /></t-form-item>
        <t-form-item label="字典编码" name="code"><t-input v-model="dictForm.code" placeholder="例如：gender" /></t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="dictForm.status">
            <t-radio :value="1">正常</t-radio>
            <t-radio :value="0">停用</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="排序" name="sort"><t-input-number v-model="dictForm.sort" :min="0" style="width: 160px" /></t-form-item>
        <t-form-item label="备注" name="remark">
          <t-textarea v-model="dictForm.remark" placeholder="请输入说明" :autosize="{ minRows: 2, maxRows: 4 }" />
        </t-form-item>
      </t-form>
      <template #footer>
        <t-space>
          <t-button variant="outline" @click="dictDialogVisible = false">取消</t-button>
          <t-button theme="primary" :loading="dictSaving" @click="submitDict">确定</t-button>
        </t-space>
      </template>
    </t-dialog>

    <t-dialog v-model:visible="configDialogVisible" :header="configDialogTitle" width="1000px" :close-on-overlay-click="false" :footer="false">
      <div class="config-toolbar">
        <div class="config-toolbar__left">
          <t-input v-model="itemFilters.keyword" clearable placeholder="名称/数据值" style="width: 200px" />
          <t-select v-model="itemFilters.status" clearable placeholder="状态" style="width: 160px" :options="statusOptions" />
          <t-button theme="primary" @click="loadItems">查询</t-button>
          <t-button variant="outline" @click="resetItemFilters">重置</t-button>
        </div>
        <div class="config-toolbar__right">
          <t-space>
            <t-button theme="primary" @click="openItemCreate">新增</t-button>
            <t-button variant="outline" @click="downloadTemplate">
              <template #icon><t-icon name="download" /></template>
              下载模板
            </t-button>
            <t-upload
              v-model="importFiles"
              :action="uploadAction"
              :headers="uploadHeaders"
              theme="file"
              :auto-upload="true"
              :use-mock-progress="true"
              :mock-progress-duration="80"
              :max="1"
              accept=".xlsx,.xls,.csv,.txt"
              @success="handleImportSuccess"
              @fail="handleImportFail"
            >
              <t-button variant="outline">
                <template #icon><t-icon name="upload" /></template>
                导入数据
              </t-button>
            </t-upload>
            <t-button variant="outline" @click="exportItems">
              <template #icon><t-icon name="download" /></template>
              导出数据
            </t-button>
          </t-space>
        </div>
      </div>

      <t-table row-key="id" :data="itemList" :columns="itemColumns" :pagination="itemPagination" :loading="itemLoading" @page-change="onItemPageChange">
        <template #valueType="{ row }">
          <t-tag variant="light">{{ valueTypeLabelMap[row.valueType] || row.valueType || 'string' }}</t-tag>
        </template>
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'default'">{{ row.status === 1 ? '正常' : '停用' }}</t-tag>
        </template>
        <template #tagColor="{ row }">
          <t-tag v-if="row.tagColor" :theme="row.tagColor" variant="light">{{ row.tagColor }}</t-tag>
          <span v-else>-</span>
        </template>
        <template #op="{ row }">
          <t-space>
            <t-link theme="primary" @click="openItemEdit(row)">编辑</t-link>
            <t-link theme="danger" @click="removeItem(row)">删除</t-link>
          </t-space>
        </template>
      </t-table>
    </t-dialog>

    <t-dialog v-model:visible="itemDialogVisible" :header="itemDialogTitle" width="520px" :close-on-overlay-click="false">
      <t-form ref="itemFormRef" :data="itemForm" :rules="itemRules" label-width="90px" layout="vertical">
        <t-form-item label="名称" name="label"><t-input v-model="itemForm.label" placeholder="例如：男" /></t-form-item>
        <t-form-item label="数据值" name="value"><t-input v-model="itemForm.value" placeholder="例如：male" /></t-form-item>
        <t-form-item label="数据值类型" name="valueType">
          <t-select v-model="itemForm.valueType" :options="valueTypeOptions" placeholder="请选择数据值类型" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="itemForm.status">
            <t-radio :value="1">正常</t-radio>
            <t-radio :value="0">停用</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="排序" name="sort"><t-input-number v-model="itemForm.sort" :min="0" style="width: 160px" /></t-form-item>
        <t-form-item label="标签颜色" name="tagColor">
          <t-select v-model="itemForm.tagColor" :options="tagColorOptions" clearable placeholder="可选" />
        </t-form-item>
        <t-form-item v-if="isAddressDistrictDict" label="省" name="province"><t-input v-model="itemForm.province" placeholder="例如：广东省" /></t-form-item>
        <t-form-item v-if="isAddressDistrictDict" label="市" name="city"><t-input v-model="itemForm.city" placeholder="例如：深圳市" /></t-form-item>
        <t-form-item v-if="isAddressDistrictDict" label="区" name="district"><t-input v-model="itemForm.district" placeholder="例如：南山区" /></t-form-item>
      </t-form>
      <template #footer>
        <t-space>
          <t-button variant="outline" @click="itemDialogVisible = false">取消</t-button>
          <t-button theme="primary" :loading="itemSaving" @click="submitItem">确定</t-button>
        </t-space>
      </template>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

import { useDictPage } from '../hooks/useDictPage';

const {
  loading,
  dictSaving,
  dictDialogVisible,
  dictDialogTitle,
  dictFormRef,
  dictList,
  filters,
  pagination,
  dictForm,
  configDialogVisible,
  isAddressDistrictDict,
  itemLoading,
  itemSaving,
  itemDialogVisible,
  itemFormRef,
  itemList,
  itemFilters,
  itemPagination,
  itemForm,
  itemColumns,
  configDialogTitle,
  itemDialogTitle,
  uploadHeaders,
  uploadAction,
  importFiles,
  loadDicts,
  resetFilters,
  onPageChange,
  openCreate,
  openEdit,
  submitDict,
  removeDict,
  toggleStatus,
  openConfig,
  loadItems,
  resetItemFilters,
  onItemPageChange,
  openItemCreate,
  openItemEdit,
  submitItem,
  removeItem,
  handleImportSuccess,
  handleImportFail,
  exportItems,
  downloadTemplate,
  dictColumns,
  statusOptions,
  valueTypeOptions,
  valueTypeLabelMap,
  tagColorOptions,
  dictRules,
  itemRules,
} = useDictPage();

onMounted(() => {
  loadDicts();
});
</script>

<style scoped lang="less">
.dict-page {
  .table-action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;

    .action-left {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
    }
  }

  .config-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;

    &__left,
    &__right {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
    }
  }
}
</style>
