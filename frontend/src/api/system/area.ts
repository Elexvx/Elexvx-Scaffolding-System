import { request } from '@/utils/request';

export interface AreaNodeResponse {
  id: number;
  name: string;
  level: number;
  zipCode?: string | null;
  hasChildren?: boolean;
}

export interface AreaPathNode {
  id: number;
  name: string;
  level: number;
  zipCode?: string | null;
}

export const fetchAreaChildren = (parentId = 0) =>
  request.get<AreaNodeResponse[]>({
    url: '/system/area/children',
    params: { parentId },
  });

export const fetchAreaPath = (areaId: number) =>
  request.get<AreaPathNode[]>({
    url: '/system/area/path',
    params: { areaId },
  });

export const resolveAreaPath = (params: { province?: string; city?: string; district?: string }) =>
  request.get<AreaPathNode[]>({
    url: '/system/area/resolve',
    params,
  });
