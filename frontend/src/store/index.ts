/**
 * Pinia Store 入口。
 *
 * - createPersistedState：为各 store 提供持久化能力（localStorage/sessionStorage）
 * - setActivePinia：允许在组件外（例如 router 守卫、request 拦截器）直接调用 useXxxStore()
 * - 统一 export：对外暴露 store 实例与各业务 store 的快捷导出
 */
import { createPinia, setActivePinia } from 'pinia';
import { createPersistedState } from 'pinia-plugin-persistedstate';

const store = createPinia();
store.use(createPersistedState());
setActivePinia(store);

export { store };

export * from './modules/app';
export * from './modules/dictionary';
export * from './modules/notification';
export * from './modules/permission';
export * from './modules/setting';
export * from './modules/tabs-router';
export * from './modules/user';

export default store;
