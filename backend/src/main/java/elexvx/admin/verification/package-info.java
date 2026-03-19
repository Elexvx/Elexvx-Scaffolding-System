/**
 * 验证能力抽象（短信/邮箱等）。
 *
 * <p>通过 Provider + Registry 的方式将“验证码发送/校验”从认证流程中解耦，使不同渠道的实现可替换、可扩展。
 * Registry 负责按配置与模块安装状态选择可用 Provider。
 */
package elexvx.admin.verification;

