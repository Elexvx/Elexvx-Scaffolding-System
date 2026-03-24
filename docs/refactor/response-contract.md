# 统一响应契约（Phase 1）

## 改了什么

- 后端统一响应模型升级为 `success/code/message/userTip/data/requestId/timestamp/path`。
- 新增统一响应工厂 `backend/src/main/java/elexvx/admin/web/ApiResponses.java`，异常与安全错误统一收口。
- `GlobalExceptionHandler`、`SecurityConfig`、`SensitiveWordFilter` 改为同一套响应写出逻辑。
- 增加 `X-Request-Id` 链路：请求头读取/生成、响应头回写、MDC 注入与清理。
- 前端请求层 `frontend/src/utils/request/index.ts` 同步兼容新旧两种结构。

## 为什么改

- 降低错误响应格式分叉，减少前后端对齐成本。
- 提供可追踪请求链路，方便排障与日志关联。
- 区分 `message`（诊断）与 `userTip`（用户提示），避免展示内部信息。

## 兼容策略

- 保留 `ApiResponse` 类名和 `code/data/message` 语义，老接口调用方仍可读取。
- 前端错误提示优先 `userTip`，缺失时回退 `message`，再回退默认文案。
- SSE/二进制/下载/代理透传接口不强行包装，保留原始协议行为。

## 后续还要做什么

- 对业务错误码建立更细粒度分层（模块级码段）。
- 补充响应契约自动化测试（后端集成测试 + 前端契约测试）。
- 在网关层统一透传 `X-Request-Id` 与审计日志字段。

## 相关目录路径

- `backend/src/main/java/elexvx/admin/vo/ApiResponse.java`
- `backend/src/main/java/elexvx/admin/web/ApiResponses.java`
- `backend/src/main/java/elexvx/admin/config/GlobalExceptionHandler.java`
- `backend/src/main/java/elexvx/admin/config/SecurityConfig.java`
- `backend/src/main/java/elexvx/admin/config/RequestIdFilter.java`
- `frontend/src/utils/request/index.ts`
