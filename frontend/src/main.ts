/* eslint-disable simple-import-sort/imports */
/**
 * 应用启动入口。
 *
 * 这里负责完成全局能力的装配（UI 组件库 / 状态管理 / 路由 / i18n），以及引入全局路由守卫（permission.ts）。
 * 注意：`./permission` 仅用于副作用注册（router.beforeEach/afterEach/onError），不需要显式导出。
 */
import { createApp } from 'vue';
import TDesign from 'tdesign-vue-next';

import App from './App.vue';
import router from './router';
import { store } from './store';
import i18n from './locales';
import { installPermDirective } from '@/directives/perm';

import 'tdesign-vue-next/es/style/index.css';
import '@/style/index.less';
import './permission';

const app = createApp(App);

app.use(TDesign);
app.use(store);
app.use(router);
app.use(i18n);
installPermDirective(app);

app.mount('#app');
