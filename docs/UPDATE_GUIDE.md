# 系统更新与业务隔离说明

## 目标

为避免后续更新覆盖业务代码，建议保持 **系统结构** 与 **业务实现** 在路径层面完全独立。这样在更新系统时，仅需替换系统核心目录即可，业务目录不会受到影响。

## 推荐目录结构

```
Elexvx-Scaffolding-System/
├── backend/                # 系统后端（核心能力）
├── frontend/               # 系统前端（核心能力）
├── business-backend/       # 业务后端（自定义接口、领域逻辑）
├── business-frontend/      # 业务前端（自定义页面、业务模块）
└── docs/                   # 文档
```

### 业务代码放置建议

- **业务后端**：放在 `business-backend/`，保持与系统后端完全分离。  
  - 推荐独立启动端口，与系统后端通过 HTTP/Feign/RPC 等方式对接。
  - 也可以引入系统后端提供的公共依赖或工具类，但不要直接修改系统后端代码。
- **业务前端**：放在 `business-frontend/`，保持与系统前端完全分离。  
  - 推荐独立部署，或以微前端方式接入。
  - 对系统前端的 UI/基础组件尽量以依赖方式复用，避免直接修改系统代码。

## 系统更新流程

1. 在更新前备份 `business-backend/` 与 `business-frontend/`。
2. 仅替换系统目录（`backend/`、`frontend/`）为最新版本代码。
3. 保持业务目录不动，重新构建并验证运行。

## 自动更新检测配置

系统支持通过接口自动检测最新版本，默认读取以下配置：

```yaml
tdesign:
  system:
    version: 1.0.0
    update-check-url: https://your-domain.com/tdesign/update.json
```

`update-check-url` 返回示例（JSON）：

```json
{
  "version": "1.2.0",
  "downloadUrl": "https://your-domain.com/releases/1.2.0"
}
```

- `version`：最新版本号
- `downloadUrl`：更新说明或下载地址

当最新版本高于当前版本时，前端会提示可更新。
