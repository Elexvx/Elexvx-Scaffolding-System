import type { PageInfo } from 'tdesign-vue-next';
import { fetchNotifications } from '@/api/notification';

import type { NotificationQuery } from '../types';

export const useNotificationTable = (options: {
  query: NotificationQuery;
  list: any;
  total: any;
  loading: any;
}) => {
  const { query, list, total, loading } = options;
  const load = async () => {
    loading.value = true;
    try {
      const res = await fetchNotifications({
        page: query.page,
        size: query.size,
        keyword: query.keyword,
        priority: query.priority,
        status: query.status,
      });
      list.value = res.list || [];
      total.value = res.total || 0;
    } finally {
      loading.value = false;
    }
  };

  const handleSearch = () => {
    query.page = 0;
    void load();
  };

  const reset = () => {
    query.keyword = '';
    query.priority = '';
    query.status = '';
    query.page = 0;
    void load();
  };

  const onPageChange = (pageInfo: PageInfo) => {
    query.page = pageInfo.current - 1;
    query.size = pageInfo.pageSize;
    void load();
  };

  return { load, handleSearch, reset, onPageChange };
};
