import { defineConfig } from 'umi';

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
    { path: '/login', component: '@/pages/auth/login', layout: false },
    { path: '/403', component: '@/layouts/errors/Forbidden', layout: false },
    { path: '/404', component: '@/layouts/errors/NotFound', layout: false },
    { path: '/500', component: '@/layouts/errors/ServerError', layout: false },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', component: '@/pages/dashboard', access: 'canViewDashboard' },
    { path: '/account/center', component: '@/pages/account/center', access: 'canAccessAccountCenter' },
    { path: '/*', component: '@/pages/dynamic' },
  ],
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
});
