import type { FormInstanceFunctions, PageInfo, UploadFile } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref } from 'vue';

import { importExportApi } from '@/api/importExport';
import type { DictionaryImportResult, SysDict, SysDictItem } from '@/api/system/dictionary';
import { createDict, createDictItem, deleteDict, deleteDictItem, fetchDictItems, fetchDictPage, updateDict, updateDictItem } from '@/api/system/dictionary';
import { useDictionaryStore, useUserStore } from '@/store';

import { dictColumns, statusOptions, tagColorOptions, valueTypeLabelMap, valueTypeOptions } from '../constants/dictOptions';
import { buildItemColumns, createDictForm, createItemForm, dictRules, itemRules } from '../schema/dictSchema';

export const useDictPage = () => {
  const dictStore = useDictionaryStore();
  const userStore = useUserStore();
  const loading = ref(false);
  const dictSaving = ref(false);
  const dictDialogVisible = ref(false);
  const dictDialogMode = ref<'create' | 'edit'>('create');
  const dictFormRef = ref<FormInstanceFunctions>();
  const dictList = ref<SysDict[]>([]);
  const editingDictId = ref<number | null>(null);

  const filters = reactive({ keyword: '', name: '', status: null as number | null });
  const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
  const dictForm = reactive(createDictForm());
  const dictDialogTitle = computed(() => (dictDialogMode.value === 'create' ? '新增字典' : '编辑字典'));

  const configDialogVisible = ref(false);
  const selectedDict = ref<SysDict | null>(null);
  const isAddressDistrictDict = computed(() => selectedDict.value?.code === 'address_district');
  const itemLoading = ref(false);
  const itemSaving = ref(false);
  const itemDialogVisible = ref(false);
  const itemDialogMode = ref<'create' | 'edit'>('create');
  const itemFormRef = ref<FormInstanceFunctions>();
  const itemList = ref<SysDictItem[]>([]);
  const editingItemId = ref<number | null>(null);
  const itemFilters = reactive({ keyword: '', status: null as number | null });
  const itemPagination = reactive({ current: 1, pageSize: 10, total: 0 });
  const itemForm = reactive(createItemForm());
  const itemColumns = computed(() => buildItemColumns(isAddressDistrictDict.value));
  const configDialogTitle = computed(() => (selectedDict.value ? `${selectedDict.value.name} - 字典配置` : '字典配置'));
  const itemDialogTitle = computed(() => (itemDialogMode.value === 'create' ? '新增字典项' : '编辑字典项'));
  const uploadHeaders = computed(() => ({ Authorization: userStore.token }));
  const uploadAction = computed(() => importExportApi.dict.importItemsAction(selectedDict.value?.id ?? 0));
  const importFiles = ref<UploadFile[]>([]);

  const loadDicts = async () => {
    loading.value = true;
    try {
      const data = await fetchDictPage({
        keyword: filters.keyword || filters.name || undefined,
        status: filters.status ?? undefined,
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      dictList.value = data.list || [];
      pagination.total = data.total || 0;
    } finally {
      loading.value = false;
    }
  };

  const resetFilters = () => {
    filters.keyword = '';
    filters.name = '';
    filters.status = null;
    pagination.current = 1;
    loadDicts();
  };

  const onPageChange = (pageInfo: PageInfo) => {
    pagination.current = pageInfo.current;
    pagination.pageSize = pageInfo.pageSize;
    loadDicts();
  };

  const openCreate = () => {
    dictDialogMode.value = 'create';
    editingDictId.value = null;
    Object.assign(dictForm, createDictForm());
    dictDialogVisible.value = true;
  };

  const openEdit = (row: SysDict) => {
    dictDialogMode.value = 'edit';
    editingDictId.value = row.id;
    dictForm.name = row.name || '';
    dictForm.code = row.code || '';
    dictForm.status = row.status ?? 1;
    dictForm.sort = row.sort ?? 0;
    dictForm.remark = row.remark || '';
    dictDialogVisible.value = true;
  };

  const submitDict = async () => {
    const valid = await dictFormRef.value?.validate();
    if (valid !== true) return;
    dictSaving.value = true;
    try {
      const payload = { ...dictForm, remark: dictForm.remark || undefined };
      if (dictDialogMode.value === 'create') {
        await createDict(payload);
        MessagePlugin.success('新增成功');
      } else if (editingDictId.value) {
        await updateDict(editingDictId.value, payload);
        MessagePlugin.success('更新成功');
      }
      dictDialogVisible.value = false;
      loadDicts();
    } finally {
      dictSaving.value = false;
    }
  };

  const removeDict = (row: SysDict) => {
    const dialog = DialogPlugin.confirm({
      header: '删除字典',
      body: `确认删除字典「${row.name}」？`,
      theme: 'danger',
      confirmBtn: '删除',
      onConfirm: async () => {
        await deleteDict(row.id);
        MessagePlugin.success('删除成功');
        dialog.hide();
        loadDicts();
      },
    });
  };

  const toggleStatus = async (row: SysDict, enabled: boolean) => {
    await updateDict(row.id, { status: enabled ? 1 : 0 });
    row.status = enabled ? 1 : 0;
  };

  const openConfig = (row: SysDict) => {
    selectedDict.value = row;
    itemFilters.keyword = '';
    itemFilters.status = null;
    itemPagination.current = 1;
    configDialogVisible.value = true;
    loadItems();
  };

  const loadItems = async () => {
    if (!selectedDict.value) return;
    itemLoading.value = true;
    try {
      const data = await fetchDictItems(selectedDict.value.id, {
        keyword: itemFilters.keyword || undefined,
        status: itemFilters.status ?? undefined,
        page: itemPagination.current - 1,
        size: itemPagination.pageSize,
      });
      itemList.value = data.list || [];
      itemPagination.total = data.total || 0;
    } finally {
      itemLoading.value = false;
    }
  };

  const resetItemFilters = () => {
    itemFilters.keyword = '';
    itemFilters.status = null;
    itemPagination.current = 1;
    loadItems();
  };

  const onItemPageChange = (pageInfo: PageInfo) => {
    itemPagination.current = pageInfo.current;
    itemPagination.pageSize = pageInfo.pageSize;
    loadItems();
  };

  const openItemCreate = () => {
    itemDialogMode.value = 'create';
    editingItemId.value = null;
    Object.assign(itemForm, createItemForm());
    itemDialogVisible.value = true;
  };

  const openItemEdit = (row: SysDictItem) => {
    itemDialogMode.value = 'edit';
    editingItemId.value = row.id;
    itemForm.label = row.label;
    itemForm.value = row.value;
    itemForm.valueType = row.valueType || 'string';
    itemForm.status = row.status ?? 1;
    itemForm.sort = row.sort ?? 0;
    itemForm.tagColor = row.tagColor || '';
    itemForm.province = row.province || '';
    itemForm.city = row.city || '';
    itemForm.district = row.district || '';
    itemDialogVisible.value = true;
  };

  const submitItem = async () => {
    if (!selectedDict.value) return;
    const valid = await itemFormRef.value?.validate();
    if (valid !== true) return;
    if (isAddressDistrictDict.value && (!itemForm.province.trim() || !itemForm.city.trim() || !itemForm.district.trim())) {
      MessagePlugin.error('address_district 字典项必须填写省/市/区字段');
      return;
    }
    itemSaving.value = true;
    try {
      const payload = {
        label: itemForm.label,
        value: itemForm.value,
        valueType: itemForm.valueType,
        status: itemForm.status,
        sort: itemForm.sort,
        tagColor: itemForm.tagColor || undefined,
        province: isAddressDistrictDict.value ? itemForm.province.trim() : undefined,
        city: isAddressDistrictDict.value ? itemForm.city.trim() : undefined,
        district: isAddressDistrictDict.value ? itemForm.district.trim() : undefined,
      };
      if (itemDialogMode.value === 'create') {
        await createDictItem(selectedDict.value.id, payload);
        MessagePlugin.success('新增成功');
      } else if (editingItemId.value) {
        await updateDictItem(editingItemId.value, payload);
        MessagePlugin.success('更新成功');
      }
      dictStore.clearDictCache(selectedDict.value.code);
      itemDialogVisible.value = false;
      loadItems();
    } finally {
      itemSaving.value = false;
    }
  };

  const removeItem = (row: SysDictItem) => {
    const dialog = DialogPlugin.confirm({
      header: '删除字典项',
      body: `确认删除字典项「${row.label}」？`,
      theme: 'danger',
      confirmBtn: '删除',
      onConfirm: async () => {
        await deleteDictItem(row.id);
        MessagePlugin.success('删除成功');
        dialog.hide();
        if (selectedDict.value) dictStore.clearDictCache(selectedDict.value.code);
        loadItems();
      },
    });
  };

  const handleImportSuccess = (context: any) => {
    const response = context?.response || {};
    const result: DictionaryImportResult | undefined = response?.data || response?.result || response?.data?.data;
    if (response?.code !== undefined && response.code !== 0) {
      MessagePlugin.error(response?.message || '导入失败');
      importFiles.value = [];
      return;
    }
    if (result) {
      MessagePlugin.success(`导入完成：新增${result.imported}条，更新${result.updated}条，失败${result.failed}条`);
      if (result.errors && result.errors.length > 0) {
        DialogPlugin.alert({ header: '导入异常提示', body: result.errors.join('\n') });
      }
    } else {
      MessagePlugin.success('导入完成');
    }
    importFiles.value = [];
    if (selectedDict.value) dictStore.clearDictCache(selectedDict.value.code);
    loadItems();
  };

  const handleImportFail = () => {
    MessagePlugin.error('导入失败');
    importFiles.value = [];
  };

  const exportItems = async () => {
    if (!selectedDict.value) return;
    const response = await importExportApi.dict.exportItems(selectedDict.value.id);
    await importExportApi.utils.downloadBlobResponse(response as any, `dict_${selectedDict.value.code}_items.xlsx`);
  };

  const downloadTemplate = async () => {
    const response = await importExportApi.dict.downloadTemplate();
    await importExportApi.utils.downloadBlobResponse(response as any, 'dict_items_template.xlsx');
  };

  return {
    loading,
    dictSaving,
    dictDialogVisible,
    dictDialogMode,
    dictFormRef,
    dictList,
    filters,
    pagination,
    dictForm,
    dictDialogTitle,
    configDialogVisible,
    selectedDict,
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
  };
};
