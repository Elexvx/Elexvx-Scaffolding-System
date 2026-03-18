# Elexvx ® Inc 脚手架系统

Elexvx 脚手架系统 是由 Elexvx ® Inc 推出的一套面向企业后台的脚手架/管理系统，涵盖用户与权限管理、系统配置、消息公告、文件与对象存储、短信/邮箱验证等基础能力，帮助快速搭建业务后台。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router、TDesign Vue Next、ECharts、Axios
- 后端：Spring Boot 3.3、Spring Security（Token）、MyBatis、Druid、MySQL（默认，亦支持 PostgreSQL/Oracle/SQLServer）、Redis（Token 会话/运维能力）、Spring Mail（可选）、SpringDoc OpenAPI
- 其他：Flyway（当前默认关闭）、AJ Captcha

## 快速开始

### 环境准备

- Node.js >= 18.18.0，npm >= 9
- JDK 17+，Maven 3.8+
- MySQL 8.x
- Redis（可选，用于监控等功能）

### 初始化数据库

- 可导入 `database/demo/tdesign_init.sql` 作为初始结构与示例数据
- 若只需要结构，可导入 `database/schema/tdesign_schema.sql`

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

- 默认端口：`9999`
- 上下文路径：`/api`
- 数据库配置：`backend/src/main/resources/application.yml`
- 可用环境变量覆盖：`TDESIGN_DB_URL`、`TDESIGN_DB_USER`、`TDESIGN_DB_PASSWORD`（也可使用 `TDESIGN_DB_DRIVER`、`TDESIGN_DB_TYPE` 等进行多数据库切换）

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

- 访问：`http://localhost:3002`
- 开发环境 `/api` 会代理到 `http://127.0.0.1:9999`

### 生产构建（可选）

```bash
cd frontend
npm run build
cd ../backend
mvn -DskipTests package
```

## 目录说明

- `frontend/` 管理后台前端（Vue 3 + Vite），页面、路由、状态管理、API 请求等
- `backend/` 后端服务（Spring Boot），接口、业务逻辑、数据访问与配置
- `database/` 初始化 SQL、表结构与基础数据脚本
- `scripts/` 项目辅助脚本（构建/部署/运维相关）

## 更新与业务隔离

为避免系统更新覆盖业务代码，建议将业务代码放在独立目录中，并通过配置或接口联动。  
详细说明请参考：`docs/UPDATE_GUIDE.md`。

## 文档

- 项目概述：`docs/PROJECT_OVERVIEW.md`
- 架构说明：`docs/ARCHITECTURE.md`
- 类图/时序图：`docs/DIAGRAMS.md`
- 模块包规范：`docs/MODULE_PACKAGE.md`

## 生产安全基线（新增默认）

后端在 `prod` 环境默认采用“安全优先”配置：

- `TDESIGN_SECURITY_FIELD_SECRET`：**必填**，至少 32 bytes；未配置时启动失败。
- `TDESIGN_CORS_ALLOWED_ORIGINS`：**必填**，仅允许显式白名单域名（禁止 `*`）。
- `FILE_TOKEN_SECRET`：建议必填，文件下载 token 使用 AES/GCM 并带过期时间（默认 600 秒）。
- `tdesign.web.expose-uploads=false`：生产默认不暴露 `/uploads/**` 静态目录。
- `springdoc.api-docs.enabled=false`、`springdoc.swagger-ui.enabled=false`：生产默认关闭 API 文档端点。
- `tdesign.file.upload.max-file-size-mb=100`：上传大小默认 100MB，可按业务下调/上调。
- 登录与短信/邮箱发送接口启用 Redis 限流（IP + 账号/邮箱/手机号维度）。

参考生产配置：

```yaml
spring:
  profiles:
    active: prod

tdesign:
  security:
    field-secret: ${TDESIGN_SECURITY_FIELD_SECRET}
    rate-limit:
      login-per-minute: 10
      login-fail-threshold: 5
      sms-email-per-minute: 3
      sms-email-per-day: 20
  web:
    expose-uploads: false
    cors:
      allowed-origin-patterns: ${TDESIGN_CORS_ALLOWED_ORIGINS}
  file:
    token-secret: ${FILE_TOKEN_SECRET}
    token-ttl-seconds: 600
    upload:
      max-file-size-mb: 100

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```


## 安全与会话存储迁移说明（PR#2）

- 密码重置接口 `POST /system/user/{id}/reset-password` 默认仅支持 JSON body 传 `password`，避免密码出现在 URL/query。
- 如需兼容历史客户端，可临时开启 `tdesign.security.allow-password-in-query=true`，并尽快完成客户端迁移后关闭。
- 在线会话 token 索引从 `auth:tokens`(Set) 迁移到 `auth:tokens:z`(ZSET, score=expiresAtMs)。
- 迁移策略为懒迁移：`listAllTokens` 会在读取时把旧集合中的有效 token 转入 ZSET，并清理失效 token。
- 回滚策略：新版本仍会写入旧 `auth:tokens` 集合作为兼容；回滚后旧版本仍可读取。
