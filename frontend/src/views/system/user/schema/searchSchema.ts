import { reactive } from 'vue';

import type { UserSearchModel } from '../types';

export const createUserSearchModel = () =>
  reactive<UserSearchModel>({
    keyword: '',
    mobile: '',
    status: null,
    createdRange: [],
  });

export const resetUserSearchModel = (model: UserSearchModel) => {
  model.keyword = '';
  model.mobile = '';
  model.status = null;
  model.createdRange = [];
};

