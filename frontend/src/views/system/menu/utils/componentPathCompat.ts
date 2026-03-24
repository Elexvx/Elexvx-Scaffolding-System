import type { MenuComponentOption } from '../types';

export const buildPageComponentOptions = (): MenuComponentOption[] => {
  const modules = import.meta.glob(['/src/pages/**/*.vue', '/src/views/**/*.vue']);
  const unique = new Set<string>();
  Object.keys(modules).forEach((moduleKey) => {
    const normalized = moduleKey.replace(/^\/src\/(pages|views)\//, '/').replace(/\.vue$/, '');
    unique.add(normalized);
  });
  return Array.from(unique)
    .filter((value) => value !== '/system/menu/index')
    .sort()
    .map((value) => ({ label: value, value }));
};

