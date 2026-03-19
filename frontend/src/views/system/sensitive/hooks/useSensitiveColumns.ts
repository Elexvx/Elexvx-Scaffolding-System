import { computed } from 'vue';

import type { SensitivePagination } from '../types';
import { sensitivePageColumns, sensitiveWordColumns } from '../constants/sensitiveOptions';

export const useSensitiveColumns = (pagination: SensitivePagination) => {
  const wordColumns = computed(() =>
    sensitiveWordColumns.map((column) => {
      if (column.colKey !== 'serial-number') return column;
      return {
        ...column,
        cell: (_h: unknown, context: { rowIndex: number }) =>
          String((pagination.current - 1) * pagination.pageSize + context.rowIndex + 1),
      };
    }),
  );

  return {
    wordColumns,
    pageColumns: sensitivePageColumns,
  };
};
