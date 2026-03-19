import { reactive, ref } from 'vue';
import type { NodeType } from '../helpers';
import type { OpenType } from '../schema';

export const useMenuForm = () => {
  const formRef = ref();
  const form = reactive({
    id: null as number | null,
    version: null as number | null,
    parentId: null as number | null,
    nodeType: 'DIR' as NodeType,
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
    actions: [] as string[],
    openType: 'internal' as OpenType,
  });
  return { formRef, form };
};
