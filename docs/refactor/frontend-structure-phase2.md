# 前端结构拆分（Phase 2）

## 目标

- 将 `system/user`、`system/menu` 从“初拆目录”推进到“职责深拆”。
- 将主组件收敛为页面编排层，减少单文件复杂度。
- 固化兼容壳策略，避免 `pages/views` 双主实现长期并存。

## 页面主实现放置规范

- 主实现目录：`frontend/src/views/**`
- 兼容入口目录：`frontend/src/pages/**`
- 入口关系：
  - `views/**/index.vue`：主实现入口
  - `pages/**/index.vue`：仅转发到 `views/**/index.vue`

## 第二轮已落地页面

### system/user

- 主页面：`views/system/user/components/UserPageMain.vue`
- 关键拆分：
  - `components/`：`UserToolbar`、`UserTable`、`UserFormDialog`、`UserResetPasswordDialog`、`UserOrgTree`
  - `hooks/`：`useUserPageState`、`useUserPermissions`、`useUserArea`、`useUserForm`、`useUserSearchForm`、`useUserTableColumns`、`useUserBatchActions`
  - `schema/`：`searchSchema`、`formSchema`
  - `constants/`：`userOptions`
  - `utils/`：`userMappers`、`userGuards`
  - `types.ts`：`UserRow/UserFormModel/UserSearchModel/RoleOption/OrgUnitNode` 等

### system/menu

- 主页面：`views/system/menu/components/MenuPageMain.vue`
- 关键拆分：
  - `components/`：`MenuToolbar`、`MenuTreeTable`、`MenuFormDialog`、`MenuIconPicker`、`MenuParentSelector`
  - `hooks/`：`useMenuPageState`、`useMenuPermissions`、`useMenuDialog`、`useMenuForm`、`useMenuDragSort`、`useMenuColumns`
  - `schema/`：`menuFormSchema`、`searchSchema`
  - `constants/`：`menuOptions`
  - `utils/`：`menuTree`、`menuMappers`、`componentPathCompat`
  - `types.ts`：`MenuNode/MenuFormModel/MenuType/MenuSubmitPayload/MenuComponentOption` 等

## 哪些文件只做兼容壳

- `frontend/src/pages/system/user/index.vue`
- `frontend/src/pages/system/menu/index.vue`
- `frontend/src/pages/system/role/index.vue`

兼容壳约束：

- 建议 5~15 行
- 仅 template + views 转发 import
- 不得包含业务状态、接口调用、表格表单弹窗实现

## 职责边界（可执行约束）

### components

- 负责 UI 渲染与事件分发
- 接收 props / emit 事件，不持有核心业务流程

### hooks

- 负责状态组织、页面交互流程、权限判断、弹窗开关、拖拽处理
- 允许调用 request/store，但应避免渲染细节

### schema

- 负责搜索模型、表单模型、校验规则、payload 结构约束

### constants

- 负责稳定枚举、默认选项、固定映射

### utils

- 负责纯函数：tree 计算、字段映射、路径兼容转换、slug/routeName 生成

### types

- 负责页面域类型集中定义，避免类型散落

## 迁移模板（复制到其他页面）

1. 在 `views/目标页面/` 建立标准骨架：
   - `components/ hooks/ schema/ constants/ utils/ types.ts index.vue`
2. 先把 `pages/目标页面/index.vue` 改成兼容壳转发。
3. 从主组件剥离：
   - 列定义 -> `hooks/useXxxColumns`
   - 搜索/表单模型 -> `schema/*`
   - 映射/树算法 -> `utils/*`
   - 弹窗和权限 -> `hooks/*`
4. 主组件只保留：
   - 页面布局
   - hooks 组装
   - 组件编排
   - 少量胶水流程
5. 执行构建与手工回归：
   - `npm --prefix frontend run build`
   - 重点回归：查询、增删改、弹窗、权限按钮、路由兼容

## 后续推广建议

- 以“兼容壳先收口 + 主实现再深拆”作为固定路径。
- 每页迁移完成后同步更新 refactor 文档，避免团队认知滞后。
- 新页面开发默认从 `views` 开始，不再在 `pages` 承载真实实现。
