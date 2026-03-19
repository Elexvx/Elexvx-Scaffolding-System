import { computed, ref, watch } from 'vue';

import { createMenuFormModel } from '../schema/menuFormSchema';
import { normalizeParentId } from '../utils/menuMappers';

export const useMenuForm = () => {
  const formRef = ref();
  const form = createMenuFormModel();

  const isExternalLink = computed<boolean>({
    get: () => form.openType === 'external' || !!form.frameSrc,
    set: (value: boolean) => {
      if (value) {
        form.openType = 'external';
      } else {
        form.openType = 'internal';
        form.frameSrc = '';
      }
    },
  });

  const onParentIdChange = (value: unknown) => {
    form.parentId = normalizeParentId(value);
  };

  const onParentIdClear = () => {
    form.parentId = null;
  };

  watch(
    () => [form.nodeType, form.openType, form.parentId] as const,
    ([nodeType, openType, parentId], [_oldNodeType, _oldOpenType, oldParentId]) => {
      if (oldParentId != null && parentId == null && form.path && !form.path.startsWith('/')) {
        form.path = `/${form.path.replace(/^\/+/, '')}`;
      }
      if (oldParentId == null && parentId != null && form.path && form.path.startsWith('/')) {
        form.path = form.path.replace(/^\/+/, '');
      }
      if (nodeType === 'BTN') {
        form.openType = 'internal';
        form.component = '';
        return;
      }
      if (nodeType !== 'PAGE') {
        form.openType = 'internal';
        form.frameSrc = '';
        if (parentId == null && !form.component.trim()) form.component = 'LAYOUT';
        return;
      }
      if (openType === 'internal') {
        form.frameSrc = '';
        if (form.component === 'IFRAME') form.component = '';
      } else {
        form.component = 'IFRAME';
      }
    },
  );

  return {
    formRef,
    form,
    isExternalLink,
    onParentIdChange,
    onParentIdClear,
  };
};
