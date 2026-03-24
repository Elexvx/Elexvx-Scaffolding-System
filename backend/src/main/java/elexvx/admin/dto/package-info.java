/**
 * DTO（请求入参）模型。
 *
 * <p>用于描述 API 入参结构，建议：
 * <ul>
 *   <li>只包含“输入”所需字段，不复用 Entity 作为入参，避免字段漂移与越权赋值。</li>
 *   <li>尽量通过 Bean Validation 标注约束（若项目启用）。</li>
 * </ul>
 */
package elexvx.admin.dto;

