import { createUserSearchModel, resetUserSearchModel } from '../schema/searchSchema';

export const useUserSearchForm = () => {
  const filters = createUserSearchModel();

  return {
    filters,
    resetFilters: () => resetUserSearchModel(filters),
  };
};

