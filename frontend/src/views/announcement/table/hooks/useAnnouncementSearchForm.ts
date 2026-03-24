import { useAnnouncementSearchState } from '../schema/searchSchema';

export const useAnnouncementSearchForm = () => {
  const query = useAnnouncementSearchState();
  const resetQuery = () => {
    query.keyword = '';
    query.priority = '';
    query.status = '';
    query.page = 0;
    query.size = 10;
  };
  return { query, resetQuery };
};
