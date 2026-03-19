package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.UserOrgUnitRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserOrgUnitMapper {
  List<Long> selectOrgUnitIdsByUserId(@Param("userId") Long userId);
  List<Long> selectUserIdsByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
  List<UserOrgUnitRelation> selectAll();
  int deleteByUserId(@Param("userId") Long userId);
  int insertUserOrgUnits(@Param("userId") Long userId, @Param("orgUnitIds") List<Long> orgUnitIds);
  int insertOrgUnitUsers(@Param("orgUnitId") Long orgUnitId, @Param("userIds") List<Long> userIds);
}
