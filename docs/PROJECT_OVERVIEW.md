# 项目概述

## 项目是做什么的

Elexvx 脚手架系统是一套面向企业后台的“开箱即用”基础管理平台脚手架，目标是把通用能力（账号与权限、菜单路由、系统配置、消息公告、文件与对象存储、验证码/短信/邮箱验证等）沉淀成可复用底座，业务系统可在此基础上快速二次开发。

## 主要能力（模块清单）

- 认证与账号：账号密码登录、短信/邮箱验证码登录、注册、找回/重置密码、个人资料维护、并发登录处理
- 权限与菜单：用户/角色/权限点、菜单树配置、前端动态路由与侧边栏菜单
- 系统配置：UI/主题/品牌/页脚/登录页/法务、密码策略与验证码开关、安全字段加密/脱敏
- 消息与公告：公告 announcements、通知 notifications、站内消息 messages
- 文件与对象存储：本地/阿里云 OSS/腾讯云 COS，统一上传与访问 URL（带签名/令牌）
- 运维与审计：操作日志、在线用户、Redis 信息与健康检查等
- 可插拔模块：短信/邮箱等验证能力以“模块开关 + Provider”方式接入

## 技术栈

### 前端

- Vue 3 + TypeScript + Vite
- Pinia、Vue Router
- Elexvx Vue Next、Axios、ECharts

### 后端

- Spring Boot 3.3（JDK 17）
- Spring Security（无状态 Token 鉴权）
- MyBatis + Druid
- Spring Data Redis（Token 会话/部分运维能力）
- SpringDoc OpenAPI（Swagger UI）
- AJ Captcha（验证码）
- 对象存储 SDK：阿里云 OSS、腾讯云 COS
- Netty Socket（可选）
- Flyway（默认关闭）

### 数据库

- 默认以 MySQL 脚本为主，同时提供 PostgreSQL / Oracle / SQLServer 的 schema 与 demo
- 位置见 [database/README.md](database/README.md)

## 代码结构

- frontend/：管理后台前端（页面、路由、状态、API 请求封装等）
- backend/：后端服务（Controller/Service/Mapper/DAO/DTO/VO/Config 等）
- database/：数据库初始化与演示数据脚本
- docs/：项目文档（部署、安全、接口说明等）

## 文档索引

- 架构说明：[ARCHITECTURE.md](docs/ARCHITECTURE.md)
- 图表（类图/时序图）：[DIAGRAMS.md](docs/DIAGRAMS.md)
- API 鉴权约定：[API_SECURITY_SPEC.md](docs/API_SECURITY_SPEC.md)
- 消息相关接口：[MESSAGE_API.md](docs/MESSAGE_API.md)
- 部署（1Panel）：[DEPLOY_1PANEL.md](docs/DEPLOY_1PANEL.md)
- 更新与业务隔离：[UPDATE_GUIDE.md](docs/UPDATE_GUIDE.md)

