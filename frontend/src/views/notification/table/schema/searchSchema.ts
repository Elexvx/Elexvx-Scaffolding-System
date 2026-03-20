import { reactive } from 'vue';

import type { NotificationQuery } from '../types';

export const createNotificationQuery = (): NotificationQuery => ({
  keyword: '',
  priority: '',
  status: '',
  page: 0,
  size: 10,
});

export const useNotificationSearchState = () => reactive(createNotificationQuery());
