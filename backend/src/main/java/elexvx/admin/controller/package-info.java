/**
 * REST API 控制器层。
 *
 * <p>职责边界：
 * <ul>
 *   <li>解析/校验请求参数（DTO）与权限边界控制（由 Security 统一处理）。</li>
 *   <li>调用 Service 完成业务编排，返回 VO/统一响应包装 {@code vo.ApiResponse}。</li>
 *   <li>避免在 Controller 写复杂业务或直接操作 Mapper。</li>
 * </ul>
 */
package elexvx.admin.controller;

