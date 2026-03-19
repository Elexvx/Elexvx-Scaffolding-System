# 模块包（Zip）规范

本系统支持通过“模块管理 → 上传 ZIP 安装”在运行中安装/更新模块包。模块包必须同时包含：前端（静态资源）、后端（Node 服务）、数据库（SQL 脚本），并在安装失败时执行回滚。

## 目录结构

模块包为一个 zip，根目录必须包含 `module.json`，其余内容按约定路径放置：

```
module.json
modules/<moduleKey>/<db>/install.sql
modules/<moduleKey>/<db>/uninstall.sql
frontend/**                # 前端静态资源（必须包含 index.html）
backend/**                 # 后端 Node 服务（必须包含 package.json）
```

其中 `<db>` 支持：`mysql`、`postgresql`、`oracle`、`sqlserver`，且必须提供 `install.sql`。

## module.json 示例

```json
{
  "key": "demo",
  "name": "Demo 模块",
  "version": "1.0.0",
  "enabledByDefault": true,
  "requiredTables": ["demo_table"],
  "frontend": {
    "type": "static",
    "basePath": "/modules/demo/",
    "index": "index.html"
  },
  "backend": {
    "type": "node",
    "startScript": "start",
    "basePath": "/module-api/demo/",
    "autoInstallDependencies": true
  }
}
```

## 运行期加载方式

- **前端（静态资源）**
  - 安装后会被放置到：`<packageDir>/frontend/<moduleKey>/...`
  - 后端会通过静态资源映射提供访问：`/modules/**`（若配置了 `server.servlet.context-path=/api`，则实际为 `/api/modules/**`）
  - 典型入口：`/modules/<moduleKey>/index.html`（或 `/api/modules/<moduleKey>/index.html`）
  - 建议通过菜单配置为 IFRAME 页面来接入（系统已支持 IFRAME 路由渲染）。

- **后端（Node 服务）**
  - 安装后会被放置到：`<packageDir>/backend/<moduleKey>/...`
  - 安装时会自动安装依赖（当 `autoInstallDependencies=true` 或未配置时）
  - 启用模块时会尝试启动 `npm run <startScript>`，并自动注入环境变量：
    - `PORT`：为模块分配的本地端口
    - `MODULE_KEY`：模块 key
  - 访问统一通过代理入口：`/module-api/<moduleKey>/**`（若配置了 `server.servlet.context-path=/api`，则实际为 `/api/module-api/<moduleKey>/**`）

- **数据库（SQL 脚本）**
  - 安装时执行 `install.sql`；卸载时执行（若存在）`uninstall.sql`
  - SQL 脚本可使用系统内置模块安装机制（`ModuleInstallationContext`）自动选择当前数据库类型。

## 回滚策略

上传安装流程分为：解析 → 临时落盘 → 提交覆盖 → 执行安装 → 启动后端。

若任一步骤失败：
- 文件系统会回滚到安装前状态（恢复旧 manifest/脚本/前端/后端目录）
- 会尝试执行模块卸载脚本进行数据库回退（best-effort，取决于脚本与数据库 DDL 事务特性）

## 配置项

- `elexvx.modules.packageDir`
  - 模块包落盘目录（默认：`data/module-packages`）
  - 该目录下会生成：
    - `manifests/`：模块清单
    - `modules/`：数据库脚本
    - `frontend/`：前端静态资源
    - `backend/`：后端 Node 服务
