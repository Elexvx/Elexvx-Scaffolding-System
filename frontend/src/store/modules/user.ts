import { defineStore } from 'pinia';

import { useNotificationStore, usePermissionStore } from '@/store';
import type { LoginResponse, UserInfo } from '@/types/interface';
import { clearTokenStorage, saveRefreshToken, saveToken } from '@/utils/secureToken';
import { migrateLocalStorageKey } from '@/utils/storage/compat';
import { clearTokenExpireTimer } from '@/utils/tokenExpire';

/**
 * 用户登录态与资料 Store。
 *
 * 核心点：
 * - token/refreshToken 的读写通过 utils/secureToken 统一收口（避免各处自行操作 storage）
 * - tokenExpiresAt 为本地过期时间戳（配合 utils/tokenExpire 做“主动过期”处理）
 * - afterRestore：持久化恢复后初始化权限路由（permissionStore.initRoutes），保证刷新后菜单可用
 */
const InitUserInfo: UserInfo = {
  id: undefined,
  name: '', // 用户名，用于展示在页面右上角头像处
  avatar: '',
  roles: [], // 前端权限模型使用 如果使用请配置modules/permission-fe.ts使用
  assignedRoles: [],
  roleSimulated: false,
  permissions: [],
  orgUnitNames: [],
};

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    refreshToken: '',
    tokenExpiresAt: null as number | null,
    userInfo: { ...InitUserInfo },
    userInfoLoaded: false,
  }),
  getters: {
    roles: (state) => {
      return state.userInfo?.roles;
    },
  },
  actions: {
    async applyLoginResponse(res: LoginResponse) {
      const accessToken = res?.token || res?.accessToken;
      if (res?.status !== 'ok' || !accessToken) return;
      this.token = accessToken;
      this.refreshToken = res?.refreshToken || '';
      this.tokenExpiresAt = res.expiresIn ? Date.now() + res.expiresIn * 1000 : null;
      this.userInfoLoaded = false;
      await saveToken(accessToken);
      await saveRefreshToken(this.refreshToken);
      const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
      if (typeof window !== 'undefined') {
        window.localStorage.removeItem('tabsRouter');
      }
      useTabsRouterStore().$reset();
    },
    async login(userInfo: Record<string, unknown>): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>({ url: '/auth/login', data: userInfo }, { withToken: false });
      await this.applyLoginResponse(res);
      return res;
    },
    async loginBySms(payload: { phone: string; code: string; force?: boolean }): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>({ url: '/auth/login/sms', data: payload }, { withToken: false });
      await this.applyLoginResponse(res);
      return res;
    },
    async loginByEmail(payload: { email: string; code: string; force?: boolean }): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>({ url: '/auth/login/email', data: payload }, { withToken: false });
      await this.applyLoginResponse(res);
      return res;
    },
    async restoreTokenFromStorage() {
      if (this.token) return this.token;
      const { loadRefreshToken, loadToken } = await import('@/utils/secureToken');
      const token = await loadToken();
      if (token) {
        this.token = token;
      }
      const refreshToken = await loadRefreshToken();
      if (refreshToken) {
        this.refreshToken = refreshToken;
      }
      return token;
    },
    async getUserInfo(force = false) {
      if (this.userInfoLoaded && !force) return;
      const { request } = await import('@/utils/request');
      const info = await request.get<UserInfo>({ url: '/auth/user' });
      this.userInfo = info;
      this.userInfoLoaded = true;
    },
    async logout() {
      const { request } = await import('@/utils/request');
      clearTokenExpireTimer();
      await request.post({ url: '/auth/logout' });
      this.token = '';
      this.refreshToken = '';
      this.tokenExpiresAt = null;
      this.userInfo = { ...InitUserInfo };
      this.userInfoLoaded = false;
      useNotificationStore().stopSocket();
      const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
      if (typeof window !== 'undefined') {
        window.localStorage.removeItem('tabsRouter');
      }
      useTabsRouterStore().$reset();
      clearTokenStorage();
    },
  },
  persist: {
    afterRestore: () => {
      migrateLocalStorageKey('elexvx.user', ['user']);
      const permissionStore = usePermissionStore();
      permissionStore.initRoutes();
    },
    key: 'elexvx.user',
    paths: ['tokenExpiresAt', 'userInfo'],
    storage: typeof window === 'undefined' ? undefined : localStorage,
  },
});
