import { useNotificationSearchState } from '../schema/searchSchema';

export const useNotificationSearchForm = () => {
  const query = useNotificationSearchState();
  const resetQuery = () => {
    query.keyword = '';
    query.priority = '';
    query.status = '';
    query.page = 0;
    query.size = 10;
  };
  return { query, resetQuery };
};
