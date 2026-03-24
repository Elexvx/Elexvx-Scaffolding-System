import { reactive } from 'vue';

import type { AnnouncementQuery } from '../types';

export const createAnnouncementQuery = (): AnnouncementQuery => ({
  keyword: '',
  priority: '',
  status: '',
  page: 0,
  size: 10,
});

export const useAnnouncementSearchState = () => reactive(createAnnouncementQuery());
