import { reactive } from 'vue';

export { useUserTableColumns } from './useUserTableColumns';
export { useUserSearchForm } from './useUserSearchForm';

export const createUserPagination = () =>
  reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  });
