# 消息与通知公告接口使用说明

本文档介绍如何在系统中调用公共消息接口和通知公告接口。

## 1. 公共消息接口 (私信/系统通知)

用于向特定用户发送消息。

### 内部 Service 调用 (推荐)

在 Java 代码中注入 `MessageService` 并调用 `send` 方法：

```java
@Autowired
private MessageService messageService;

// 发送消息
// 参数：接收人ID, 内容, 类型, 优先级
messageService.send(1L, "您有一条新的系统通知", "notification", "high");
```

### REST API 调用

- **接口地址**: `POST /api/message`
- **权限**: 需要登录
- **请求体**:
```json
{
  "toUserId": 1,
  "content": "消息内容",
  "type": "notification",
  "quality": "high"
}
```

---

## 2. 通知公告接口 (全员广播)

用于向系统中所有激活用户发送广播消息。

### 内部 Service 调用 (推荐)

在 Java 代码中注入 `MessageService` 并调用 `broadcast` 方法：

```java
@Autowired
private MessageService messageService;

// 全员广播
// 参数：内容, 类型, 优先级
int count = messageService.broadcast("系统将于今晚24:00进行维护", "announcement", "high");
System.out.println("成功发送给 " + count + " 位用户");
```

### REST API 调用

- **接口地址**: `POST /api/message/broadcast`
- **权限**: 需要 `system:message:broadcast` 权限（通常仅限 admin）
- **请求体**:
```json
{
  "content": "全体公告内容",
  "type": "announcement",
  "quality": "high"
}
```

---

## 3. 参数说明

- **type (类型)**:
    - `notification`: 系统通知
    - `announcement`: 公告
    - `message`: 私信
- **quality (优先级/质量)**:
    - `low`: 普通
    - `middle`: 重要
    - `high`: 紧急
