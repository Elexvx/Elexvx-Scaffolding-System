/**
 * 认证与访问控制（Security Domain）。
 *
 * <p>该包围绕“无状态 Token + 服务端会话存储”的模型提供：
 * <ul>
 *   <li>Token 解析与请求鉴权上下文（Principal / Session / Context）。</li>
 *   <li>访问控制与权限决策的基础能力。</li>
 * </ul>
 *
 * <p>注意：鉴权相关逻辑需要非常谨慎，任何“默认放行/容错”都可能成为安全隐患。
 */
package top.elexvx.admin.security;

