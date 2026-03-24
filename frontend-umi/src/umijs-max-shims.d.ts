declare module '@umijs/max' {
  import type { ComponentType } from 'react';

  export const history: {
    location: { pathname: string };
    push: (path: string) => void;
    replace: (path: string) => void;
  };

  export const Navigate: ComponentType<{ to: string }>;

  export function useModel(namespace: string): any;
  export function useAccess(): { hasPermission: (permission?: string | string[]) => boolean };
  export function useLocation(): { pathname: string };
  export function request(url: string, options?: any): Promise<any>;
  export function defineConfig(config: any): any;

  export type RequestConfig = any;
  export type RunTimeLayoutConfig = any;
}
