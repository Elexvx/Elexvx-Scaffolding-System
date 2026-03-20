# 前端结构拆分（Phase 5）

## 本轮处理范围

- `frontend/src/pages/login/**`
- `frontend/src/views/login/**`
- `frontend/src/pages/system/personalize/components/ImagePanel.vue`
- `frontend/src/pages/system/personalize/components/VerificationPanel.vue`
- `frontend/src/pages/system/personalize/components/PersonalizePanel.vue`

## 登录主实现归位

本轮已将登录主实现归位到 `frontend/src/views/login/`：

- `frontend/src/views/login/index.vue`
- `frontend/src/views/login/components/LoginPageMain.vue`
- `frontend/src/views/login/components/LoginFormPanel.vue`
- `frontend/src/views/login/components/RegisterFormPanel.vue`
- `frontend/src/views/login/components/ForgotPasswordPanel.vue`

旧路径仍兼容：

- `frontend/src/pages/login/index.vue`
- `frontend/src/pages/login/components/Login.vue`
- `frontend/src/pages/login/components/Register.vue`
- `frontend/src/pages/login/components/ForgotPassword.vue`

以上文件现仅保留兼容入口，不再承载登录/注册/找回密码的真实流程实现。

## 登录拆分方式

### components/

- `LoginPageMain.vue`：页面骨架、标题区、面板切换、底部信息与布局编排。
- `LoginFormPanel.vue`：登录表单视图编排。
- `RegisterFormPanel.vue`：注册表单视图编排。
- `ForgotPasswordPanel.vue`：找回密码表单视图编排。
- `LoginHeroPanel.vue`：右侧背景展示区。
- `AgreementCheckbox.vue`：协议勾选包装组件。
- `VerificationCodeInput.vue`：验证码输入 + 发送按钮。
- `LoginTabs.vue`：登录方式 tab 切换。

### hooks/

- `useLoginPageState.ts`：登录页路由态、维护提示、版权信息、背景图。
- `useLoginForm.ts`：登录表单状态、验证码、提交、重定向、记住账号。
- `useRegisterForm.ts`：注册表单状态、密码策略、验证码、提交错误归一化。
- `useForgotPasswordForm.ts`：找回密码表单状态与验证码发送。
- `useAuthAgreement.ts`：协议勾选校验。
- `useVerificationCode.ts`：短信/邮箱验证码发送与倒计时。
- `useLoginRedirect.ts`：登录后跳转归一化。
- `useCaptcha.ts`：图片/拖动验证码通用状态与刷新逻辑。

### schema/

- `loginSchema.ts`
- `registerSchema.ts`
- `forgotPasswordSchema.ts`

### constants/utils/types

- `constants/loginOptions.ts`：初始表单、倒计时与记住账号常量。
- `utils/loginMappers.ts`：输入清洗、错误提示映射。
- `utils/loginGuards.ts`：密码连续字符判断、登录方式兜底选择。
- `types.ts`：登录页表单、验证码、面板类型。

## personalize 拆分方式

### ImagePanel

主实现目录：`frontend/src/pages/system/personalize/components/image-panel/`

- `ImagePanelMain.vue`：表单编排、保存入口、上传组件装配。
- `ImageUploadSection.vue`：单个图片配置区（URL/上传/预览）。
- `ImagePreviewSection.vue`：裁切弹窗。
- `hooks/useImagePanelForm.ts`：读取与组织图片配置。
- `hooks/useImageUpload.ts`：Logo/二维码上传、裁切上传、文件输入交互。
- `hooks/useImagePreview.ts`：裁切弹窗标题、提示、预览框参数。
- `hooks/useImagePanelPermissions.ts`：管理员权限与 token 获取。
- `constants/imagePanelOptions.ts`：Logo 裁切规格。
- `utils/imagePanelMappers.ts`：上传响应转换、保存 payload 组装。
- `types.ts`：图片字段与裁切类型。

### VerificationPanel

主实现目录：`frontend/src/pages/system/personalize/components/verification-panel/`

- `VerificationPanelMain.vue`：短信/邮箱 tab 编排与保存动作。
- `VerificationSmsSection.vue`：短信配置区域。
- `VerificationEmailSection.vue`：邮箱配置区域。
- `hooks/useVerificationPanelForm.ts`：配置读入与 payload 构造。
- `hooks/useVerificationSections.ts`：provider 选项、只读态、分区显隐。
- `hooks/useVerificationPanelPermissions.ts`：权限判断。
- `constants/verificationOptions.ts`：tab 与 provider 常量。
- `utils/verificationMappers.ts`：接口字段映射、tab 解析。
- `types.ts`：短信/邮箱配置类型。

### 最小收口

- `frontend/src/pages/system/personalize/components/PersonalizePanel.vue` 已将菜单路径映射、默认首页归一化、payload 组织下沉到 `personalize-panel/hooks` 与 `personalize-panel/utils`。

## 本轮结构原则

1. `views` 承担登录页主实现，`pages` 仅保留兼容壳。
2. 表单 schema、错误映射、默认值、权限判断、验证码/上传逻辑必须下沉。
3. 主面板只保留布局编排、hooks 装配和少量保存胶水逻辑。
4. 不改接口字段名、不改登录/注册/找回密码与 personalize 保存语义。
5. 组件控制在可维护规模内，超长表单优先拆成 section + hook。

## 本轮后仍待治理的组件

当前 `frontend/src/pages/login/**` 与 `frontend/src/pages/system/personalize/**` 范围内，已无明显大于 400 行的组件；但仍建议继续治理以下“职责偏重”组件：

1. `frontend/src/pages/system/personalize/components/AgreementPanel.vue`
2. `frontend/src/pages/system/personalize/components/WatermarkPanel.vue`
3. `frontend/src/pages/login/components/AgreementCheck.vue`
4. `frontend/src/pages/system/personalize/index.vue`

## 下一轮建议优先级

1. `frontend/src/pages/system/personalize/components/WatermarkPanel.vue`
2. `frontend/src/pages/system/personalize/components/AgreementPanel.vue`
3. `frontend/src/pages/login/components/AgreementCheck.vue`
4. `frontend/src/pages/system/personalize/index.vue`

## 验证要求

- 登录：账号密码、短信、邮箱三种路径分别验证。
- 注册：图片验证码/拖动验证码、密码策略提示、协议勾选。
- 找回密码：短信验证码发送、重置成功后回到登录。
- personalize：Logo 裁切上传、二维码上传、登录背景图上传、短信/邮箱配置保存、默认首页保存。
- 所有改动完成后执行 `npm --prefix frontend run build`。
