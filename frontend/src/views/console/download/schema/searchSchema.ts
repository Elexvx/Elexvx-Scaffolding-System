import type { DownloadFilters } from '../types';

export const createDownloadFilters = (): DownloadFilters => ({
  page: 0,
  size: 10,
});
