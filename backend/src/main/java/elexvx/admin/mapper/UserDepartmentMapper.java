package elexvx.admin.mapper;

import elexvx.admin.dto.UserIdLongValue;
import elexvx.admin.dto.UserIdStringValue;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserDepartmentMapper {
  List<Long> selectDepartmentIdsByUserId(@Param("userId") Long userId);
  List<String> selectDepartmentNamesByUserId(@Param("userId") Long userId);
  List<UserIdStringValue> selectDepartmentNamesByUserIds(@Param("userIds") Collection<Long> userIds);
  List<UserIdLongValue> selectDepartmentIdsByUserIds(@Param("userIds") Collection<Long> userIds);
  int deleteByUserId(@Param("userId") Long userId);
  int insertUserDepartments(@Param("userId") Long userId, @Param("departmentIds") List<Long> departmentIds);
}
