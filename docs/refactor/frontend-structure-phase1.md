# 前端结构拆分（Phase 1）

## 改了什么

- 将 `system/user` 与 `system/menu` 的主实现迁移到 `src/views/system/**`。
- 新增页面分层骨架：
  - `index.vue`（页面入口编排）
  - `components/*`（页面实现组件）
  - `hooks/*`（table/form/dialog 状态与逻辑）
  - `schema.ts`（页面类型）
- `system/user`：
  - 主实现：`src/views/system/user/components/UserPageMain.vue`
  - hooks：`useUserTable.ts`、`useUserForm.ts`、`useUserDialog.ts`
  - 类型：`src/views/system/user/schema.ts`
- `system/menu`：
  - 主实现：`src/views/system/menu/components/MenuPageMain.vue`
  - hooks：`useMenuTable.ts`、`useMenuForm.ts`、`useMenuDialog.ts`
  - 类型：`src/views/system/menu/schema.ts`
- 路由动态组件解析默认优先目录由 `pages` 调整为 `views`，确保新主实现生效。

## 为什么改

- 旧结构以 `pages` 为主，`views` 多为壳转发，迁移不彻底。
- 千行页面持续增长，维护与回归成本高。
- 需要先在代表性页面建立可复制的拆分模式，再推广全局。

## 兼容策略

- 保留 `src/pages/system/user/index.vue` 兼容壳，转发到 `views` 主实现。
- 动态路由解析仍同时支持 `pages` 与 `views`，老菜单 component 不改库可继续运行。
- 菜单组件选择器支持扫描 `pages/views` 两目录并去重。

## 后续还要做什么

- 将 `src/pages/system/menu/index.vue` 收敛为兼容壳，逐步减少双实现维护。
- 持续将页面内 `any` 收敛为明确类型。
- 将 `components/UserPageMain.vue`、`components/MenuPageMain.vue` 继续下沉为更细组件。

## 相关目录路径

- `frontend/src/views/system/user/`
- `frontend/src/views/system/menu/`
- `frontend/src/pages/system/user/index.vue`
- `frontend/src/pages/system/menu/index.vue`
- `frontend/src/utils/route/index.ts`
