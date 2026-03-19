# 认证域拆分（Phase 1）

## 改了什么

- `AuthService` 从巨型实现重构为轻量门面，仅负责 DTO 映射与子服务分发。
- 拆出子服务：
  - `AuthLoginService`
  - `ConcurrentLoginOrchestrator`
  - `AuthRegisterService`
  - `PasswordResetService`
  - `CurrentUserProfileService`
- 在 auth 域增加模型分层示范：
  - `req`：登录/注册/重置密码请求模型
  - `resp`：登录响应模型
  - `command`：登录编排命令
  - `query`：登录用户查询模型
- 补充认证关键流程单测：登录成功/失败、注册校验、找回密码分支、并发登录冲突。

## 为什么改

- 认证相关职责过度集中，导致可读性和可测试性下降。
- 并发登录流程（冲突判定、pending、确认）需要独立边界。
- 为后续 auth 子域持续拆分提供稳定骨架。

## 兼容策略

- 保持 `AuthController` 与 `ConcurrentLoginController` 路径不变。
- 保持前端 API 语义不变，登录响应仍兼容原结构（成功/待确认）。
- 旧调用方继续通过 `AuthService` 访问，不引入控制器层改造成本。

## 后续还要做什么

- 继续拆分 `AuthLoginService` 内部通道逻辑（账号/短信/邮箱登录策略化）。
- 抽离验证码发送与验证到独立应用服务。
- 增加并发登录场景的集成测试（SSE + decision flow）。

## 相关目录路径

- `backend/src/main/java/elexvx/admin/service/AuthService.java`
- `backend/src/main/java/elexvx/admin/service/auth/`
- `backend/src/main/java/elexvx/admin/service/auth/model/`
- `backend/src/test/java/elexvx/admin/service/auth/AuthDomainServiceTest.java`
