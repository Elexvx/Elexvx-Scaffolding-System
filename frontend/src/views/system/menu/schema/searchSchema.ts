import { reactive } from 'vue';

export interface MenuSearchModel {
  keyword: string;
}

export const createMenuSearchModel = () =>
  reactive<MenuSearchModel>({
    keyword: '',
  });

