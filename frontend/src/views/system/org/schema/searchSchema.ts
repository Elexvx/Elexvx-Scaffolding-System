import type { OrgSearchForm } from '../types';

export const createOrgSearchFormModel = (): OrgSearchForm => ({
  keyword: '',
  status: null,
});

export const resetOrgSearchFormModel = (form: OrgSearchForm) => {
  form.keyword = '';
  form.status = null;
};
