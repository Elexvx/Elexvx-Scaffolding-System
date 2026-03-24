# API Security Spec

This document describes the repeat-submit guard and sensitive-data masking rules
used by backend APIs.

## Authentication Token Usage

All authenticated API requests must include an `Authorization` header using the
Bearer scheme:

```
Authorization: Bearer <token>
```

URL/query parameters are not accepted for tokens by default. If legacy clients
must be supported, enable the `allowUrlTokenParam` security setting explicitly.


## Password Reset Payload Rule

`POST /system/user/{id}/reset-password` now accepts password from JSON body by default:

```json
{ "password": "newPassword" }
```

Passing password in query parameter is deprecated and disabled by default to avoid leaking sensitive data into URL logs/proxies.
If you are migrating legacy clients, temporarily enable `elexvx.security.allow-password-in-query=true` and switch clients to body payloads as soon as possible.

## Repeat Submit Guard

Purpose:
- Prevent duplicate writes caused by double-clicks, retries, or slow networks.
- Avoid duplicate create/update/delete actions or repeated side effects.

Usage:
- Add `@RepeatSubmit` on controller methods that modify state.
- Optional params:
  - `interval` (ms, default 5000)
  - `message` (default "不允许重复提交，请稍后再试")

Behavior:
- Requests are keyed by: handler signature + HTTP method + URI + query + user
  identity + arguments hash.
- If the same key appears again within the interval, the request is rejected.
- Response: HTTP 429 + `ApiResponse` code 429 with the configured message.

Applied endpoints (current):
- `backend/src/main/java/elexvx/admin/controller/AuthController.java`
  - /auth/sms/send
  - /auth/login/confirm
  - /auth/register
  - /auth/profile (PUT)
  - /auth/password
  - /auth/logout
  - /auth/assume-role
- `backend/src/main/java/elexvx/admin/controller/ConcurrentLoginController.java`
  - /auth/concurrent/decision
- `backend/src/main/java/elexvx/admin/controller/SystemUserController.java`
  - POST /system/user
  - PUT /system/user/{id}
  - DELETE /system/user/{id}
  - POST /system/user/{id}/reset-password
- `backend/src/main/java/elexvx/admin/controller/SystemRoleController.java`
  - POST /system/role
  - PUT /system/role/{id}
  - DELETE /system/role/{id}
- `backend/src/main/java/elexvx/admin/controller/SystemMenuController.java`
  - POST /system/menu
  - PUT /system/menu/{id}
  - DELETE /system/menu/{id}
  - PUT /system/menu/reorder
  - POST /system/menu/seed-default
- `backend/src/main/java/elexvx/admin/controller/UiSettingController.java`
  - POST /system/ui
  - POST /system/ui/upload
- `backend/src/main/java/elexvx/admin/controller/WatermarkController.java`
  - POST /system/watermark
- `backend/src/main/java/elexvx/admin/controller/StorageController.java`
  - POST /system/storage
  - POST /system/storage/test
- `backend/src/main/java/elexvx/admin/controller/UserParameterController.java`
  - POST /api/user/parameters
  - PUT /api/user/parameters/{id}
  - DELETE /api/user/parameters/{id}
- `backend/src/main/java/elexvx/admin/controller/FileUploadController.java`
  - POST /system/file/upload
- `backend/src/main/java/elexvx/admin/controller/MessageController.java`
  - POST /message
  - POST /message/broadcast
  - POST /message/read
  - POST /message/unread
  - POST /message/read-all
  - DELETE /message/{id}

## Sensitive Data Masking

Purpose:
- Mask sensitive fields in API responses to reduce data exposure.

Usage:
- Annotate response fields with `@Sensitive` and a `DesensitizedType`.
- Current types:
  - `USERNAME`, `PASSWORD`, `ID_CARD`, `PHONE`, `EMAIL`, `BANK_CARD`, `CAR_LICENSE`

Current coverage:
- `backend/src/main/java/elexvx/admin/vo/UserListItem.java`
  - `mobile`, `phone`, `email`, `idCard`
- `backend/src/main/java/elexvx/admin/vo/UserProfileResponse.java`
  - `mobile`, `phone`, `email`, `idCard`

Update behavior with masked input:
- To avoid overwriting real values with masked placeholders, updates ignore
  values that contain `*`.
- Implemented in:
  - `backend/src/main/java/elexvx/admin/service/UserAdminService.java`
  - `backend/src/main/java/elexvx/admin/service/AuthService.java`

Example:
```java
@Sensitive(desensitizedType = DesensitizedType.PHONE)
private String mobile;
```
