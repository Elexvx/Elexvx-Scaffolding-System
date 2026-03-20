# 前端结构拆分（Phase 3）

## 目标与范围

- 目标：完成第三轮重页面“真迁移 + 深拆分”，统一 `views` 主实现与 `pages` 兼容壳策略。
- 范围：
  - `system/org`
  - `user`
  - `system/dict`
  - `system/sensitive`
  - `console/download`

## 核心原则

### views 作为主实现目录

- 页面真实业务实现全部放在 `frontend/src/views/**`。
- `views/**/index.vue` 作为页面主入口，负责装配 `components/hooks`。
- 列定义、表单模型、映射函数、权限判断不得回流到 `pages/**`。

### pages 只做兼容壳

- `frontend/src/pages/**/index.vue` 仅保留旧路径兼容入口。
- 兼容壳目标 5~15 行，只允许模板转发和 `views` import。
- 兼容壳中禁止出现表格、表单、弹窗、请求流程、业务状态。

## 本轮页面拆分结果

### system/org

- 主实现：`views/system/org/components/OrgPageMain.vue`
- 拆分：
  - `components`：工具栏、表格、编辑弹窗、组织用户选择
  - `hooks`：页面状态、树处理、列定义、表单与弹窗流程、权限
  - `schema/constants/utils/types`：搜索与表单结构、选项枚举、树映射与守卫、域类型

### user

- 主实现：`views/user/components/UserProfilePageMain.vue`
- 拆分：
  - `components`：资料总览、基础资料、安全设置、偏好设置、密码弹窗、头像上传
  - `hooks`：资料状态、编辑表单、密码流程、头像上传、偏好状态
  - `schema/constants/utils/types`：资料/密码/偏好 schema，映射与校验，用户域类型

### system/dict

- 主实现：`views/system/dict/components/DictPageMain.vue`
- 拆分：
  - `components`：页面编排与展示
  - `hooks`：字典类型/字典项联动、检索、弹窗、行操作流程
  - `schema/constants/types`：字典表单与查询结构、选项常量、字典域类型

### system/sensitive

- 主实现：`views/system/sensitive/components/SensitivePageMain.vue`
- 拆分：
  - `components`：工具栏、表格、批量操作、表单弹窗
  - `hooks`：列表请求、列定义、检索模型、弹窗流程、权限与批量动作
  - `schema/constants/utils/types`：敏感词表单与检索结构、选项常量、状态映射与守卫、域类型

### console/download

- 主实现：`views/console/download/components/DownloadPageMain.vue`
- 拆分：
  - `components`：工具栏、表格、编辑弹窗、详情抽屉
  - `hooks`：页面状态、列表与列定义、检索模型、表单流程、详情开关、权限
  - `schema/constants/utils/types`：资源表单/查询结构、选项常量、状态映射与守卫、资源域类型

## 职责边界模板

- `components/`：只渲染 UI 与抛出事件，不持有核心业务流。
- `hooks/`：组织页面状态、调用接口、处理交互流程、承载权限判断。
- `schema/`：集中定义搜索表单、编辑表单、校验规则与 payload 结构。
- `constants/`：维护稳定枚举、状态选项、展示常量。
- `utils/`：纯函数与映射逻辑，不耦合渲染上下文。
- `types.ts`：统一页面域类型，避免类型散落与循环引用。

## 兼容策略

- 路由与菜单仍指向历史 `pages` 路径。
- `pages/**/index.vue` 通过组件转发保持外部路径不变。
- 对仍有历史引用的 helper，采用最小转发到 `views` 实现，避免重复维护。

## 后续迁移模板（可复用）

1. 在 `views/目标页` 建立标准骨架：`components/hooks/schema/constants/utils/types.ts/index.vue`。
2. 先把 `pages/目标页/index.vue` 收口为兼容壳，锁定路径兼容。
3. 从旧主文件按“列定义、schema、映射、权限、弹窗”顺序下沉。
4. 主组件只保留布局、hooks 装配、组件编排和最少胶水逻辑。
5. 迁移后执行 build 与关键业务回归，再清理残留重复实现。

## 建议继续迁移页面

- `frontend/src/pages/system/role/index.vue`
- `frontend/src/pages/system/config/index.vue`
- `frontend/src/pages/system/dept/index.vue`
- `frontend/src/pages/system/post/index.vue`
- `frontend/src/pages/console/file/index.vue`


## Phase 4 衔接说明

- 第四轮已继续完成 `announcement/table`、`notification/table`、`system/monitor/redis`、`system/security` 的真迁移。
- 新增文档：`docs/refactor/frontend-structure-phase4.md`。
- 对 `announcement/cards`、`system/verification`、`system/storage` 先执行了最小兼容收口，作为后续深拆起点。
- 后续开发请优先按 Phase 4 中的目录边界继续治理剩余大页面。
