# 前端迁移说明（Vue3/TDesign -> React18/Umi Max）

## 迁移范围

- 保留旧前端 `frontend` 作为对照，不删除。
- 新前端 `frontend-umi` 作为唯一迁移落点。
- 迁移口径以 `frontend/src/pages` 为准，覆盖全部旧页面。

## 迁移策略

1. 保持后端接口路径、菜单字段、权限码模型不变，前端适配。
2. 采用动态菜单 + `componentRegistry` 白名单映射，避免后端组件路径直连。
3. 对能力相近页面进行“合并重构”，同时保留旧路由兼容入口。
4. 列表页统一使用 `PageScaffold + ResponsiveListTable`，移动端默认卡片化。

## 旧页 -> 新页完整映射表

| 旧页面路径 | 新页面路径 | 迁移状态 | 功能状态 | 备注 |
| --- | --- | --- | --- | --- |
| `login/index.vue` | `src/pages/auth/login/index.tsx` | 已迁移 | 完整 | 登录流程一对一迁移。 |
| `announcement/cards/index.vue` | `src/pages/announcement/cards/index.tsx` | 已迁移 | 完整 | 复用公告管理页并保留旧路由。 |
| `announcement/table/index.vue` | `src/pages/announcement/table/index.tsx` | 已迁移 | 完整 | 与 cards 共享公告能力。 |
| `console/download/index.vue` | `src/pages/console/download/index.tsx` | 已迁移 | 完整 | 提供下载入口、常用资源列表和链接下载。 |
| `message/send/index.vue` | `src/pages/message/send/index.tsx` | 已迁移 | 完整 | 兼容旧路由并落到消息发送页。 |
| `notification/table/index.vue` | `src/pages/notification/table/index.tsx` | 已迁移 | 完整 | 兼容旧通知表格入口。 |
| `system/dict/index.vue` | `src/pages/system/dict/index.tsx` | 已迁移 | 完整 | 字典管理独立页。 |
| `system/log/index.vue` | `src/pages/system/log/index.tsx` | 已迁移 | 完整 | 兼容旧日志路由，内部复用监控日志能力。 |
| `system/menu/index.vue` | `src/pages/system/menu/index.tsx` | 已迁移 | 完整 | 菜单管理页。 |
| `system/modules/index.vue` | `src/pages/system/modules/index.tsx` | 已迁移 | 完整 | 模块管理页。 |
| `system/monitor/online-user/index.vue` | `src/pages/system/monitor/online-user/index.tsx` | 已迁移 | 完整 | 在线用户监控。 |
| `system/monitor/redis/index.vue` | `src/pages/system/monitor/redis/index.tsx` | 已迁移 | 完整 | Redis 监控。 |
| `system/monitor/server/index.vue` | `src/pages/system/monitor/server/index.tsx` | 已迁移 | 完整 | 服务器监控。 |
| `system/org/index.vue` | `src/pages/system/org/index.tsx` | 已迁移 | 完整 | 组织管理页。 |
| `system/personalize/index.vue` | `src/pages/system/personalize/index.tsx` | 已兼容 | 完整 | 合并到系统配置框架，默认个性化分区。 |
| `system/role/index.vue` | `src/pages/system/role/index.tsx` | 已迁移 | 完整 | 角色管理页。 |
| `system/security/index.vue` | `src/pages/system/security/index.tsx` | 已兼容 | 完整 | 合并到系统配置框架，默认安全分区。 |
| `system/sensitive/index.vue` | `src/pages/system/sensitive/index.tsx` | 已迁移 | 完整 | 敏感词管理页。 |
| `system/storage/index.vue` | `src/pages/system/storage/index.tsx` | 已兼容 | 完整 | 合并到系统配置框架，默认存储分区。 |
| `system/user/index.vue` | `src/pages/system/user/index.tsx` | 已迁移 | 完整 | 用户管理页。 |
| `system/verification/index.vue` | `src/pages/system/verification/index.tsx` | 已兼容 | 完整 | 合并到系统配置框架，默认验证分区。 |
| `system/watermark/index.vue` | `src/pages/system/watermark/index.tsx` | 已迁移 | 完整 | 水印设置页。 |
| `user/index.vue` | `src/pages/user/index.tsx` | 已迁移 | 完整 | 与账号中心等效映射。 |
| `result/403/index.vue` | `src/pages/result/403/index.tsx` | 已迁移 | 完整 | 独立结果页。 |
| `result/404/index.vue` | `src/pages/result/404/index.tsx` | 已迁移 | 完整 | 独立结果页。 |
| `result/500/index.vue` | `src/pages/result/500/index.tsx` | 已迁移 | 完整 | 独立结果页。 |
| `result/browser-incompatible/index.vue` | `src/pages/result/browser-incompatible/index.tsx` | 已迁移 | 完整 | 提供返回入口。 |
| `result/fail/index.vue` | `src/pages/result/fail/index.tsx` | 已迁移 | 完整 | 提供返回入口。 |
| `result/maintenance/index.vue` | `src/pages/result/maintenance/index.tsx` | 已迁移 | 完整 | 提供返回入口。 |
| `result/network-error/index.vue` | `src/pages/result/network-error/index.tsx` | 已迁移 | 完整 | 提供刷新入口。 |
| `result/success/index.vue` | `src/pages/result/success/index.tsx` | 已迁移 | 完整 | 提供回工作台入口。 |
| `example/goods/index.vue` | `src/pages/example/goods/index.tsx` | 示例页 | 完整 | 作为内部演示能力保留。 |
| `example/order/index.vue` | `src/pages/example/order/index.tsx` | 示例页 | 完整 | 作为内部演示能力保留。 |

## 说明

- 动态菜单与页面映射请见 `src/pages/dynamic/componentRegistry.ts`。
- 配置类页面采用“分区合并”迁移，不丢失旧菜单路由命中能力。
- 示例页保留可访问实现，避免历史菜单/链接失效。
