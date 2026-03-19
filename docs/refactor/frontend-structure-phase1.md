# 前端结构拆分（Phase 1）

## 范围与目标

- 目标从“目录迁移”升级为“主实现收敛 + 职责边界稳定”。
- 页面主实现统一放在 `src/views/**`，`src/pages/**` 仅保留兼容入口。
- 建立可复制分层：`components/hooks/schema/constants/utils/types`。

## 第一轮与第二轮结果总览

- 已完成 `system/user`、`system/menu` 两个重点页面的第二轮深拆分。
- 已将 `src/pages/system/user/index.vue`、`src/pages/system/menu/index.vue` 收敛为薄兼容壳。
- 同步清理同类双主实现并存问题：`src/pages/system/role/index.vue` 也改为兼容壳。

## system/user 第二轮深拆分

- 主组件目标：`UserPageMain.vue` 控制在 400 行以内，当前已达成。
- 拆分内容：
  - 表格列定义下沉到 `hooks/useUserTableColumns.ts`
  - 搜索模型与重置下沉到 `hooks/useUserSearchForm.ts` + `schema/searchSchema.ts`
  - 表单模型与规则下沉到 `hooks/useUserForm.ts` + `schema/formSchema.ts`
  - 区域映射与联动下沉到 `hooks/useUserArea.ts`
  - 权限判断下沉到 `hooks/useUserPermissions.ts`
  - 批量行为能力下沉到 `hooks/useUserBatchActions.ts`
  - 类型集中在 `types.ts`
  - 组件拆分为 `UserToolbar/UserTable/UserFormDialog/UserResetPasswordDialog/UserOrgTree`
- 结果：主组件只保留布局、hooks 组装、事件编排与少量胶水逻辑。

## system/menu 第二轮深拆分

- 主组件目标：`MenuPageMain.vue` 控制在 400 行以内，当前已达成。
- 拆分内容：
  - 菜单类型/动作常量下沉到 `constants/menuOptions.ts`
  - 表单 schema 与重置下沉到 `schema/menuFormSchema.ts`
  - 搜索 schema 下沉到 `schema/searchSchema.ts`
  - component 路径兼容映射下沉到 `utils/componentPathCompat.ts`
  - 树过滤、路径计算、排序 payload 下沉到 `utils/menuTree.ts`
  - 表单映射、路由字段补全、提交 payload 下沉到 `utils/menuMappers.ts`
  - 列定义下沉到 `hooks/useMenuColumns.tsx`
  - 拖拽/移动交互下沉到 `hooks/useMenuDragSort.tsx`
  - 页面状态、权限、弹窗、表单联动拆分到 `useMenuPageState/useMenuPermissions/useMenuDialog/useMenuForm`
  - 组件拆分为 `MenuToolbar/MenuTreeTable/MenuFormDialog/MenuIconPicker/MenuParentSelector`
  - 类型集中到 `types.ts`
- 结果：主组件只保留页面编排与流程编排，不再承担完整实现细节。

## pages 兼容壳策略

- 规则：`pages` 目录只允许存在薄入口，建议 5~15 行。
- 兼容壳只做一件事：转发 `views` 对应页面，不承载表格、表单、弹窗、schema、业务逻辑。
- 当前已收口的兼容壳：
  - `src/pages/system/user/index.vue`
  - `src/pages/system/menu/index.vue`
  - `src/pages/system/role/index.vue`

## views 作为主实现目录的原则

- 统一入口：`src/views/**/index.vue` 是页面入口，负责挂载主页面组件。
- 主实现归位：页面主逻辑在 `src/views/**/components/*PageMain.vue` 及其分层模块。
- 禁止回流：新功能不得再写回 `src/pages/**` 业务实现。
- 保持兼容：动态路由继续支持旧 `pages` component 路径，但解析优先 `views`。

## 分层职责边界（标准）

- `components/`：UI 组件与事件上抛，不直接承载复杂业务流程。
- `hooks/`：页面状态管理、流程编排、可复用交互逻辑。
- `schema/`：表单模型、搜索模型、规则与提交字段结构。
- `constants/`：稳定枚举、默认选项、静态映射常量。
- `utils/`：纯函数工具，如 mappers/tree/path 兼容转换。
- `types.ts`：页面域类型集中定义，避免类型散落在 SFC 内。

## 迁移执行顺序（建议）

1. 先收口 `pages` 为兼容壳，阻断双主实现并存。
2. 在 `views` 建立标准目录骨架。
3. 先下沉 `types/constants/schema`，再下沉 `utils/hooks/components`。
4. 将主组件压到“编排层”后，再做行为回归与构建验证。

## 相关文档

- 第二轮详单与模板：`docs/refactor/frontend-structure-phase2.md`
