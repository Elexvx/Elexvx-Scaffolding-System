package elexvx.admin.mapper;

import elexvx.admin.dto.UserIdLongValue;
import elexvx.admin.dto.UserIdStringValue;
import elexvx.admin.entity.OrgUnitEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrgUnitMapper {
  OrgUnitEntity selectById(@Param("id") Long id);
  List<OrgUnitEntity> selectAll();
  List<OrgUnitEntity> selectByUserId(@Param("userId") Long userId);
  List<String> selectNamesByUserId(@Param("userId") Long userId);
  List<Long> selectIdsByUserId(@Param("userId") Long userId);
  List<UserIdStringValue> selectNamesByUserIds(@Param("userIds") Collection<Long> userIds);
  List<UserIdLongValue> selectIdsByUserIds(@Param("userIds") Collection<Long> userIds);
  List<OrgUnitEntity> selectByIds(@Param("ids") Collection<Long> ids);
  Integer selectMaxSortOrder(@Param("parentId") Long parentId);
  int insert(OrgUnitEntity entity);
  int update(OrgUnitEntity entity);
  int deleteById(@Param("id") Long id);
  long countChildren(@Param("id") Long id);
  int updateParentAndSort(@Param("id") Long id, @Param("parentId") Long parentId, @Param("sortOrder") Integer sortOrder);
}
