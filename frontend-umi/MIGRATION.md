# 前端迁移说明（Vue3 -> Umi Max）

## 1. 重构范围与目标

本次在仓库根目录新增 `frontend-umi`，不改动旧 `frontend` 与 `backend`。  
核心目标是建立可持续迁移的 React 后台骨架，并兼容现有后端接口与权限模型。

## 2. 后端接口兼容策略

- 保留登录、用户信息、菜单、用户、角色、组织、公告、消息、通知、系统配置、日志接口路径。
- 统一请求层沿用后端信封协议（`code/message/success/data`）并做 `unwrap`。
- 对分页返回做兼容归一（`records/list/items` + `total/count`）。
- 登录态继续使用 Token 模型，401 自动回登录，403 跳转权限页。

## 3. 动态菜单与路由安全改造

- 后端仍返回菜单树，前端在 `services/system/menu.ts` 做 adapter。
- 菜单数据转换为内部 `AppMenuItem`，保留 `path/name/icon/permCode/componentKey`。
- 页面渲染不接受后端组件路径直连，统一走 `componentRegistry` 白名单解析。
- 动态页根据当前 `pathname` + `componentKey` 渲染组件，未命中返回 404。

## 4. 已完成模块

- 登录与退出、个人中心、工作台
- 用户管理、角色管理、菜单管理、组织管理
- 公告管理、消息管理、通知管理
- 系统配置页、操作日志页
- 字典管理、模块管理、敏感词管理、水印配置
- 监控页：在线用户、Redis 监控、服务器监控

## 5. 占位能力

- 富文本能力占位
- 水印能力占位
- 会话监听与并发登录提示占位

以上能力保留接口兼容点，后续按业务优先级继续落地。

## 6. 移动端与响应式落地

- 布局在小屏自动折叠侧边导航。
- 列表页使用统一组件：桌面表格 + 移动端卡片列表降级。
- 查询区支持移动端筛选抽屉。
- 操作区支持“主操作 + 更多”收纳模式。

## 7. 知识产权与代码署名落地情况

### 7.1 品牌配置统一入口

- 新增 `src/config/brand.ts` 统一维护：
  - `brandName`
  - `productName`
  - `copyrightOwner`
  - `copyrightStartYear`
  - `defaultDocumentTitle`
  - `defaultFooterText`
  - `exportFilePrefix`
  - 轻量版权展示开关
- 登录页、布局品牌区、页脚文案均从该配置读取。

### 7.2 核心模块头注释落地

仅在核心基础设施添加简洁头注释：

- `src/app.tsx`
- `src/access.ts`
- `src/config/brand.ts`
- `src/utils/request.ts`
- `src/layouts/AppLayout.tsx`
- `src/components/core/PageScaffold.tsx`
- `src/components/core/PermissionButton.tsx`
- `src/pages/dynamic/componentRegistry.ts`

### 7.3 明确不添加版权头的文件类型

以下文件不批量加版权头：

- 普通 CRUD 页面
- 简单 hooks 与工具函数
- 样式文件
- 纯类型文件
- 测试文件与临时脚本

原因：保持仓库干净、降低噪音、便于二次开发，仅在关键基础设施表达归属。

## 8. 旧前端页面 -> 新前端页面映射表

> 口径：以 `frontend/src/pages` 为准，正式业务页优先迁移；`example/*` 与大多数 `result/*` 归类为非正式业务页。

| 旧页面路径 | 新页面路径 | 迁移状态 | 备注 |
| --- | --- | --- | --- |
| `login/index.vue` | `src/pages/auth/login/index.tsx` | 已迁移 | 登录主流程一对一迁移。 |
| `console/download/index.vue` | - | 待迁移 | 下载中心能力尚未进入本轮。 |
| `announcement/cards/index.vue` | `src/pages/system/announcement/index.tsx` | 已合并 | 与 `announcement/table` 合并为统一公告页。 |
| `announcement/table/index.vue` | `src/pages/system/announcement/index.tsx` | 已合并 | 卡片/表格模式统一承接。 |
| `message/send/index.vue` | `src/pages/system/message/index.tsx` | 已合并 | 发送与列表能力先合并在消息页落点。 |
| `notification/table/index.vue` | `src/pages/system/notification/index.tsx` | 已合并 | 通知列表统一迁入通知页。 |
| `system/menu/index.vue` | `src/pages/system/menu/index.tsx` | 已迁移 | 一对一迁移。 |
| `system/org/index.vue` | `src/pages/system/org/index.tsx` | 已迁移 | 一对一迁移。 |
| `system/role/index.vue` | `src/pages/system/role/index.tsx` | 已迁移 | 一对一迁移。 |
| `system/user/index.vue` | `src/pages/system/user/index.tsx` | 已迁移 | 一对一迁移。 |
| `system/dict/index.vue` | `src/pages/system/dict/index.tsx` | 已迁移 | 新增正式字典管理页。 |
| `system/modules/index.vue` | `src/pages/system/modules/index.tsx` | 已迁移 | 新增正式模块管理页。 |
| `system/sensitive/index.vue` | `src/pages/system/sensitive/index.tsx` | 已迁移 | 新增正式敏感词管理页。 |
| `system/watermark/index.vue` | `src/pages/system/watermark/index.tsx` | 已迁移 | 新增独立水印配置页。 |
| `system/monitor/online-user/index.vue` | `src/pages/system/monitor/online-user/index.tsx` | 已迁移 | 新增在线用户监控页。 |
| `system/monitor/redis/index.vue` | `src/pages/system/monitor/redis/index.tsx` | 已迁移 | 新增 Redis 监控页。 |
| `system/monitor/server/index.vue` | `src/pages/system/monitor/server/index.tsx` | 已迁移 | 新增服务器监控页。 |
| `system/log/index.vue` | `src/pages/system/monitor/log/index.tsx` | 已合并 | 日志页统一落点到 monitor/log。 |
| `system/personalize/index.vue` | `src/pages/system/config/index.tsx` | 已合并 | 个性化配置合并到系统配置页。 |
| `system/security/index.vue` | `src/pages/system/config/index.tsx` | 已合并 | 安全配置合并到系统配置页。 |
| `system/storage/index.vue` | `src/pages/system/config/index.tsx` | 已合并 | 存储配置合并到系统配置页。 |
| `system/verification/index.vue` | `src/pages/system/config/index.tsx` | 已合并 | 验证配置合并到系统配置页。 |
| `user/index.vue` | `src/pages/account/center/index.tsx` | 已合并 | 用户中心落点到个人中心页。 |
| `example/goods/index.vue` | - | 废弃 | 模板示例页，不进入正式菜单。 |
| `example/order/index.vue` | - | 废弃 | 模板示例页，不进入正式菜单。 |
| `result/403/index.vue` | `src/layouts/errors/Forbidden.tsx` | 已迁移 | 保留必要异常页能力。 |
| `result/404/index.vue` | `src/layouts/errors/NotFound.tsx` | 已迁移 | 保留必要异常页能力。 |
| `result/500/index.vue` | `src/layouts/errors/ServerError.tsx` | 已迁移 | 保留必要异常页能力。 |
| `result/browser-incompatible/index.vue` | - | 废弃 | 纯结果页，当前不纳入正式迁移范围。 |
| `result/fail/index.vue` | - | 废弃 | 纯结果页，当前不纳入正式迁移范围。 |
| `result/maintenance/index.vue` | - | 废弃 | 纯结果页，当前不纳入正式迁移范围。 |
| `result/network-error/index.vue` | - | 废弃 | 纯结果页，当前不纳入正式迁移范围。 |
| `result/success/index.vue` | - | 废弃 | 纯结果页，当前不纳入正式迁移范围。 |

## 9. 后续迁移建议

- 继续补齐 `console/download` 下载中心正式能力。
- 将系统配置合并页中的个性化 / 验证 / 安全 / 存储拆分为可选子页（按权限决定是否独立展示）。
- 补齐水印、Redis、服务器监控的编辑与图表化交互。
- 增加接口契约测试与页面级回归测试。
