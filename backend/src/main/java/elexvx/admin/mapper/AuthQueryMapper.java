package elexvx.admin.mapper;

import elexvx.admin.dto.UserIdStringValue;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthQueryMapper {
  int deleteRoleMenusByMenuIds(@Param("menuIds") Collection<Long> menuIds);
  List<String> findRoleNamesByUserId(@Param("userId") long userId);
  List<UserIdStringValue> findRoleNamesByUserIds(@Param("userIds") Collection<Long> userIds);
  List<String> findPermissionsByUserId(@Param("userId") long userId);
  List<String> findPermissionsByRoleNames(@Param("roleNames") Collection<String> roleNames);
  List<String> findPermissionsByRoleId(@Param("roleId") long roleId);
  List<Long> findMenuIdsByRoleId(@Param("roleId") long roleId);
  List<Long> findMenuIdsByUserId(@Param("userId") long userId);
  List<Long> findMenuIdsByRoleNames(@Param("roleNames") Collection<String> roleNames);
  List<String> findExistingRoleNames(@Param("roleNames") Collection<String> roleNames);
  int deleteUserRoles(@Param("userId") long userId);
  int insertUserRoles(@Param("userId") long userId, @Param("roles") List<String> roles);
  int deleteRolePermissions(@Param("roleId") long roleId);
  int insertRolePermissions(@Param("roleId") long roleId, @Param("permissions") List<String> permissions);
  int deleteRoleMenus(@Param("roleId") long roleId);
  int insertRoleMenus(@Param("roleId") long roleId, @Param("menuIds") List<Long> menuIds);
  int insertMissingAdminMenus();
  Long selectRoleIdByName(@Param("name") String name);
  long countRolePermission(@Param("roleId") long roleId, @Param("permission") String permission);
  int insertRolePermission(@Param("roleId") long roleId, @Param("permission") String permission);
}
