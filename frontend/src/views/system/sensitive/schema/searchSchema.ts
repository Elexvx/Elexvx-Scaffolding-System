import type { SensitiveWordSearchForm } from '../types';

export const createSensitiveWordSearchForm = (): SensitiveWordSearchForm => ({
  keyword: '',
});

export const resetSensitiveWordSearchForm = (form: SensitiveWordSearchForm) => {
  form.keyword = '';
};
