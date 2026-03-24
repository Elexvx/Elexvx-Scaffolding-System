# 数据库迁移治理（Flyway）

## 改了什么

- 启用 Flyway 并将主库迁移入口固定到 `backend/src/main/resources/db/migration/**`。
- 建立 `V1__baseline.sql` 基线链路（多数据库目录）。
- 退役旧式 `DatabaseSchemaInitializer` 初始化路径，避免“提示导库”式伪初始化。
- 将 `database/*.sql` 定位为历史参考资料，不再作为正式演进入口。

## 为什么改

- 消除手工导库导致的结构漂移与版本不可追踪问题。
- 统一新老环境初始化方式，降低发布失败概率。
- 让数据库变更进入“可审计、可回放、可管控”的版本治理流程。

## 兼容策略

- 对老环境使用 `baseline-on-migrate` 接入，不强制立即重建数据库。
- 仍保留 `database/*.sql` 作为排障和历史比对参考。
- Flyway `clean-disabled=true`，避免误删生产数据。

## 背景与目标

项目早期主要通过 `database/*.sql` 手工导入初始化，存在以下问题：

- 环境之间结构漂移难追踪
- 变更无法形成有序版本历史
- 新老环境初始化流程不一致

为保证数据库结构可审计、可回滚策略化、可持续演进，项目正式切换为 Flyway 版本治理。

## 正式迁移入口

- 正式迁移目录：`backend/src/main/resources/db/migration`
- 主库（MySQL）基线：`backend/src/main/resources/db/migration/mysql/V1__baseline.sql`
- 配置入口：`backend/src/main/resources/application.yml` 的 `spring.flyway.*`

从本次整改起，数据库结构演进以 Flyway 迁移脚本为唯一正式入口。

## baseline 的作用

- `V1__baseline.sql` 用于定义当前主系统的基础结构基线
- 基线脚本仅保留结构定义，不包含演示业务数据
- 后续新增表/字段/索引通过更高版本脚本追加

## 新增迁移规范

新增数据库变更时：

1. 在对应目录新增版本脚本（示例：`V2__add_user_last_login.sql`）
2. 仅追加，不修改已执行过的历史迁移文件
3. 脚本必须可重复在“同版本空库”场景通过
4. 一次迁移只做同一主题变更，避免跨域混杂

禁止事项：

- 禁止修改、重排、覆盖已发布版本号
- 禁止把演示数据混入结构迁移
- 禁止在生产环境启用 Flyway clean

## 新环境初始化

新环境初始化流程：

1. 配置数据库连接（`application.yml` 或环境变量）
2. 启动后端应用
3. Flyway 自动执行 `V1` 及后续迁移

不再要求优先手工导入 `database/*.sql`。

## 老环境接入 baseline

老环境（已有业务表）接入建议：

1. 先完整备份数据库
2. 确认当前结构与 `V1` 基线一致或可接受差异
3. 依赖 `baseline-on-migrate` 写入基线版本
4. 后续仅通过新迁移版本继续演进

若历史库与基线差异较大，先补齐一次人工对齐脚本，再接入 Flyway。

## 旧 SQL 脚本定位

- `database/*.sql` 保留为历史参考与手工排障资料
- `database/*.sql` 不再作为正式版本演进入口
- 正式结构变更与发布流程仅认可 `db/migration` 中版本脚本

## 后续还要做什么

- 在 `V2+` 迁移中按业务域拆分脚本，避免单迁移过大。
- 补充数据库迁移 CI 检查（版本号递增、禁止修改历史脚本）。
- 按环境补齐迁移验证清单（空库初始化、老库 baseline 接入、回归校验）。

## 相关目录路径

- `backend/src/main/resources/db/migration/`
- `backend/src/main/resources/application.yml`
- `database/`
- `docs/refactor/database-migration.md`
