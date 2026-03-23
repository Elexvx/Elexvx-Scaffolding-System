# frontend-umi

Elexvx 后台重构新前端（React 18 + TypeScript + Umi Max + Ant Design + ProComponents）。

## 快速开始

```bash
cd frontend-umi
npm install
npm run dev
```

默认使用 `/api` 作为后端前缀，可通过 `.env.local` 覆盖：

```bash
UMI_APP_API_PREFIX=/api
```

## 构建

```bash
npm run build
npm run start
```

## 当前能力

- 登录鉴权、401/403 跳转、统一请求注入 Token
- `getInitialState + access` 权限模型
- 后端菜单适配器 + `componentKey` 白名单组件注册
- 统一页面模板（标题区/查询区/操作区/内容区）
- 列表页移动端卡片化降级、筛选抽屉、移动端主操作与更多菜单
- 首批核心模块：用户、角色、菜单、组织、公告、消息、通知、系统配置、操作日志

## 目录结构

```text
src
├── app.tsx
├── access.ts
├── config
├── components
├── layouts
├── pages
├── services
├── types
└── utils
```

## 迁移说明

详细迁移策略、兼容说明与后续计划见 [MIGRATION.md](./MIGRATION.md)。
