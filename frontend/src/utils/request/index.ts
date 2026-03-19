/**
 * Axios 请求实例入口（VAxios 封装）。
 *
 * 统一收口的能力：
 * - API Host 解析：根据 Vite 环境变量与部署模式计算 baseURL
 * - 请求前处理：前缀拼接、时间戳防缓存、参数/日期格式化、RESTful 参数拼接
 * - 鉴权注入：从 userStore 写入 Authorization，并附带当前页面路径（X-Page-Path）
 * - 错误处理：把后端业务 code/message 映射为 Error；对 401/403/422/5xx 做统一提示与跳转
 * - 重试策略：仅对允许重试的请求按 requestOptions.retry 进行退避重试
 */
import type { AxiosInstance } from 'axios';
import axios from 'axios';
import isString from 'lodash/isString';
import merge from 'lodash/merge';
import { MessagePlugin } from 'tdesign-vue-next';

import { ContentTypeEnum } from '@/constants';
import router from '@/router';
import { useUserStore } from '@/store';
import { resolveApiHost } from '@/utils/apiHost';
import { clearTokenStorage } from '@/utils/secureToken';
import { migrateSessionStorageKey } from '@/utils/storage/compat';

import { VAxios } from './Axios';
import type { AxiosTransform, CreateAxiosOptions } from './AxiosTransform';
import { formatRequestDate, joinTimestamp, setObjToUrlParams } from './utils';

let hasNotifiedUnauthorized = false;
let isUnauthorizedRedirecting = false;

const host = resolveApiHost();
const UNAUTHORIZED_SENTINEL = '\u767B\u5F55\u72B6\u6001\u5DF2\u5931\u6548 [401]';
const UNAUTHORIZED_NOTICE_KEY = 'elexvx.auth.invalid.notice';
const LEGACY_UNAUTHORIZED_NOTICE_KEY = 'tdesign.auth.invalid.notice';
const UNAUTHORIZED_NOTICE_TEXT = '\u5F53\u524D\u767B\u5F55\u72B6\u6001\u5931\u6548\uFF0C\u8BF7\u91CD\u65B0\u767B\u5F55';

const buildHardLoginHref = (redirect?: string) => {
  const query: Record<string, string> = { _t: String(Date.now()) };
  if (redirect) query.redirect = redirect;
  return router.resolve({ path: '/login', query }).href;
};

const saveUnauthorizedNotice = (message: string) => {
  try {
    migrateSessionStorageKey(UNAUTHORIZED_NOTICE_KEY, [LEGACY_UNAUTHORIZED_NOTICE_KEY]);
    sessionStorage.setItem(UNAUTHORIZED_NOTICE_KEY, message);
  } catch {}
};

const isSilent403Path = (url?: string) => {
  if (!url) return false;
  const pure = String(url).split('?')[0];
  return pure.endsWith('/system/ui');
};

const transform: AxiosTransform = {
  transformRequestHook: (res, options) => {
    const { isTransformResponse, isReturnNativeResponse } = options;

    const method = res.config.method?.toLowerCase();
    if (res.status === 204 && ['put', 'patch', 'delete'].includes(method)) {
      return res;
    }

    if (isReturnNativeResponse) {
      return res;
    }
    if (!isTransformResponse) {
      return res.data;
    }

    const { data } = res;
    if (!data) {
      throw new Error('Request interface error');
    }

    const { code, message } = data;

    const hasSuccess = data && code === 0;
    if (hasSuccess) {
      return data.data;
    }

    const errorMsg = message || `请求失败, 错误码: ${code}`;
    throw new Error(`${errorMsg} [${code}]`);
  },

  requestCatchHook: async (e) => {
    if (axios.isCancel(e)) {
      return Promise.reject(e);
    }

    const msg = String(e?.message || '');
    const axiosError = axios.isAxiosError(e) ? e : null;
    const responseStatus = axiosError?.response?.status as number | undefined;
    const responseMessage = String(axiosError?.response?.data?.message || '').trim();
    const m = msg.match(/\[(\d{3})\]/) || msg.match(/status\s*(\d{3})/i);
    const code = m ? Number(m[1]) : responseStatus;

    if (code === 401) {
      const user = useUserStore();
      const humanMsg = UNAUTHORIZED_NOTICE_TEXT;
      const currentRoute = router.currentRoute.value;
      const onLoginPage = currentRoute.path === '/login';

      user.token = '';
      user.refreshToken = '';
      user.tokenExpiresAt = null;
      user.userInfo = { name: '', avatar: '', roles: [] };
      user.userInfoLoaded = false;
      clearTokenStorage();

      if (!onLoginPage && !isUnauthorizedRedirecting) {
        isUnauthorizedRedirecting = true;
        if (!hasNotifiedUnauthorized) {
          hasNotifiedUnauthorized = true;
        }
        saveUnauthorizedNotice(humanMsg || UNAUTHORIZED_NOTICE_TEXT);
        const hardHref = buildHardLoginHref(currentRoute.fullPath);
        window.location.replace(hardHref);
        return new Promise(() => {});
      }

      return Promise.reject(new Error(UNAUTHORIZED_SENTINEL));
    } else if (code === 403) {
      const humanMsg = responseMessage || msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      const url = String(axiosError?.config?.url || '');
      if (!isSilent403Path(url)) {
        MessagePlugin.error(
          humanMsg || '\u6743\u9650\u4E0D\u8DB3\uFF0C\u8BF7\u8054\u7CFB\u7BA1\u7406\u5458\u5F00\u901A',
        );
      }
    } else if (code === 422) {
      const humanMsg = responseMessage || msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      if (humanMsg) {
        MessagePlugin.warning(humanMsg);
      }
    } else if (code != null && code >= 500) {
      const humanMsg = responseMessage || msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      MessagePlugin.error(humanMsg || '\u670D\u52A1\u5668\u9519\u8BEF\uFF0C\u8BF7\u7A0D\u540E\u91CD\u8BD5');
    }
    return Promise.reject(e);
  },

  beforeRequestHook: (config, options) => {
    const { apiUrl, isJoinPrefix, urlPrefix, joinParamsToUrl, formatDate, joinTime = true } = options;

    if (isJoinPrefix && urlPrefix && isString(urlPrefix)) {
      config.url = `${urlPrefix}${config.url}`;
    }

    if (apiUrl && isString(apiUrl)) {
      config.url = `${apiUrl}${config.url}`;
    }
    const params = config.params || {};
    const data = config.data || false;

    if (formatDate && data && !isString(data)) {
      formatRequestDate(data);
    }
    if (config.method?.toUpperCase() === 'GET') {
      if (!isString(params)) {
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        config.url = `${config.url + params}${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else if (!isString(params)) {
      if (formatDate) {
        formatRequestDate(params);
      }
      if (
        Reflect.has(config, 'data') &&
        config.data &&
        (Object.keys(config.data).length > 0 || data instanceof FormData)
      ) {
        config.data = data;
        config.params = params;
      } else {
        config.data = params;
        config.params = undefined;
      }
      if (joinParamsToUrl) {
        config.url = setObjToUrlParams(config.url as string, { ...config.params, ...config.data });
      }
    } else {
      config.url += params;
      config.params = undefined;
    }
    return config;
  },

  requestInterceptors: (config, options) => {
    const userStore = useUserStore();
    const { token } = userStore;

    if (token) hasNotifiedUnauthorized = false;
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      (config as Recordable).headers.Authorization = options.authenticationScheme
        ? `${options.authenticationScheme} ${token}`
        : token;
    }
    const data = (config as Recordable)?.data;
    if (typeof FormData !== 'undefined' && data instanceof FormData) {
      (config as Recordable).headers = (config as Recordable).headers || {};
      delete (config as Recordable).headers['Content-Type'];
      delete (config as Recordable).headers['content-type'];
    }
    const currentPath = router.currentRoute.value?.path;
    if (currentPath) {
      (config as Recordable).headers = (config as Recordable).headers || {};
      (config as Recordable).headers['X-Page-Path'] = currentPath;
    }
    return config;
  },

  responseInterceptors: (res) => {
    return res;
  },

  responseInterceptorsCatch: (error: any, instance: AxiosInstance) => {
    const { config } = error;
    const status = error?.response?.status as number | undefined;

    if (status === 401) {
      return Promise.reject(error);
    }

    if (status === 403) {
      const serverMsg = error?.response?.data?.message;
      const url = String(error?.config?.url || '');
      if (!isSilent403Path(url)) {
        MessagePlugin.error(
          serverMsg || '\u6743\u9650\u4E0D\u8DB3\uFF0C\u8BF7\u8054\u7CFB\u7BA1\u7406\u5458\u5F00\u901A',
        );
      }
    }

    if (status && status >= 400 && status < 500) {
      return Promise.reject(error);
    }

    if (!config || !config.requestOptions.retry) return Promise.reject(error);

    config.retryCount = config.retryCount || 0;

    if (config.retryCount >= config.requestOptions.retry.count) {
      return Promise.reject(error);
    }

    config.retryCount += 1;

    const backoff = new Promise((resolve) => {
      setTimeout(() => {
        resolve(config);
      }, config.requestOptions.retry.delay || 1);
    });
    config.headers = { ...config.headers, 'Content-Type': ContentTypeEnum.Json };
    return backoff.then((config) => instance.request(config));
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    merge(
      <CreateAxiosOptions>{
        authenticationScheme: '',
        timeout: 10 * 1000,
        withCredentials: false,
        headers: { 'Content-Type': ContentTypeEnum.Json },
        transform,
        requestOptions: {
          apiUrl: host,
          isJoinPrefix: true,
          urlPrefix: import.meta.env.VITE_API_URL_PREFIX,
          isTransformResponse: true,
          joinParamsToUrl: false,
          ignoreCancelToken: false,
          dedupe: {
            enabled: true,
            strategy: 'cancelPrevious',
            windowMs: 1000,
            skipUrls: [/\/sse(\/|$)/i, /\/stream(\/|$)/i],
          },
          withToken: true,
          retry: {
            count: 3,
            delay: 1000,
          },
        },
      },
      opt || {},
    ),
  );
}
export const request = createAxios();
