import { reactive } from 'vue';

import { createOrgSearchFormModel, resetOrgSearchFormModel } from '../schema/searchSchema';

export const useOrgSearchForm = () => {
  const filters = reactive(createOrgSearchFormModel());

  const resetFilters = () => {
    resetOrgSearchFormModel(filters);
  };

  return { filters, resetFilters };
};
