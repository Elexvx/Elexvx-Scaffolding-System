import { defineConfig } from '@umijs/max';

export default defineConfig({
  npmClient: 'npm',
  antd: {},
  model: {},
  initialState: {},
  access: {},
  request: {},
  layout: {},
  locale: false,
  hash: true,
  alias: {
    '@': '/src',
  },
  routes: [
    { path: '/login', component: '@/pages/auth/login' },
    { path: '/403', component: '@/layouts/errors/Forbidden' },
    { path: '/404', component: '@/layouts/errors/NotFound' },
    { path: '/500', component: '@/layouts/errors/ServerError' },
    {
      path: '/',
      component: '@/layouts/AppLayout',
      routes: [
        { path: '/', redirect: '/dashboard' },
        { path: '/dashboard', component: '@/pages/dashboard', access: 'canViewDashboard' },
        { path: '/account/center', component: '@/pages/account/center', access: 'canAccessAccountCenter' },
        { path: '/*', component: '@/pages/dynamic' },
      ],
    },
  ],
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
});
