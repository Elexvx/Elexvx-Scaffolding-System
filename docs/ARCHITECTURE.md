# 架构与实现说明（当前状态）

## 总体架构

- 前后端分离：前端基于 Vue 3 + TDesign，后端基于 Spring Boot + MyBatis。
- 数据与缓存：关系型数据库（默认 MySQL，多库脚本保留）+ Redis（会话与运行态能力）。
- 数据库演进入口已切换为 Flyway，`database/*.sql` 为历史参考。

## 后端分层

- Controller：接口入口、参数校验、协议适配。
- Service：业务编排与子域实现。
- Mapper/DAO：持久化访问、批量与组合查询。
- DTO/VO/Entity：输入、输出、持久化模型分层。
- Config/Web：安全链路、过滤器、统一异常与响应写出。

## 统一响应与链路追踪

- 统一响应结构：`success/code/message/userTip/data/requestId/timestamp/path`。
- 统一响应工厂：`backend/src/main/java/elexvx/admin/web/ApiResponses.java`。
- 统一异常收口：`GlobalExceptionHandler` + `SecurityConfig.writeError`。
- requestId 链路：`RequestIdFilter` 负责请求头读取/生成、响应头回写、MDC 注入与清理。

## 认证域（Phase 1）

- `AuthService` 已收敛为轻量门面，控制器入口保持不变。
- 子服务拆分：
  - `AuthLoginService`
  - `ConcurrentLoginOrchestrator`
  - `AuthRegisterService`
  - `PasswordResetService`
  - `CurrentUserProfileService`
- auth 域模型示范：`service/auth/model/{req,resp,command,query}`。

## 权限与动态菜单

- 菜单接口：`GET /get-menu-list-i18n`。
- 前端守卫：首次加载用户与菜单后动态 `addRoute`。
- 动态组件解析支持 `pages/views` 双目录；当前默认优先 `views`，`pages` 作为兼容回退。

## 前端页面结构（Phase 1）

- `system/user` 与 `system/menu` 主实现迁移到 `src/views/system/**`。
- 页面拆分骨架：`index.vue` + `components/*` + `hooks/*` + `schema.ts`。
- `src/pages` 保留兼容入口，避免历史菜单 component 配置失效。

## 文件与对象存储

- 统一由 `ObjectStorageService` 封装本地与云对象存储。
- 下载/流式/SSE/代理透传接口保留原始协议，不强制套统一响应体。

## 迁移文档索引

- `docs/refactor/response-contract.md`
- `docs/refactor/database-migration.md`
- `docs/refactor/auth-refactor-phase1.md`
- `docs/refactor/frontend-structure-phase1.md`
