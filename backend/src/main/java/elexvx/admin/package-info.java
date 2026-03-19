/**
 * 应用主包。
 *
 * <p>约定：
 * <ul>
 *   <li>Controller 仅负责参数校验/鉴权边界与返回包装，不承载复杂业务。</li>
 *   <li>Service 承载业务编排与事务边界，调用 Mapper/DAO 完成持久化访问。</li>
 *   <li>VO 用于对外响应，DTO 用于入参，Entity 对应数据库表结构。</li>
 *   <li>全局异常统一由 {@code config.GlobalExceptionHandler} 转为 {@code vo.ApiResponse}。</li>
 * </ul>
 */
package elexvx.admin;

