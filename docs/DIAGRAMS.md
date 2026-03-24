# 图表（Mermaid）

本文档使用 Mermaid 绘制类图、时序图等。若你的 Markdown 预览不支持 Mermaid，可使用支持 Mermaid 的编辑器/平台（如 VS Code Mermaid 插件、GitHub、GitLab 等）查看。

## 时序图

### 账号密码登录（含验证码、并发登录处理）

```mermaid
sequenceDiagram
  autonumber
  actor U as 用户
  participant FE as 前端（Vue）
  participant AC as AuthController
  participant AS as AuthService
  participant SS as SecuritySettingService
  participant CS as CaptchaService
  participant UM as UserMapper(MyBatis)
  participant CLS as ConcurrentLoginService
  participant ATS as AuthTokenService
  participant R as Redis
  participant OLS as OperationLogService

  U->>FE: 输入账号/密码/验证码并提交
  FE->>AC: POST /auth/login
  AC->>AS: login(req)
  AS->>SS: getOrCreate()
  alt 启用验证码
    AS->>CS: verify(captchaId, captchaCode)
    CS-->>AS: true/false
    AS-->>AC: 失败则抛出异常（验证码无效/过期）
  end
  AS->>UM: selectByAccount(account)
  UM-->>AS: UserEntity
  AS->>AS: BCrypt.checkpw(password, passwordHash)
  alt 不允许多端登录且存在活跃会话
    AS->>CLS: hasActiveSubscriber(userId)
    alt 有审批监听者且未 force
      AS->>CLS: createPending(...)
      CLS-->>AS: PendingLogin(requestId, requestKey)
      AS-->>AC: 返回 pending（需要确认）
      AC-->>FE: 200 {status:"pending", requestId, requestKey}
    else 强制/无监听者
      AS->>ATS: removeUserTokens(userId)
    end
  end
  AS->>ATS: createToken(userId, session, expiresIn)
  ATS->>R: SET auth:token:{token}=session (TTL)
  ATS->>R: SADD auth:tokens, auth:user:{id}:tokens
  ATS-->>AS: token
  AS->>OLS: logLogin(...)
  AS-->>AC: LoginResponse.success(token, expiresIn)
  AC-->>FE: 200 {status:"ok", token, expiresIn}
```

### API 请求鉴权（Bearer Token）

```mermaid
sequenceDiagram
  autonumber
  actor FE as 前端（Axios）
  participant SF as Spring Security FilterChain
  participant ATF as AuthTokenFilter
  participant ATS as AuthTokenService
  participant R as Redis
  participant SC as SecurityContextHolder
  participant API as 任意受保护 API Controller
  participant CTX as AuthContext

  FE->>SF: 请求 /api/**\nAuthorization: Bearer {token}
  SF->>ATF: doFilterInternal()
  ATF->>ATF: resolveToken()
  ATF->>ATS: getSession(token)
  ATS->>R: GET auth:token:{token}
  R-->>ATS: session(json)
  ATS-->>ATF: AuthSession / null
  alt token 有效
    ATF->>SC: setAuthentication(AuthPrincipal(userId, token))
  end
  ATF-->>SF: 继续过滤器链
  SF->>API: 调用 Controller
  API->>CTX: requireUserId()
  CTX-->>API: userId
  API-->>FE: 业务响应 / 401 / 403
```

### 前端动态菜单/路由初始化（首次进入或刷新）

```mermaid
sequenceDiagram
  autonumber
  actor U as 用户
  participant R as Router Guard(permission.ts)
  participant US as UserStore
  participant BE as 后端 API
  participant MC as MenuController
  participant MIS as MenuItemService
  participant PS as PermissionStore
  participant VR as Vue Router

  U->>R: 访问任意受保护页面
  R->>US: restoreTokenFromStorage()
  alt 已登录且 token 未过期
    R->>US: getUserInfo()
    US->>BE: GET /auth/user
    BE-->>US: UserInfo
    R->>PS: buildAsyncRoutes(userInfo)
    PS->>MC: GET /get-menu-list-i18n
    MC->>MIS: getMenuRoutesForCurrentUser()
    MIS-->>MC: RouteItem[]
    MC-->>PS: {list: RouteItem[]}
    PS->>PS: transformObjectToRoute()
    PS->>VR: addRoute(asyncRoutes...)
    R-->>U: 进入目标页面
  else 未登录
    R-->>U: 重定向 /login
  end
```

## 类图

### 认证与 Token（后端核心类关系）

```mermaid
classDiagram
  direction LR

  class SecurityConfig
  class AuthTokenFilter
  class AuthTokenService
  class AuthContext
  class AuthService
  class AuthController
  class CaptchaService
  class UserMapper
  class UserEntity
  class AuthSession
  class AuthPrincipal
  class RedisTemplate
  class SecuritySettingService
  class ConcurrentLoginService
  class OperationLogService

  SecurityConfig --> AuthTokenFilter : addFilterBefore()
  AuthTokenFilter --> AuthTokenService : getSession()/updateSession()
  AuthTokenFilter --> SecuritySettingService : allowUrlTokenParam
  AuthTokenFilter --> AuthPrincipal : setAuthentication

  AuthContext --> AuthPrincipal : read from SecurityContext

  AuthController --> AuthService
  AuthService --> CaptchaService
  AuthService --> SecuritySettingService
  AuthService --> UserMapper
  AuthService --> AuthTokenService
  AuthService --> ConcurrentLoginService
  AuthService --> OperationLogService
  AuthTokenService --> AuthSession : serialize/deserialize
  AuthTokenService --> RedisTemplate : store sessions
  UserMapper --> UserEntity
```

### 动态菜单（后端菜单模型关系）

```mermaid
classDiagram
  direction LR

  class MenuController
  class MenuItemService
  class MenuItemMapper
  class MenuItemEntity
  class RouteItem
  class AuthContext
  class PermissionFacade
  class AuthQueryDao
  class OperationLogService

  MenuController --> MenuItemService
  MenuItemService --> MenuItemMapper
  MenuItemService --> MenuItemEntity
  MenuItemService --> RouteItem
  MenuItemService --> AuthContext
  MenuItemService --> PermissionFacade
  MenuItemService --> AuthQueryDao
  MenuItemService --> OperationLogService
```
