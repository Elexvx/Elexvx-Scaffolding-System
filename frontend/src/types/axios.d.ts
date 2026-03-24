import type { AxiosRequestConfig } from 'axios';

/**
 * Axios请求配置
 */
export interface RequestOptions {
  /**
   * 接口地址
   *
   * 例: http://www.baidu.com/api
   */
  apiUrl?: string;
  /**
   * 是否自动添加接口前缀
   *
   * 例: http://www.baidu.com/api
   * urlPrefix: 'api'
   */
  isJoinPrefix?: boolean;
  /**
   * 接口前缀
   */
  urlPrefix?: string;
  /**
   * POST请求的时候添加参数到Url中
   */
  joinParamsToUrl?: boolean;
  /**
   * 格式化提交参数时间
   */
  formatDate?: boolean;
  /**
   * 是否需要对响应数据进行处理
   */
  isTransformResponse?: boolean;
  /**
   * 是否返回原生响应头
   *
   * 例: 需要获取响应头时使用该属性
   */
  isReturnNativeResponse?: boolean;
  /**
   * 是否忽略请求取消令牌
   *
   * 如果启用，则重复请求时不进行处理
   *
   * 如果禁用，则重复请求时会取消当前请求
   */
  ignoreCancelToken?: boolean;
  /**
   * 自动对请求添加时间戳参数
   */
  joinTime?: boolean;
  /**
   * 请求去重配置
   */
  dedupe?: RequestDedupeOptions;
  /**
   * 是否携带Token
   */
  withToken?: boolean;
  /**
   * 重试配置
   */
  retry?: {
    /**
     * 重试次数
     */
    count: number;
    /**
     * 隔多久重试
     *
     * 单位: 毫秒
     */
    delay: number;
  };
  /**
   * 接口级节流
   *
   * 单位: 毫秒
   */
  throttle?: {
    delay: number;
  };
  /**
   * 接口级防抖
   *
   * 单位: 毫秒
   */
  debounce?: {
    delay: number;
  };
}

export type RequestDedupeStrategy = 'cancelPrevious' | 'cancelCurrent';

export interface RequestDedupeOptions {
  /**
   * 是否启用请求去重
   */
  enabled?: boolean;
  /**
   * 重复请求处理策略
   * cancelPrevious: 取消前一个并继续当前请求
   * cancelCurrent: 取消当前请求并保留前一个请求
   */
  strategy?: RequestDedupeStrategy;
  /**
   * 去重时间窗口（毫秒）
   */
  windowMs?: number;
  /**
   * 白名单 URL（匹配到则跳过去重）
   */
  skipUrls?: Array<string | RegExp>;
}

export interface Result<T = any> {
  code: number;
  data: T;
  message?: string;
}

export interface AxiosRequestConfigRetry extends AxiosRequestConfig {
  retryCount?: number;
}
