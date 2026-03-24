import { MessagePlugin } from 'tdesign-vue-next';
import type { Ref } from 'vue';

import type { FileResourceItem } from '@/api/download';
import { deleteFileResource, fetchFileResources } from '@/api/download';

import { parseApiError } from '../utils/downloadGuards';

interface UseDownloadTableOptions {
  query: { page: number; size: number };
  list: Ref<FileResourceItem[]>;
  total: Ref<number>;
  loading: Ref<boolean>;
}

export const useDownloadTable = (options: UseDownloadTableOptions) => {
  const { query, list, total, loading } = options;

  const load = async () => {
    loading.value = true;
    try {
      const data = await fetchFileResources({ page: query.page, size: query.size });
      list.value = data.list;
      total.value = data.total;
    } catch (error) {
      MessagePlugin.error(parseApiError(error, '加载失败'));
    } finally {
      loading.value = false;
    }
  };

  const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
    query.page = pageInfo.current - 1;
    query.size = pageInfo.pageSize;
    load();
  };

  const remove = async (row: FileResourceItem) => {
    try {
      await deleteFileResource(row.id);
      MessagePlugin.success('删除成功');
      await load();
    } catch (error) {
      MessagePlugin.error(parseApiError(error, '删除失败'));
    }
  };

  return { load, onPageChange, remove };
};
