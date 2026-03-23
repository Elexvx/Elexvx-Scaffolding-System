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

## 8. 后续迁移建议

- 继续迁移监控、模块管理、敏感词、存储配置、安全设置等页面到统一模板。
- 补齐富文本、水印、会话监听的完整 UI 与交互。
- 将筛选抽屉、移动端操作收纳能力推广到所有列表页。
- 增加接口契约测试与页面级回归测试。
