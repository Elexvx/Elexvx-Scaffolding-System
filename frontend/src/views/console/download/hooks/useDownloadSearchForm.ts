import { reactive } from 'vue';

import { createDownloadFilters } from '../schema/searchSchema';

export const useDownloadSearchForm = () => {
  const query = reactive(createDownloadFilters());
  return { query };
};
