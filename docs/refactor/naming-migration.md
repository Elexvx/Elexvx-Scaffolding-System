# 品牌命名迁移说明

本次重构将项目主品牌统一到 **Elexvx**，并保留旧 `tdesign` 命名作为兼容层。

## 后端

- Java 根包：`com.tencent.tdesign` → `elexvx.admin`
- Maven 坐标：`com.tencent.tdesign:tdesign-backend` → `elexvx:elexvx-backend`
- Spring Boot 启动类：`TDesignApplication` → `ElexvxApplication`
- 运行时配置主前缀：`elexvx.*`
- 兼容前缀：`tdesign.*`

### 兼容方式

- 新增 `ElexvxLegacyPropertyEnvironmentPostProcessor`，在启动早期为 `elexvx.*` 与 `tdesign.*` 建立双向别名。
- 对关键配置增加 `ELEXVX_*` 环境变量，并继续兼容 `TDESIGN_*`。
- 兼容注释均已标注“新旧键兼容，后续版本再移除旧键”。

## 前端

- npm 包名：`@company/admin-frontend` → `@elexvx/admin-frontend`
- 本地存储主键统一到 `elexvx.*`
- 旧 `tdesign.*` 登录态、通知、设置、菜单展开状态在首次读取时自动迁移到新键

## 文档

- README 与 docs 内的品牌说明统一为 Elexvx
- 原 `file:///` 本地绝对路径链接已替换为仓库相对路径

## 建议的后续清理项

1. 在后续大版本删除 `tdesign.*` 兼容属性别名。
2. 删除前端 `tdesign.*` localStorage/sessionStorage 迁移逻辑。
3. 清理数据库脚本与历史部署文档中的旧品牌示例名称。
