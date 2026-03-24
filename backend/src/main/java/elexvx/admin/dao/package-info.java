/**
 * DAO（组合查询）层。
 *
 * <p>用于承载跨表/复杂查询或批处理等不适合放入单表 Mapper 的数据访问逻辑。DAO 返回的对象通常仍属于持久化视角，
 * 由 Service 再转换为对外 VO。
 */
package elexvx.admin.dao;

