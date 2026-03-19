/**
 * 业务服务层（Service）。
 *
 * <p>职责：
 * <ul>
 *   <li>承载业务规则、编排流程与事务边界。</li>
 *   <li>聚合对 Mapper/DAO 的调用，统一处理缓存、幂等与一致性策略。</li>
 *   <li>对 Controller 提供稳定的业务接口，并将持久化模型转换为 VO。</li>
 * </ul>
 */
package top.elexvx.admin.service;

