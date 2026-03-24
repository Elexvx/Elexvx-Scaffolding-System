# 前端结构拆分（Phase 4）

## 本轮迁移页面

本轮完成以下页面从 `frontend/src/pages/**` 向 `frontend/src/views/**` 的主实现迁移：

- `announcement/table` → `frontend/src/views/announcement/table/`
- `notification/table` → `frontend/src/views/notification/table/`
- `system/monitor/redis` → `frontend/src/views/system/monitor/redis/`
- `system/security` → `frontend/src/views/system/security/`

## 主实现目录落点

- `frontend/src/views/announcement/table/index.vue`
- `frontend/src/views/notification/table/index.vue`
- `frontend/src/views/system/monitor/redis/index.vue`
- `frontend/src/views/system/security/index.vue`

## 兼容壳原则

- `frontend/src/pages/**/index.vue` 仅保留旧路由 / 动态菜单兼容入口。
- 兼容壳控制在 5~15 行，禁止继续承载表格、表单、弹窗、接口流程、权限判断等真实实现。
- 后端菜单 `component` 仍可继续指向旧 `pages` 路径，不影响历史配置。

## 本轮拆分边界

### announcement/table

- `components/`：页面编排、工具栏、表格、编辑抽屉。
- `hooks/`：页面状态、表格请求、列定义、表单流程、发布/删除动作、权限开关。
- `schema/`：搜索模型与表单规则。
- `constants/`：状态/优先级/类型兜底选项与附件限制。
- `utils/`：上传响应格式化、状态映射、payload 构建、错误处理。
- `types.ts`：公告查询、表单、上传上下文类型。

### notification/table

- 沿用与 `announcement/table` 一致的分层方式，确保通知与公告两页结构完全对齐。
- 批量流程本轮未新增，保留当前单行操作语义不变。

### system/monitor/redis

- `components/`：主页面编排、刷新工具栏、统计卡片、命令/Key 表格。
- `hooks/`：监控状态、轮询刷新、图表渲染、权限占位。
- `constants/`：刷新频率、表格列定义。
- `utils/`：Redis 指标转换、响应映射、历史数据处理。
- `types.ts`：Redis 监控数据结构。

### system/security

- `components/`：主页面编排、Token 策略表单、验证码策略表单、密码策略表单、防御阈值表单、保存工具条。
- `hooks/`：页面状态、统一配置读写、验证码关闭确认流程、权限占位。
- `constants/`：默认安全配置。
- `utils/`：后端设置合并与字段规范化。
- `types.ts`：安全表单与 tab 类型。

## 本轮额外最小收口

以下页面本轮只做“兼容壳化 + 建立 views 主入口”的最小整理，暂未进行深拆：

- `frontend/src/pages/announcement/cards/index.vue`
- `frontend/src/pages/system/verification/index.vue`
- `frontend/src/pages/system/storage/index.vue`

对应主入口：

- `frontend/src/views/announcement/cards/index.vue`
- `frontend/src/views/system/verification/index.vue`
- `frontend/src/views/system/storage/index.vue`

## 尚未迁移完的遗留页面

以下页面仍建议后续继续治理：

- `frontend/src/pages/message/send/index.vue`
- `frontend/src/pages/system/log/index.vue`
- `frontend/src/pages/system/modules/index.vue`
- `frontend/src/pages/system/monitor/online-user/index.vue`
- `frontend/src/pages/system/monitor/server/index.vue`
- `frontend/src/pages/system/watermark/index.vue`

## 下一轮建议优先级

1. `frontend/src/pages/system/modules/index.vue`
2. `frontend/src/pages/system/log/index.vue`
3. `frontend/src/pages/system/monitor/server/index.vue`
4. `frontend/src/pages/system/monitor/online-user/index.vue`
5. `frontend/src/pages/message/send/index.vue`

## 可复用迁移步骤

1. 在 `views/目标路径` 下建立 `index.vue` 主入口。
2. 旧 `pages` 页面先收口为薄兼容壳，锁定路径兼容。
3. 将列表、表单、弹窗、列定义、schema、映射、权限、状态流逐项下沉到 `components/hooks/schema/constants/utils/types`。
4. 主组件只保留页面布局、hooks 装配、组件编排与少量胶水逻辑。
5. 迁移完成后统一执行 `npm --prefix frontend run build` 验证。


## Phase 5 衔接说明

- 登录页与 `system/personalize` 的第五轮治理已单独记录在 `docs/refactor/frontend-structure-phase5.md`。
- 后续继续处理认证页与个性化面板时，以 Phase 5 中的 `views/login + components/hooks/schema/constants/utils/types` 约束为准。
