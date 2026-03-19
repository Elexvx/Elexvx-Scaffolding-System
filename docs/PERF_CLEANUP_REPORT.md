# PERF Cleanup Report

## 1. 后端改造摘要

### 1.1 菜单维护从请求期迁移到启动期
- 新增 `MenuMaintenanceRunner`，应用启动后一次性执行：
  - `ensureOrgManagementMenuSeeded`
  - `ensureConsolePrintMenuSeeded`
  - `removeObsoleteWatermarkRoute`
  - `removeObsoleteTeamRoute`
  - `removeObsoletePrintRoute`
  - `removeObsoleteNotificationRoute`
- 新增开关：`elexvx.menu.maintenance.enabled`（默认 `true`，可通过环境变量 `ELEXVX_MENU_MAINTENANCE_ENABLED` 关闭）。
- `MenuController#getMenuList` 与 `SystemMenuController#tree` 去除了维护逻辑，改为纯读路径。

### 1.2 菜单并发幂等
- `MenuItemService` 在种子写入路径引入 `DataIntegrityViolationException` 幂等兜底。
- 并发启动时若因唯一键冲突插入失败，会回查 `routeName` 并返回已存在记录，避免实例启动抖动。

### 1.3 TTL 缓存与自动淘汰
- 新增 `VerificationCacheService`（基于 Caffeine）统一管理：
  - Captcha（120s，最大 10k）
  - SmsCode（300s，最大 20k）
  - EmailCode（300s，最大 20k）
  - ConcurrentLogin Pending（120s，最大 50k）
- `CaptchaService/SmsCodeService/EmailCodeService/ConcurrentLoginService` 已切换到 TTL cache，避免 `ConcurrentHashMap` 无限增长。

### 1.4 文件分片会话过期清理
- `FileChunkUploadService` 增加：
  - 会话 TTL：30 分钟无更新过期
  - 启动时与每 10 分钟定时扫描 `runtime/upload-chunks`
  - 清理过期会话目录并移除 `sessionLocks`
  - `finalizeUpload` 异常路径通过 `finally` 确保释放锁

### 1.5 对象存储 client 复用
- 新增 `ObjectStorageClientManager`：Aliyun OSS / Tencent COS 客户端按配置指纹复用。
- `ObjectStorageService` 上传/下载/stat 路径改为从 manager 获取 client，不再每次请求新建/销毁。
- 配置保存后触发 client 失效与重建（平滑切换）。
- 热点微优化：`UUID.replaceAll("-", "")` -> `replace("-", "")`。

### 1.6 通知分页筛选下推到 SQL
- `NotificationMapper` 与 `NotificationMapper.xml` 扩展分页/计数条件：
  - `statuses`
  - `keywordLike`（`LOWER(title|summary) LIKE`）
  - `priority`
- `NotificationService#page` 去除 Java Stream 二次过滤，`total` 与筛选条件保持一致。

## 2. 前端改造摘要

### 2.1 import.meta.glob 扫描范围收敛
- 菜单组件选择页：
  - `../../**/*.vue` -> `/src/pages/**/*.vue`
- 动态路由模块扫描：
  - 仅保留 `/src/pages/**/*.vue` + `/src/views/**/*.vue`
  - 去掉 tsx 扫描规则

### 2.2 ECharts 按需引入
- 新增 `src/utils/echarts.ts`，使用 `echarts/core` + `LineChart/GridComponent/TooltipComponent/CanvasRenderer`。
- Redis 监控页改为复用 `initEChart`，保留原渲染/resize/销毁逻辑。

## 3. 废弃接口/包/路由清单（兼容清理项）

### 3.1 菜单路由（已在启动维护中清理）
- `SystemWatermark`
- `ExamplePrint`
- `notification`
- `NotificationTable`
- `SystemTeam`
- `team`

> 以上属于“历史菜单路由清理项”，建议保留一版兼容说明后彻底下线。

### 3.2 后端接口（候选）
- 本轮以“无行为变更”为原则，未直接删除接口。
- 建议后续结合访问日志 + 前端调用图进一步确认候选下线接口。

### 3.3 依赖瘦身
- Maven 新增数据库 profile：`mysql`(默认) / `pg` / `oracle` / `sqlserver`，默认包仅携带 mysql 驱动。
- `caffeine` 已引入用于统一 TTL 缓存。

## 4. 构建与验证

### 4.1 已执行
- 后端打包：受网络环境限制（无法访问 Maven Central）未完成。
- 前端：`npm ci`、`npm run build` 通过。

### 4.2 Maven profiles 构建示例
- MySQL（默认）：
  - `mvn -Pmysql -DskipTests package`
- PostgreSQL：
  - `mvn -Ppg -DskipTests package`
- Oracle：
  - `mvn -Poracle -DskipTests package`
- SQLServer：
  - `mvn -Psqlserver -DskipTests package`

## 5. 风险点与回滚建议

### 风险点
- 缓存由 Map 切换为 Caffeine 后，容量上限策略可能在极端压测下提前淘汰。
- OSS/COS client 复用后，若配置频繁切换，会触发 client 重建（已实现旧 client 关闭）。
- 分片清理任务与上传并发场景已加锁，但仍建议在高并发压测下重点观察日志。

### 回滚建议
1. 回滚到本次提交前版本（git revert）。
2. 若只临时关闭菜单维护任务，可设：`ELEXVX_MENU_MAINTENANCE_ENABLED=false`。
3. 分阶段回滚优先级：
   - 先回滚对象存储 client 复用
   - 再回滚缓存层改造
   - 最后回滚通知 SQL 下推
