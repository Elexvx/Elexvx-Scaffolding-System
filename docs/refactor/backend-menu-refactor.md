# backend-menu-refactor

## 背景与目标

- `backend/src/main/java/elexvx/admin/service/MenuItemService.java` 职责混杂，查询、写操作、动态路由、权限裁剪、维护逻辑耦合在一个类中，改动风险高。
- 本次将菜单领域拆为查询、命令、维护、路由组装、权限解析五类职责，并保持控制器 URL 与接口语义不变。

## 新结构

- `backend/src/main/java/elexvx/admin/service/menu/MenuFacadeService.java`
- `backend/src/main/java/elexvx/admin/service/menu/query/MenuQueryService.java`
- `backend/src/main/java/elexvx/admin/service/menu/query/MenuRouteQueryService.java`
- `backend/src/main/java/elexvx/admin/service/menu/command/MenuMutationService.java`
- `backend/src/main/java/elexvx/admin/service/menu/command/MenuMaintenanceService.java`
- `backend/src/main/java/elexvx/admin/service/menu/support/MenuPermissionResolver.java`
- `backend/src/main/java/elexvx/admin/service/menu/support/MenuRouteAssembler.java`
- `backend/src/main/java/elexvx/admin/service/menu/support/MenuTreeBuilder.java`
- `backend/src/main/java/elexvx/admin/service/menu/support/MenuSeedCatalog.java`
- `backend/src/main/java/elexvx/admin/service/menu/support/MenuSeedNode.java`

## 职责边界

- 查询边界：
  - `MenuQueryService` 仅负责菜单是否已配置、后台树查询。
  - `MenuRouteQueryService` 仅负责当前用户动态路由查询编排。
- 写操作边界：
  - `MenuMutationService` 负责菜单增删改、排序、约束校验、并发版本校验、角色菜单关联清理。
- 维护边界：
  - `MenuMaintenanceService` 负责默认菜单种子、过时路由清理、启动维护任务。
- 支撑边界：
  - `MenuPermissionResolver` 负责用户可见菜单裁剪与父节点补齐。
  - `MenuRouteAssembler` 负责动态路由树与 `requiredPermissions` 组装。
  - `MenuTreeBuilder` 负责树结构转换与节点映射。

## 控制器与兼容策略

- `MenuController`、`SystemMenuController` 改为依赖 `MenuFacadeService`，URL 与返回语义保持不变。
- `MenuMaintenanceRunner` 改为调用 `MenuFacadeService`，维护行为保持不变。
- 动态菜单兼容：
  - 仍返回 `RouteItem/RouteMeta` 结构。
  - 仍按 `actions` 生成 `system:{resource}:{action}` 权限码。
  - 仍对空 `LAYOUT` 节点做裁剪。

## 测试护栏

- `backend/src/test/java/elexvx/admin/service/menu/MenuPermissionResolverTest.java`
  - 覆盖可访问子菜单时父节点自动补齐。
- `backend/src/test/java/elexvx/admin/service/menu/MenuRouteAssemblerTest.java`
  - 覆盖动态路由权限码组装与空布局裁剪。

## 风险与后续

- 风险点：
  - 默认菜单种子清单改动仍可能影响新环境初始化的一致性。
  - `MenuMutationService` 校验规则较多，后续新增节点类型需同步更新校验矩阵。
- 后续建议：
  - 将 `MenuMutationService` 的校验规则进一步下沉为独立策略对象。
  - 为 `reorder` 增加更多边界测试（跨层移动、并发版本冲突、非法 parent）。
