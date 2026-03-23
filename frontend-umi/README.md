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

- 登录鉴权、401/403/500 跳转、统一请求注入 Token
- `getInitialState + access + layout` 的 Umi Max 运行时模型
- 后端真实菜单适配器 + `componentKey / routeName / path` 白名单组件注册
- 统一页面骨架（标题区 / 查询区 / 操作区 / 内容区）
- 列表页移动端卡片化降级、筛选抽屉、移动端主操作与更多菜单
- 首批核心模块：用户、角色、菜单、组织、公告、消息、通知、系统配置、操作日志

## 版本策略（SemVer）

当前重构基线版本：`0.1.0`。

- **主版本**：发生不兼容变更时递增。
- **次版本**：新增向下兼容功能时递增。
- **修订号**：向下兼容的问题修复时递增。
- **预发布**：使用 `-alpha` / `-beta` / `-rc` 标识预发布阶段。

## 轻量品牌说明

仅在以下位置保留少量 Elexvx 品牌信息：

- 登录页
- 浏览器 / 布局标题
- 运行时核心模块头注释
- README / CHANGELOG
- 本地存储命名空间

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
