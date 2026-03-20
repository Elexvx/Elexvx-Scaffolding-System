import { reactive, ref } from 'vue';

import { request } from '@/utils/request';

import { createPersonalizePayload, normalizePath } from '../utils/personalizeMappers';

export function usePersonalizePanel() {
  const form = reactive({
    allowMultiDeviceLogin: true,
    logRetentionDays: 90,
    defaultHome: '/user/index',
    autoTheme: false,
    headerGithubUrl: '',
    headerHelpUrl: '',
    maintenanceEnabled: false,
    maintenanceMessage: '',
    lightStartTime: '06:00',
    darkStartTime: '18:00',
  });
  const homeOptions = ref<{ label: string; value: string }[]>([]);
  const homePathMap = ref<Record<string, string>>({});

  const loadHomeOptions = async () => {
    const tree = await request.get<any[]>({ url: '/system/menu/tree' });
    const options: { label: string; value: string }[] = [];
    const pathMap: Record<string, string> = {};
    const walk = (nodes: any[], parentTitle = '', parentPath = '') => {
      nodes.forEach((n) => {
        const fullTitle = parentTitle ? `${parentTitle} / ${n.titleZhCn}` : n.titleZhCn;
        const fullPath = normalizePath(parentPath, n.path);
        if (n.nodeType === 'PAGE' && fullPath && !n.hidden) {
          options.push({ label: fullTitle, value: fullPath });
          if (n.path && !pathMap[n.path]) pathMap[n.path] = fullPath;
        }
        if (n.children?.length) walk(n.children, fullTitle, fullPath);
      });
    };
    walk(tree);
    homeOptions.value = options;
    homePathMap.value = pathMap;
  };

  const load = async () => {
    const s = await request.get<any>({ url: '/system/ui' });
    form.allowMultiDeviceLogin = s.allowMultiDeviceLogin !== undefined ? !!s.allowMultiDeviceLogin : true;
    form.logRetentionDays = s.logRetentionDays !== undefined ? Number(s.logRetentionDays) : 90;
    const rawHome = s.defaultHome || '/user/index';
    form.defaultHome = homePathMap.value[rawHome] || normalizePath('', rawHome);
    form.autoTheme = !!s.autoTheme;
    form.headerGithubUrl = s.headerGithubUrl || '';
    form.headerHelpUrl = s.headerHelpUrl || '';
    form.maintenanceEnabled = s.maintenanceEnabled !== undefined ? !!s.maintenanceEnabled : false;
    form.maintenanceMessage = s.maintenanceMessage || '';
    form.lightStartTime = s.lightStartTime || '06:00';
    form.darkStartTime = s.darkStartTime || '18:00';
  };

  return { createPayload: () => createPersonalizePayload(form, homePathMap.value), form, homeOptions, load, loadHomeOptions };
}
