import cloneDeep from 'lodash/cloneDeep';

import type { RouteItem } from '@/api/model/permissionModel';
import {
  BLANK_LAYOUT,
  EXCEPTION_COMPONENT,
  IFRAME,
  LAYOUT,
  PAGE_NOT_FOUND_ROUTE,
  PARENT_LAYOUT,
} from '@/utils/route/constant';

/**
 * 后端菜单/路由结构 -> vue-router RouteRecordRaw 的转换工具。
 *
 * 关键点：
 * - LayoutMap：将后端 component 标识（LAYOUT/BLANK/IFRAME/PARENT_LAYOUT）映射到前端布局组件
 * - dynamicImport：支持从 pages 与 views 两套目录动态加载页面组件（由 VITE_ROUTE_VIEW_DIR 控制优先级）
 * - asyncImportRoute：递归处理子路由，拼接相对 path，并为每个路由补齐 component
 */
// 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
// const iconsPath = import.meta.glob('../../../node_modules/tdesign-icons-vue-next/esm/components/*.js');

// async function getMenuIcon(iconName: string): Promise<string> {
//   const RenderIcon = iconsPath[`../../../node_modules/tdesign-icons-vue-next/esm/components/${iconName}.js`];

//   const Icon = await RenderIcon();
//   return shallowRef(Icon.default);
// }

const LayoutMap = new Map<string, () => Promise<typeof import('*.vue')>>();

LayoutMap.set('LAYOUT', LAYOUT);
LayoutMap.set('BLANK', BLANK_LAYOUT);
LayoutMap.set('IFRAME', IFRAME);
LayoutMap.set('PARENT_LAYOUT', PARENT_LAYOUT);

type ViewDir = 'pages' | 'views';
type ViewModuleLoader = () => Promise<Recordable>;

const VIEW_DIRS: ViewDir[] = ['pages', 'views'];
let viewModulesByDir: Record<ViewDir, Map<string, ViewModuleLoader>> | undefined;

function getPreferredViewDir(): ViewDir {
  const raw = import.meta.env.VITE_ROUTE_VIEW_DIR;
  const value = typeof raw === 'string' ? raw.toLowerCase() : '';
  return value === 'pages' ? 'pages' : 'views';
}

function getViewModulesByDir() {
  if (viewModulesByDir) return viewModulesByDir;

  const modules = import.meta.glob(['/src/pages/**/*.vue', '/src/views/**/*.vue']) as Record<string, ViewModuleLoader>;

  const byDir: Record<ViewDir, Map<string, ViewModuleLoader>> = {
    pages: new Map(),
    views: new Map(),
  };

  Object.entries(modules).forEach(([file, loader]) => {
    const match = file.match(/^\/src\/(pages|views)\/(.+)\.vue$/i);
    if (!match) return;
    const dir = match[1].toLowerCase() as ViewDir;
    const normalized = match[2];
    byDir[dir].set(normalized, loader);
  });

  viewModulesByDir = byDir;
  return viewModulesByDir;
}

function normalizeRouteComponentPath(component: string): { dirHint?: ViewDir; normalized: string } {
  let path = String(component || '').trim();
  if (!path) return { normalized: '' };

  path = path.replace(/\\/g, '/');
  path = path.replace(/\.vue$/i, '');

  if (path.startsWith('/')) path = path.slice(1);
  if (path.startsWith('src/')) path = path.slice('src/'.length);
  if (path.startsWith('/src/')) path = path.slice('/src/'.length);

  const dirMatch = path.match(/^(pages|views)\//i);
  let dirHint: ViewDir | undefined;
  if (dirMatch) {
    dirHint = dirMatch[1].toLowerCase() as ViewDir;
    path = path.slice(dirHint.length + 1);
  }

  return { dirHint, normalized: path };
}

// 动态引入路由组件
function asyncImportRoute(routes: RouteItem[] | undefined, parentPath: string = '', isPathMerged: boolean = false) {
  getViewModulesByDir();
  if (!routes) return;

  routes.forEach(async (item) => {
    const { component, name } = item;
    const { children } = item;

    // 处理相对路径：如果path不以/开头且有父路径，则拼接
    // isPathMerged 标志表示路径是否已经在上层被处理过
    if (item.path && !item.path.startsWith('/') && parentPath && !isPathMerged) {
      item.path = `${parentPath}/${item.path}`;
    }

    if (component) {
      const layoutFound = LayoutMap.get(component.toUpperCase());
      if (layoutFound) {
        item.component = layoutFound;
      } else {
        item.component = dynamicImport(component);
      }
    } else if (name) {
      item.component = PARENT_LAYOUT;
    }

    // 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
    // if (item.meta.icon) item.meta.icon = await getMenuIcon(item.meta.icon);

    // 递归处理子路由，传递当前路由路径作为父路径
    // 由于路径已经在当前级别处理，子路由的处理时 isPathMerged = true
    if (children) {
      const currentPath = item.path || parentPath;
      asyncImportRoute(children, currentPath, true);
    }
  });
}

function dynamicImport(component: string) {
  const { dirHint, normalized } = normalizeRouteComponentPath(component);
  if (!normalized) return EXCEPTION_COMPONENT;

  const modules = getViewModulesByDir();

  if (dirHint) {
    const hit = modules[dirHint].get(normalized);
    if (hit) return hit;
  }

  const preferred = getPreferredViewDir();
  const ordered = preferred === 'views' ? [...VIEW_DIRS].reverse() : VIEW_DIRS;
  for (const dir of ordered) {
    const hit = modules[dir].get(normalized);
    if (hit) return hit;
  }

  console.warn(`Can't find ${component} in ${VIEW_DIRS.join('/')} folder`);
  return EXCEPTION_COMPONENT;
}

// 将背景对象变成路由对象
export function transformObjectToRoute<T = RouteItem>(routeList: RouteItem[]): T[] {
  routeList.forEach(async (route) => {
    const component = route.component as string;

    if (component) {
      if (component.toUpperCase() === 'LAYOUT') {
        route.component = LayoutMap.get(component.toUpperCase());
        // 对于LAYOUT类型的路由，需要处理其子路由的路径
        if (route.children && Array.isArray(route.children)) {
          const parentPath = String(route.path || '').replace(/\/$/, ''); // 移除末尾的/
          route.children.forEach((child) => {
            // 如果子路由的路径不以/开头，说明是相对路径，需要拼接
            if (child.path && !child.path.startsWith('/')) {
              child.path = `${parentPath}/${child.path}`;
            }
          });
        }
      } else {
        route.children = [cloneDeep(route)];
        route.component = LAYOUT;
        route.name = `${route.name}Parent`;
        route.path = '';
        route.meta = route.meta || {};
      }
    } else {
      throw new Error('component is undefined');
    }

    // 调用asyncImportRoute时，传递当前路由的路径
    // 对于LAYOUT类型，子路由路径已经在上面处理过，所以 isPathMerged = true
    // 对于其他类型（如包装的路由），路径还没有处理，所以 isPathMerged = false
    if (route.children) {
      const currentPath = route.path || '';
      const isPathAlreadyMerged = (route.component as any) === LayoutMap.get('LAYOUT');
      asyncImportRoute(route.children, currentPath, isPathAlreadyMerged);
    }

    // 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
    // if (route.meta.icon)
    // route.meta.icon = await getMenuIcon(route.meta.icon);
  });

  return [PAGE_NOT_FOUND_ROUTE, ...routeList] as unknown as T[];
}
