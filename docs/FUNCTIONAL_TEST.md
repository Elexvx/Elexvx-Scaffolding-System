# 功能测试文档（Functional Test）

本文档面向 Elexvx 脚手架系统的功能测试，覆盖后端 API 与前端管理后台的关键业务能力，并为每个功能部分给出可执行的测试用例。

## 1. 文档信息

| 项 | 值 |
| --- | --- |
| 项目 | Elexvx-Scaffolding-System |
| 后端 | Spring Boot（默认端口 9999，上下文路径 `/api`） |
| 前端 | Vue3（开发默认 `http://localhost:3002`） |
| 测试类型 | 功能测试（接口 + 页面联动） |

## 2. 测试范围

### 2.1 覆盖范围（In Scope）

- 认证与会话：验证码、登录（账号/短信/邮箱）、登出、并发登录处理、角色切换、401/403 统一错误提示
- 权限与菜单：RBAC 权限校验、菜单树、动态路由/页面可见性
- 系统管理：用户、角色、菜单（目录/页面管理）、机构（组织树）、字典（含导入/导出/模板）
- 消息与内容：站内消息、通知、公告（列表/详情/发布/撤回/删除）
- 文件与存储：普通上传、分片上传、取消/完成、文件访问 token、Range 断点、对象存储配置与测试
- 系统配置：UI 公共配置与管理配置、模块注册与启停/安装卸载、水印
- 安全增强：防重复提交、敏感字段脱敏、脱敏输入忽略覆盖
- 运维监控：健康检查、在线用户、Redis 监控（开关条件）、服务器监控
- 数据库脚本：demo/schema 脚本导入可用性（MySQL/PostgreSQL/Oracle/SQL Server）

### 2.2 不覆盖范围（Out of Scope）

- 性能压测与容量评估（可另出性能测试方案）
- 安全渗透（SQL 注入、XSS 等）与合规审计（可另出安全测试方案）
- 第三方云厂商真实账密的联调验收（对象存储/短信/邮箱的生产参数验收由专门环境完成）

## 3. 测试环境与前置条件

### 3.1 测试环境建议

- 数据库：MySQL 8.x（主测）；其他数据库用于脚本导入验证
- Redis：可选（若启用，将影响 token 会话、在线用户、Redis 监控能力）
- 后端基础地址：`http://localhost:9999/api`
- 前端地址：`http://localhost:3002`

### 3.2 数据初始化

- 建议导入含演示数据脚本（便于快速覆盖更多功能）：[database/README.md](database/README.md)
  - `database/demo/tdesign_init.sql`（MySQL）
- 如果只验证结构与空库行为，可导入 `database/schema/tdesign_schema.sql`

### 3.3 测试账号与权限

- 建议使用初始化脚本中的默认管理员账号登录（通常为 `admin / 123456`，以实际初始化脚本为准；部署文档也有说明 [DEPLOY_1PANEL.md](docs/DEPLOY_1PANEL.md#L124-L130)）
- 若默认账号不可用，优先通过数据库脚本确认 `users` 表的账号信息，或在测试库中重置密码再继续用例

## 4. 用例编写规范

### 4.1 编号规则

- 统一：`FT-<模块>-<序号>`
- 模块建议：AUTH / RBAC / USER / ROLE / MENU / ORG / DICT / MSG / ANN / NOTIF / FILE / STORAGE / UI / SEC / OPS / DB

### 4.2 用例字段

- 前置条件：环境、账号角色、配置开关、已有数据
- 步骤：可用 UI 操作或 API 调用描述（API 需注明方法与路径）
- 预期结果：HTTP 状态、业务响应码、页面提示、数据落库/权限变化

## 5. 功能测试用例

### 5.1 认证与会话（AUTH）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-AUTH-001 | 获取登录验证码（图片） | `elexvx.security.captcha.enabled=true` | 1）GET `/auth/captcha` | 返回 `ApiResponse.success`，包含验证码数据结构，前端可渲染/校验 | P0 |
| FT-AUTH-002 | 获取滑块验证码（AJ Captcha） | AJ Captcha 已启用 | 1）POST `/captcha/get` 2）按组件规范提交校验 POST `/captcha/check` | 服务返回 `ResponseModel`，校验成功后可进入登录流程 | P0 |
| FT-AUTH-003 | 账号密码登录成功 | 有可用账号（建议 admin） | 1）POST `/auth/login`（填账号/密码/验证码字段按前端要求） | 返回 token；后续带 `Authorization: Bearer <token>` 可访问受保护接口 | P0 |
| FT-AUTH-004 | 登录失败（密码错误） | 有可用账号 | 1）POST `/auth/login`（错误密码） | 返回业务失败响应；前端提示“账号或密码错误/登录失败”且不写入 token | P0 |
| FT-AUTH-005 | 短信验证码发送（防重复） | 短信模块可用；手机号有效格式 | 1）POST `/auth/sms/send` 2）5 秒内重复提交同参数 | 第一次成功；第二次返回 HTTP 429 + 提示“不允许重复提交，请稍后再试” | P0 |
| FT-AUTH-006 | 邮箱验证码发送（防重复） | 邮箱模块可用；邮箱格式正确 | 1）POST `/auth/email/send` 2）5 秒内重复提交 | 同 FT-AUTH-005 | P0 |
| FT-AUTH-007 | 短信/邮箱登录成功 | 已获取有效验证码 | 1）POST `/auth/login/sms` 或 `/auth/login/email` 2）如需确认：POST `/auth/login/confirm` | 登录成功返回 token；确认接口返回一致的登录态 | P0 |
| FT-AUTH-008 | 获取当前用户信息 | 已登录 | 1）GET `/auth/user` | 返回当前用户基础信息、角色等；与菜单/权限联动一致 | P0 |
| FT-AUTH-009 | 修改个人资料（脱敏输入不覆盖） | 已登录；账号存在手机号/邮箱等字段 | 1）GET `/auth/profile` 2）PUT `/auth/profile`（将手机号字段改为带 `*` 的脱敏值） | 更新成功但真实字段不被 `*` 覆盖；再次查询仍为原值 | P1 |
| FT-AUTH-010 | 修改密码与重新登录 | 已登录 | 1）POST `/auth/password` 2）登出 3）用新密码登录 | 新密码生效；旧密码不可登录；登出后 token 失效 | P0 |
| FT-AUTH-011 | 找回/重置密码 | 账号具备可用手机号/邮箱 | 1）POST `/auth/password/reset`（按接口要求） | 返回成功；可用新密码登录（或收到重置提示） | P1 |
| FT-AUTH-012 | 401/403 统一提示 | 准备两个角色：有权限/无权限 | 1）不带 token 调用受保护接口 2）带 token 调用无权限接口（如 `/system/user/page`） | 401 返回“未登录或登录已失效，请重新登录”；403 返回“权限不足，请联系管理员开通” | P0 |
| FT-AUTH-013 | 并发登录处理（SSE 通知与决策） | 开启多端登录策略；有两个浏览器会话 | 1）会话A登录并订阅 GET `/auth/concurrent/stream` 2）会话B再次登录触发提示 3）POST `/auth/concurrent/decision` approve/reject | approve：旧会话被强制下线；reject：新会话登录被拒绝/等待确认；两端提示一致 | P1 |
| FT-AUTH-014 | 角色切换与权限即时生效 | 当前用户拥有多个角色 | 1）GET `/auth/assume-role/options` 2）POST `/auth/assume-role` 切到另一角色 3）刷新菜单与页面 | 返回新的用户信息；菜单与可访问页面随角色变更 | P0 |

### 5.2 菜单与权限（RBAC / MENU）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-RBAC-001 | 获取当前用户菜单路由 | 已登录 | 1）GET `/get-menu-list-i18n` | 返回 `MenuListResult`；前端基于结果生成动态路由 | P0 |
| FT-RBAC-002 | 无菜单配置时返回空列表 | 菜单未配置或清空 | 1）GET `/get-menu-list-i18n` | 返回空 routes，不报错；前端提示或兜底首页 | P2 |
| FT-MENU-001 | 获取后台菜单树 | 具备 `system:SystemMenu:query` | 1）GET `/system/menu/tree` | 返回树结构；包含目录/页面节点；排序正确 | P0 |
| FT-MENU-002 | 新建菜单节点 | 具备 `system:SystemMenu:create` | 1）POST `/system/menu`（创建目录/页面） | 创建成功；再次查询树可见；字段校验正确 | P0 |
| FT-MENU-003 | 更新菜单节点 | 具备 `system:SystemMenu:update` | 1）PUT `/system/menu/{id}`（修改名称/路由/权限码等） | 更新成功；前端刷新后路由与可见性变化符合预期 | P0 |
| FT-MENU-004 | 删除菜单（非级联） | 存在父子菜单 | 1）DELETE `/system/menu/{id}?cascade=false` | 若存在子节点：删除失败并提示原因；若无子节点：删除成功 | P1 |
| FT-MENU-005 | 删除菜单（级联） | 存在父子菜单 | 1）DELETE `/system/menu/{id}?cascade=true` | 父子节点均删除；树结构正确；相关页面入口消失 | P1 |
| FT-MENU-006 | 菜单拖拽排序 | 具备更新权限 | 1）PUT `/system/menu/reorder`（按请求结构提交排序） | 返回成功；再次拉取树排序符合新顺序 | P1 |
| FT-RBAC-003 | 权限码校验一致性 | 准备无权限账号 | 1）用无权限账号调用 `/system/menu/tree` | 返回 403；前端提示“权限不足” | P0 |
| FT-RBAC-004 | 种子菜单生成 | 具备菜单创建权限 | 1）POST `/system/menu/seed-default?overwrite=false` | 返回新增数量；树结构包含默认菜单；不重复插入 | P2 |

### 5.3 用户管理（USER）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-USER-001 | 用户分页查询与筛选 | 具备 `system:SystemUser:query` | 1）GET `/system/user/page`（keyword/mobile/status/time 等组合） | 返回分页结果；筛选条件生效；时间格式容错 | P0 |
| FT-USER-002 | 用户详情查看 | 具备查询权限 | 1）GET `/system/user/{id}` | 返回用户详情；敏感字段按脱敏规则展示 | P0 |
| FT-USER-003 | 新建用户 | 具备 `system:SystemUser:create` | 1）POST `/system/user`（必填字段） | 创建成功；列表可查；默认状态/角色符合预期 | P0 |
| FT-USER-004 | 更新用户（脱敏字段不覆盖） | 具备 `system:SystemUser:update` | 1）PUT `/system/user/{id}`（把手机号/邮箱传为带 `*` 的脱敏值） | 更新成功；真实字段不被覆盖；再次查询保持原值 | P1 |
| FT-USER-005 | 删除用户（禁止删除自己） | 具备删除权限 | 1）DELETE `/system/user/{selfId}` | 返回失败提示“不允许删除当前登录用户”；用户仍存在 | P0 |
| FT-USER-006 | 删除用户（正常删除） | 具备删除权限 | 1）DELETE `/system/user/{id}`（非本人） | 删除成功；列表不可见；相关关联清理符合预期 | P0 |
| FT-USER-007 | 重置用户密码 | 具备更新权限 | 1）POST `/system/user/{id}/reset-password`（默认仅 body 传 password；query 仅在开启 elexvx.security.allow-password-in-query 后兼容） 2）用新密码登录 | 返回成功；新密码可登录 | P1 |
| FT-USER-008 | 防重复提交生效 | 写接口具备 @RepeatSubmit | 1）5 秒内重复 POST `/system/user` 或 PUT `/system/user/{id}` | 第二次返回 HTTP 429；不产生重复数据 | P1 |

### 5.4 角色管理（ROLE）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-ROLE-001 | 角色列表查询 | 具备 `system:SystemRole:query` | 1）GET `/system/role/list` | 返回角色列表；含 admin 等默认角色 | P0 |
| FT-ROLE-002 | 角色详情 | 具备查询权限 | 1）GET `/system/role/{id}` | 返回角色详情；字段正确 | P1 |
| FT-ROLE-003 | 新建角色 | 具备 `system:SystemRole:create` | 1）POST `/system/role` | 创建成功；可用于用户切换/授权 | P0 |
| FT-ROLE-004 | 更新角色 | 具备 `system:SystemRole:update` | 1）PUT `/system/role/{id}` | 更新成功；关联用户权限变化符合预期 | P1 |
| FT-ROLE-005 | 删除角色 | 具备 `system:SystemRole:delete` | 1）DELETE `/system/role/{id}` | 删除成功；若有关联约束，需返回可理解的错误提示 | P1 |
| FT-ROLE-006 | 防重复提交生效 | 写接口具备 @RepeatSubmit | 1）5 秒内重复提交 POST/PUT/DELETE | 第二次返回 429；数据不重复变化 | P2 |

### 5.5 机构管理（ORG）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-ORG-001 | 获取组织树 | 具备 `system:SystemOrg:query` | 1）GET `/system/org/tree` | 返回树结构；父子关系正确 | P0 |
| FT-ORG-002 | 新建机构节点 | 具备 `system:SystemOrg:create` | 1）POST `/system/org`（parentId、name 等） | 创建成功；树中可见；排序字段默认合理 | P0 |
| FT-ORG-003 | 更新机构节点 | 具备 `system:SystemOrg:update` | 1）PUT `/system/org/{id}` | 更新成功；树中展示更新后的字段 | P1 |
| FT-ORG-004 | 删除机构节点 | 具备 `system:SystemOrg:delete` | 1）DELETE `/system/org/{id}` | 无子节点则删除成功；有子节点应失败并提示或要求先清理 | P1 |
| FT-ORG-005 | 机构排序调整 | 具备更新权限 | 1）PUT `/system/org/reorder` | 返回成功；树顺序与新排序一致 | P2 |

### 5.6 字典管理（DICT）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-DICT-001 | 字典分页查询 | 已登录 | 1）GET `/system/dict/page`（keyword/status） | 返回分页数据；筛选生效 | P1 |
| FT-DICT-002 | 新建字典 | 已登录 | 1）POST `/system/dict` | 创建成功；可继续维护条目 | P1 |
| FT-DICT-003 | 更新/删除字典 | 已登录 | 1）PUT `/system/dict/{id}` 2）DELETE `/system/dict/{id}` | 更新成功；删除成功或提示存在依赖 | P2 |
| FT-DICT-004 | 条目分页查询 | 已存在字典 | 1）GET `/system/dict/{dictId}/items` | 返回条目分页；筛选生效 | P1 |
| FT-DICT-005 | 通过 code 拉取条目列表 | 已存在字典 code | 1）GET `/system/dict/code/{code}/items` | 返回条目列表（用于前端下拉等） | P0 |
| FT-DICT-006 | 新建/更新/删除条目 | 已存在字典 | 1）POST `/system/dict/{dictId}/items` 2）PUT `/system/dict/items/{id}` 3）DELETE `/system/dict/items/{id}` | CRUD 正常；状态字段生效 | P1 |
| FT-DICT-007 | 下载导入模板 | 已登录 | 1）GET `/system/dict/items/template` | 浏览器下载成功；模板列与导入逻辑匹配 | P1 |
| FT-DICT-008 | 导入条目（正常/异常） | 准备符合模板的文件 | 1）POST `/system/dict/{dictId}/items/import` 2）导入重复 key/非法格式文件 | 正常导入返回统计；异常返回清晰错误原因 | P1 |
| FT-DICT-009 | 导出条目 | 已存在条目 | 1）GET `/system/dict/{dictId}/items/export` | 下载成功；内容与条目一致 | P2 |

### 5.7 消息（MSG）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-MSG-001 | 查询个人消息列表 | 已登录 | 1）GET `/message/list` | 返回仅属于当前用户的消息；包含已读状态 | P0 |
| FT-MSG-002 | 发送点对点消息 | 已登录；存在接收者用户ID | 1）POST `/message`（toUserId/content/type/quality） | 发送成功；接收者列表可见；发送者不越权 | P0 |
| FT-MSG-003 | 广播消息 | 已登录；具备广播权限策略（若有） | 1）POST `/message/broadcast` | 返回发送数量；多用户收到消息 | P1 |
| FT-MSG-004 | 标记已读/未读 | 已登录；存在消息ID | 1）POST `/message/read?id=...` 2）POST `/message/unread?id=...` | 状态切换成功；列表展示一致 | P0 |
| FT-MSG-005 | 全部标记已读 | 已登录 | 1）POST `/message/read-all` | 返回变更数量；未读变为已读 | P1 |
| FT-MSG-006 | 删除消息（仅本人） | 已登录；存在本人消息ID | 1）DELETE `/message/{id}` | 删除成功；其他用户无法删除非本人消息 | P0 |

### 5.8 公告（ANN）与通知（NOTIF）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-ANN-001 | 公告列表与筛选 | 已登录 | 1）GET `/announcement`（keyword/status/priority） | 返回分页；筛选生效 | P1 |
| FT-ANN-002 | 公告详情 | 已存在公告 | 1）GET `/announcement/{id}` | 返回详情；内容正确 | P1 |
| FT-ANN-003 | 创建/更新/删除公告 | 已登录 | 1）POST `/announcement` 2）PUT `/announcement/{id}` 3）DELETE `/announcement/{id}` | CRUD 正常；操作记录可追溯 | P1 |
| FT-ANN-004 | 发布/撤回公告 | 已存在公告 | 1）POST `/announcement/{id}/publish?publish=true` 2）publish=false | 状态切换成功；`/announcement/latest` 仅返回已发布 | P0 |
| FT-NOTIF-001 | 通知列表与筛选 | 已登录 | 1）GET `/notification`（keyword/status/priority） | 返回分页；筛选生效 | P1 |
| FT-NOTIF-002 | 通知广播（快速发布） | 已登录 | 1）POST `/notification/broadcast` | 创建并发布成功；`/notification/latest` 可见 | P0 |
| FT-NOTIF-003 | 发布/撤回通知 | 已存在通知 | 1）POST `/notification/{id}/publish` | 状态切换成功；latest 结果一致 | P1 |

### 5.9 文件上传/下载与访问（FILE）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-FILE-001 | 普通文件上传成功（业务） | 已登录；文件类型允许（png/jpg/pdf 等） | 1）POST `/system/file/upload`（multipart：file） | 返回 `url`；可在浏览器访问 `GET /files/{token}` | P0 |
| FT-FILE-002 | 普通上传失败：空文件 | 已登录 | 1）POST `/system/file/upload`（不传 file 或空文件） | 返回 400“上传文件不能为空” | P0 |
| FT-FILE-003 | 普通上传失败：不支持扩展名 | 已登录；准备 `.exe/.zip` | 1）POST `/system/file/upload` | 返回 400“文件格式不支持” | P0 |
| FT-FILE-004 | 分片上传全流程 | 已登录；准备大文件 | 1）POST `/system/file/upload/init` 2）循环 POST `/system/file/upload/chunk` 3）GET `/system/file/upload/status` 4）POST `/system/file/upload/complete` | 返回最终 `url`；文件可访问；状态显示分片上传完成 | P0 |
| FT-FILE-005 | 取消分片上传 | 已 init 会话 | 1）POST `/system/file/upload/cancel`（uploadId） | 返回成功；再次查 status 显示已取消/不可继续 | P1 |
| FT-FILE-006 | 文件访问：HEAD 获取元信息 | 拿到有效 token | 1）HEAD `/files/{token}` | 返回 200；包含 `Content-Length`、`Accept-Ranges` | P1 |
| FT-FILE-007 | 文件访问：Range 断点续传 | 拿到有效 token；文件长度 > 0 | 1）GET `/files/{token}` 携带 `Range: bytes=0-99` | 返回 206；`Content-Range` 正确；内容长度为 100 | P1 |
| FT-FILE-008 | 文件访问：非法 Range | 拿到有效 token | 1）GET `/files/{token}` 携带 `Range: bytes=999999999-` | 返回 416；`Content-Range: bytes */<total>` | P2 |
| FT-FILE-009 | 文件访问：非法 token | 无 | 1）GET `/files/invalidToken` | 返回 404 | P0 |
| FT-FILE-010 | 文件下载资源管理 CRUD | 具备相关权限 | 1）GET `/system/file/resource` 2）POST `/system/file/resource` 3）PUT/DELETE | 列表分页正常；CRUD 生效；用于“文件下载”业务场景 | P2 |

### 5.10 对象存储配置（STORAGE）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-STORAGE-001 | 查询当前存储配置 | 具备 `system:SystemStorage:query` | 1）GET `/system/storage` | 返回当前配置；provider 等字段正确 | P1 |
| FT-STORAGE-002 | 保存存储配置（管理员） | 管理员 | 1）POST `/system/storage`（provider/local/aliyun/tencent） | 保存成功；操作日志记录“更新对象存储配置” | P1 |
| FT-STORAGE-003 | 测试存储配置（成功/失败） | 管理员；准备正确/错误参数 | 1）POST `/system/storage/test` | 正确参数返回 success；错误参数返回明确失败原因 | P0 |
| FT-STORAGE-004 | provider 变更后文件访问可用 | 已配置 provider；上传文件 | 1）上传文件得到 url 2）访问 `/files/{token}` | provider 匹配时可访问；不匹配应提示 404 或不可用（视配置与对象存在） | P1 |

### 5.11 UI 设置 / 模块注册 / 水印（UI）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-UI-001 | 获取公共 UI 配置（免登录） | 无 | 1）GET `/system/ui/public` | 返回基础 UI 字段；`smsEnabled/emailEnabled/captchaEnabled` 与配置/模块可用性一致 | P0 |
| FT-UI-002 | 获取管理 UI 配置（鉴权） | 已登录；具备查询权限之一 | 1）GET `/system/ui` | 返回更完整字段；无权限则 403“权限不足，请联系管理员开通” | P0 |
| FT-UI-003 | 保存 UI 配置（鉴权与模块约束） | 具备 update 权限 | 1）POST `/system/ui`（启用短信/邮箱开关） | 模块不可用时返回“短信模块未安装或未启用/邮箱模块未安装或未启用”；可用时保存成功 | P0 |
| FT-UI-004 | 发送测试邮件 | 已登录；邮箱模块可用且 enabled | 1）POST `/system/ui/email/test` | 返回成功；记录“发送测试邮件”；邮箱禁用时提示“邮箱验证已禁用” | P1 |
| FT-UI-005 | 系统文件上传（仅管理员） | 管理员；准备图片等文件 | 1）POST `/system/ui/upload`（multipart） | 上传成功返回 url；非管理员返回 403 | P1 |
| FT-UI-006 | 模块描述列表（用于前端展示） | 无或已登录 | 1）GET `/system/modules/descriptor` | 返回模块列表与描述；用于模块中心展示 | P2 |
| FT-UI-007 | 模块注册列表与启停/安装卸载 | 具备 `system:SystemModule:*` | 1）GET `/system/modules` 2）POST `/system/modules/{key}/enable|disable|install|uninstall` | 列表状态变化正确；变更后 `/system/ui/public` 的 enabled 字段联动 | P1 |
| FT-UI-008 | 水印配置查询（免登录） | 无 | 1）GET `/system/watermark` | 返回默认或当前水印配置；前端可渲染 | P0 |
| FT-UI-009 | 水印配置保存（管理员） | 管理员 | 1）POST `/system/watermark` | 保存成功；操作日志记录“更新水印配置” | P1 |

### 5.12 敏感词与敏感信息（SEC）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-SEC-001 | 防重复提交通用校验 | 任意带 `@RepeatSubmit` 的写接口 | 1）在 5 秒内重复提交同请求 | 第二次返回 HTTP 429；不产生重复副作用（详见规范文档）[API_SECURITY_SPEC.md](docs/API_SECURITY_SPEC.md#L18-L35) | P0 |
| FT-SEC-002 | 返回脱敏字段展示 | 有用户手机号/邮箱/身份证等 | 1）GET `/system/user/page` 或 `/auth/profile` | 响应中敏感字段按 `@Sensitive` 规则脱敏 | P1 |
| FT-SEC-003 | 脱敏输入忽略覆盖（用户管理） | 已存在带真实手机号的用户 | 1）PUT `/system/user/{id}`，手机号传 `138****0000` | 更新成功但真实手机号不变（详见规范）[API_SECURITY_SPEC.md](docs/API_SECURITY_SPEC.md#L100-L106) | P1 |
| FT-SEC-004 | 敏感词列表查询 | 已登录 | 1）GET `/system/sensitive/words`（keyword 可选） | 返回敏感词列表；筛选生效 | P2 |
| FT-SEC-005 | 敏感词分页查询 | 已登录 | 1）GET `/system/sensitive/words/page` | 返回分页数据 | P2 |
| FT-SEC-006 | 新建/删除敏感词 | 已登录 | 1）POST `/system/sensitive/words` 2）DELETE `/system/sensitive/words/{id}` | CRUD 正常；重复词返回合理提示 | P2 |
| FT-SEC-007 | 下载敏感词模板与导入 | 已登录；准备模板文件 | 1）GET `/system/sensitive/words/template` 2）POST `/system/sensitive/words/import` | 模板下载成功；导入返回统计；错误文件给出原因 | P2 |
| FT-SEC-008 | 保存/查询敏感词设置 | 已登录 | 1）GET `/system/sensitive/settings` 2）POST `/system/sensitive/settings` | 设置保存后可再次读出一致 | P3 |

### 5.13 运维与监控（OPS）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-OPS-001 | 健康检查（含 Redis 状态） | 服务已启动 | 1）GET `/health` | 返回 `redisEnabled/redisAvailable/redisMessage`，字段与配置/实际一致 | P0 |
| FT-OPS-002 | 服务器监控信息 | 已登录（或按权限配置） | 1）GET `/system/monitor/server` | 返回服务器信息 VO；页面可渲染 | P1 |
| FT-OPS-003 | Redis 监控开关：启用时可访问 | `elexvx.redis.enabled=true` 且 Redis 可用 | 1）GET `/system/monitor/redis` | 返回 Redis 信息 VO；字段合理 | P1 |
| FT-OPS-004 | Redis 监控开关：关闭时接口不可用 | `elexvx.redis.enabled=false` | 1）GET `/system/monitor/redis` | 接口不注册或返回 404；前端菜单入口隐藏/不可点 | P1 |
| FT-OPS-005 | 在线用户列表（管理员） | admin 角色；存在登录会话 | 1）GET `/system/monitor/online` | 返回分页在线用户；筛选生效 | P0 |
| FT-OPS-006 | 强制下线在线用户 | admin 角色；存在目标 sessionId | 1）DELETE `/system/monitor/online/{sessionId}` | 返回成功；被踢用户收到失效提示并重新登录 | P0 |

### 5.14 数据库脚本（DB）

| 用例ID | 用例名称 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| FT-DB-001 | MySQL demo 脚本导入 | MySQL 可用；空库 | 1）导入 `database/demo/tdesign_init.sql` | 导入成功；表/数据存在；后端可正常启动登录 | P0 |
| FT-DB-002 | MySQL schema 脚本导入 | MySQL 可用；空库 | 1）导入 `database/schema/tdesign_schema.sql` | 导入成功；后端启动后核心接口不报表缺失 | P1 |
| FT-DB-003 | PostgreSQL 脚本导入 | PostgreSQL 可用；空库 | 1）导入 `database/demo/tdesign_init_pg.sql` 2）或 `database/schema/tdesign_schema_pg.sql` | 导入成功；关键表结构与字段存在 | P1 |
| FT-DB-004 | Oracle 脚本导入 | Oracle 可用；空 schema | 1）导入 `database/demo/tdesign_init_oracle.sql` 2）或 `database/schema/tdesign_schema_oracle.sql` | 导入成功；关键表结构存在 | P2 |
| FT-DB-005 | SQL Server 脚本导入 | SQL Server 可用；空库 | 1）导入 `database/demo/tdesign_init_sqlserver.sql` 2）或 `database/schema/tdesign_schema_sqlserver.sql` | 导入成功；关键表结构存在 | P2 |

## 6. 执行检查清单（可执行性）

- 后端启动正常，无报错；Swagger/OpenAPI 可访问（若启用）
- `server.servlet.context-path=/api` 生效：所有接口以 `/api` 前缀访问
- 登录成功后，`Authorization: Bearer <token>` 头能访问受保护接口
- 401/403 错误提示与 [SecurityConfig.java](backend/src/main/java/top/elexvx/admin/config/SecurityConfig.java#L64-L88) 一致
- 已覆盖带 `@RepeatSubmit` 的写接口重复提交场景（详见 [API_SECURITY_SPEC.md](docs/API_SECURITY_SPEC.md#L18-L83)）
- 文件上传得到的 `url` 能通过 `/files/{token}` 访问，并支持 Range
