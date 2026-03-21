# ui-setting-model-refactor

## 背景与目标

- `backend/src/main/java/elexvx/admin/dto/UiSettingRequest.java` 原先承载 UI 外观、登录、验证、安全策略等全部字段，模型边界模糊。
- 本次目标是将请求模型按领域切分，并保持 `/system/ui` 单入口兼容旧前端大对象 payload。

## 新请求模型

- `backend/src/main/java/elexvx/admin/model/req/setting/UiBrandSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/UiLayoutSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/UiThemeSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/UiLoginSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/UiFooterSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/UiLegalSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/UiSystemSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/VerificationProviderSettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/SessionPolicySettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/SecurityPolicySettingRequest.java`
- `backend/src/main/java/elexvx/admin/model/req/setting/WatermarkSettingRequest.java`

## 兼容策略

- `UiSettingRequest` 保留原扁平字段，新增领域化子模型字段与映射方法：
  - `toBrandSettingRequest()`
  - `toLayoutSettingRequest()`
  - `toThemeSettingRequest()`
  - `toLoginSettingRequest()`
  - `toFooterSettingRequest()`
  - `toLegalSettingRequest()`
  - `toSystemSettingRequest()`
  - `toVerificationProviderSettingRequest()`
  - `toSessionPolicySettingRequest()`
  - `toSecurityPolicySettingRequest()`
- 映射优先级：子模型字段优先，子模型为空时回退到扁平字段，确保旧前端与新前端 payload 均可用。

## 服务层拆分

- 新增领域服务（UI 配置）：
  - `backend/src/main/java/elexvx/admin/service/setting/UiBrandSettingDomainService.java`
  - `backend/src/main/java/elexvx/admin/service/setting/UiLayoutSettingDomainService.java`
  - `backend/src/main/java/elexvx/admin/service/setting/UiThemeSettingDomainService.java`
  - `backend/src/main/java/elexvx/admin/service/setting/UiFooterSettingDomainService.java`
  - `backend/src/main/java/elexvx/admin/service/setting/UiLoginSettingDomainService.java`
  - `backend/src/main/java/elexvx/admin/service/setting/UiLegalSettingDomainService.java`
  - `backend/src/main/java/elexvx/admin/service/setting/UiSystemSettingDomainService.java`
- `UiSettingService` 变为编排层，只负责聚合读取与领域服务分发。
- `VerificationSettingService.applyRequest` 改为接收 `VerificationProviderSettingRequest`。
- `SecuritySettingService.applyRequest` 改为接收 `SessionPolicySettingRequest + SecurityPolicySettingRequest`。
- `UiSettingController` 在 `save` 中统一走领域映射分发，URL 与总体语义不变。

## 字段归属

- 品牌/UI：`websiteName/appVersion/logoExpandedUrl/logoCollapsedUrl/faviconUrl/qrCodeUrl`
- 布局：`defaultHome/showFooter/isSidebarCompact/showBreadcrumb/menuAutoCollapsed/layout/splitMenu/sideMode/isFooterAside/isSidebarFixed/isHeaderFixed/isUseTabsRouter/showHeader/headerGithubUrl/headerHelpUrl`
- 主题：`autoTheme/lightStartTime/darkStartTime/mode/brandTheme`
- 登录：`loginBgUrl/allowMultiDeviceLogin`
- 页脚与版权：`footerCompany/footerIcp/copyrightStartYear`
- 法务协议：`userAgreement/privacyAgreement`
- 系统运行：`logRetentionDays/maintenanceEnabled/maintenanceMessage`
- 验证通道：短信与邮箱全部字段
- 会话策略：`sessionTimeoutMinutes/tokenTimeoutMinutes/tokenRefreshGraceMinutes/allowUrlTokenParam`
- 安全策略：验证码与密码策略字段

## 测试护栏

- `backend/src/test/java/elexvx/admin/dto/UiSettingRequestMappingTest.java`
  - 覆盖旧扁平 payload 映射。
  - 覆盖新子模型字段优先级。

## 风险与后续

- 风险点：
  - 前后端并存期内，字段重复来源可能引发“同字段双写”认知偏差。
- 后续建议：
  - 前端逐步收敛到子模型 payload；
  - 后端在后续版本增加字段冲突检测与告警。
