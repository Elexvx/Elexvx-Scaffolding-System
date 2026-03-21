# frontend-role-and-setting-store-refactor

## 目标

- 深拆 `frontend/src/views/system/role/components/RolePageMain.vue`，将主组件控制在 400 行以内并下沉核心逻辑。
- 对 `frontend/src/store/modules/setting.ts` 做分层瘦身，保留对外导出兼容。

## Role 页面拆分结果

### 目录结构

- `frontend/src/views/system/role/components/RolePageMain.vue`
- `frontend/src/views/system/role/components/RoleToolbar.vue`
- `frontend/src/views/system/role/components/RoleTable.vue`
- `frontend/src/views/system/role/components/RoleFormDialog.vue`
- `frontend/src/views/system/role/hooks/useRoleApi.ts`
- `frontend/src/views/system/role/hooks/useRoleColumns.ts`
- `frontend/src/views/system/role/hooks/useRolePermissions.ts`
- `frontend/src/views/system/role/hooks/useRolePageState.ts`
- `frontend/src/views/system/role/constants/roleOptions.ts`
- `frontend/src/views/system/role/utils/roleMappers.ts`
- `frontend/src/views/system/role/types.ts`

### 职责重排

- `RolePageMain.vue` 仅保留布局编排、事件绑定、页面生命周期触发。
- `useRolePageState.ts` 承担表单状态、菜单树联动、动作权限映射、CRUD 编排。
- `useRoleColumns.ts` 承担表格列定义。
- `useRoleApi.ts` 承担接口访问。
- `useRolePermissions.ts` 承担权限位判断。
- `roleMappers.ts` 承担菜单树与权限映射纯函数。
- `RoleToolbar.vue/RoleTable.vue` 承担展示组件职责。

### 兼容策略

- 角色页 API 地址与 payload 语义保持不变：
  - `/system/role/list`
  - `/system/role/:id`
  - `/system/role`
  - `/system/menu/tree`
  - `/system/permission/catalog`
- 页面交互保持不变：新增、编辑、删除、菜单树勾选、动作权限勾选、管理员角色保护。

## setting store 分层结果

### 新目录

- `frontend/src/store/modules/setting/index.ts`
- `frontend/src/store/modules/setting/types.ts`
- `frontend/src/store/modules/setting/persistence.ts`
- `frontend/src/store/modules/setting/migration.ts`
- `frontend/src/store/modules/setting/layout.ts`
- `frontend/src/store/modules/setting/theme.ts`
- `frontend/src/store/modules/setting/loginPage.ts`
- `frontend/src/store/modules/setting/personalize.ts`
- `frontend/src/store/modules/setting.ts`（兼容出口）

### 分层职责

- `types.ts`：默认值与状态类型。
- `persistence.ts`：持久化路径与允许保存字段白名单。
- `migration.ts`：旧 localStorage key 迁移入口。
- `layout.ts`：`mode/sideMode` 联动归一化。
- `theme.ts`：自动主题、主题切换、侧边主题、品牌色注入。
- `loginPage.ts`：离线资源兜底与模拟 UI 配置。
- `personalize.ts`：后端 UI 响应映射为 store payload。
- `index.ts`：轻量编排层，协调加载、保存、更新逻辑。

### 兼容策略

- `frontend/src/store/modules/setting.ts` 保留导出入口：`export * from './setting/index'`。
- `useSettingStore/getSettingStore` API 保持不变。
- 持久化 key 仍为 `elexvx.setting`。
- 旧 key `setting` 的迁移逻辑保留。

## 测试与验证

- 后端新增：
  - `backend/src/test/java/elexvx/admin/service/menu/MenuPermissionResolverTest.java`
  - `backend/src/test/java/elexvx/admin/service/menu/MenuRouteAssemblerTest.java`
  - `backend/src/test/java/elexvx/admin/dto/UiSettingRequestMappingTest.java`
- 前端现状：
  - 当前仓库无可执行前端单测框架；
  - 本轮通过纯函数下沉（`roleMappers.ts`、`personalize.ts`、`layout.ts`）提高可测试性，并通过 `vue-tsc + build` 做类型与构建护栏。

## 仍存结构债

- `RoleFormDialog.vue` 尚可继续拆成“菜单树面板 + 动作权限面板”两个子组件。
- setting store 仍存在一定编排逻辑集中在 `index.ts`，后续可进一步拆分为加载编排器与保存编排器。
