package elexvx.admin.mapper;

import elexvx.admin.entity.UserParameterEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserParameterMapper {
  List<UserParameterEntity> selectByUserId(@Param("userId") Long userId);
  UserParameterEntity selectById(@Param("id") Long id);
  int insert(UserParameterEntity entity);
  int update(UserParameterEntity entity);
  int deleteById(@Param("id") Long id);
}
