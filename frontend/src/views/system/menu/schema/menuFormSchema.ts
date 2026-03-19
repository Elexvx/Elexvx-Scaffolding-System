import { reactive } from 'vue';

import { defaultMenuActions } from '../constants/menuOptions';
import type { MenuFormModel } from '../types';

export const createMenuFormModel = () =>
  reactive<MenuFormModel>({
    id: null,
    version: null,
    parentId: null,
    nodeType: 'DIR',
    path: '',
    routeName: '',
    component: '',
    redirect: '',
    titleZhCn: '',
    titleEnUs: '',
    icon: '',
    hidden: false,
    frameSrc: '',
    frameBlank: false,
    enabled: true,
    orderNo: 0,
    actions: [...defaultMenuActions],
    openType: 'internal',
  });

export const resetMenuFormModel = (form: MenuFormModel) => {
  form.id = null;
  form.version = null;
  form.parentId = null;
  form.nodeType = 'DIR';
  form.path = '';
  form.routeName = '';
  form.component = '';
  form.redirect = '';
  form.titleZhCn = '';
  form.titleEnUs = '';
  form.icon = '';
  form.hidden = false;
  form.frameSrc = '';
  form.frameBlank = false;
  form.enabled = true;
  form.orderNo = 0;
  form.actions = [...defaultMenuActions];
  form.openType = 'internal';
};

