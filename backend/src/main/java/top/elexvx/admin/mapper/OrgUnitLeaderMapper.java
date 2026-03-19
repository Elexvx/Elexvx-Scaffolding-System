package top.elexvx.admin.mapper;

import top.elexvx.admin.dto.OrgUnitLeaderPair;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrgUnitLeaderMapper {
  List<Long> selectLeaderIds(@Param("orgUnitId") Long orgUnitId);
  List<OrgUnitLeaderPair> selectLeaderPairsByOrgUnitIds(@Param("orgUnitIds") List<Long> orgUnitIds);
  int deleteByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
  int insertLeaders(@Param("orgUnitId") Long orgUnitId, @Param("userIds") List<Long> userIds);
}
