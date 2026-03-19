# 架构与实现说明

## 总体架构

系统采用前后端分离：

- 前端（Vue 3）负责：登录/路由守卫、动态菜单渲染、业务页面与交互
- 后端（Spring Boot）负责：鉴权、权限校验、业务 API、数据持久化、文件/对象存储与第三方能力集成
- 数据层：关系型数据库（默认 MySQL；同时提供多数据库脚本）+ Redis（Token 会话与部分运维能力）

## 后端代码组织（分层）

典型调用链为 Controller → Service → Mapper/DAO → DB/外部服务：

- controller：REST API 入口，负责参数校验与返回统一响应
- service：核心业务逻辑编排（鉴权/权限、菜单、系统配置、文件、消息等）
- mapper：MyBatis Mapper（与 mappers/*.xml 对应），负责数据访问
- dao：偏组合查询/批量操作的 DAO（如权限关联维护）
- dto/vo/entity：入参 DTO、出参 VO、持久化实体 Entity
- config：Spring 配置（Security、Redis、Netty 等）

统一响应结构见 [ApiResponse](backend/src/main/java/top/elexvx/admin/vo/ApiResponse.java)。

## 鉴权与安全

### Token 模型（无状态 + Redis 会话）

- 登录成功后，后端生成随机 token，并将会话（AuthSession）序列化存入 Redis，带 TTL
- 客户端请求通过 Authorization 头携带 Bearer token
- 过滤器 [AuthTokenFilter](backend/src/main/java/top/elexvx/admin/security/AuthTokenFilter.java) 从请求解析 token → 查询 Redis 会话 → 写入 SecurityContext
- 业务侧通过 [AuthContext](backend/src/main/java/top/elexvx/admin/security/AuthContext.java) 获取当前 userId/token

对应安全配置见 [SecurityConfig](backend/src/main/java/top/elexvx/admin/config/SecurityConfig.java)（白名单接口、401/403 统一返回、Filter 链顺序等）。

### 并发登录处理（可选流程）

登录服务会根据 UI/安全设置决定是否允许多端同时在线；不允许时：

- 若存在“审批监听者”，可能返回 pending（需要确认）
- 否则撤销旧 token，再继续创建新 token

实现入口见 [AuthService](backend/src/main/java/top/elexvx/admin/service/AuthService.java) 的 completeLogin 相关逻辑。

## 权限与动态菜单

### 后端：菜单数据由数据库驱动

- 后端提供菜单路由接口：GET /get-menu-list-i18n（见 [MenuController](backend/src/main/java/top/elexvx/admin/controller/MenuController.java)）
- [MenuItemService](backend/src/main/java/top/elexvx/admin/service/MenuItemService.java) 从菜单表读取全量菜单 → 根据当前用户权限过滤 → 输出 RouteItem 树

### 前端：路由守卫触发动态路由加载

- 路由守卫见 [permission.ts](frontend/src/permission.ts)
- 首次进入/刷新时：
  - 拉取用户信息：GET /auth/user（见 [user.ts](frontend/src/store/modules/user.ts)）
  - 拉取菜单路由：GET /get-menu-list-i18n（见 [permission.ts](frontend/src/store/modules/permission.ts)）
  - 转换为 Vue Router 结构并 addRoute，驱动侧边栏/页面权限

## 文件与对象存储

[ObjectStorageService](backend/src/main/java/top/elexvx/admin/service/ObjectStorageService.java) 统一封装文件上传与读取，支持：

- LOCAL：落盘到 uploads/ 目录
- ALIYUN：阿里云 OSS
- TENCENT：腾讯云 COS

上传完成后返回“可访问 URL”，通常由 FileTokenService 生成带签名的访问地址（避免直接暴露内部对象 Key）。

## 可插拔模块（短信/邮箱/验证码）

系统通过“模块开关 + Provider Registry”实现可插拔能力：

- module_registry 记录模块启用状态与配置
- VerificationProviderRegistry 统一管理发送验证码的实现（短信/邮箱等）
- 业务侧（如 AuthService）在调用前做模块可用性校验与配置完整性校验

## 运维与审计

- Redis/健康检查：提供运维 API（例如 Redis 信息、健康检查）
- 操作审计：OperationLogService 记录关键操作（登录、菜单变更等），落表 operation_logs

## 图表

类图与时序图汇总见 [DIAGRAMS.md](docs/DIAGRAMS.md)。

