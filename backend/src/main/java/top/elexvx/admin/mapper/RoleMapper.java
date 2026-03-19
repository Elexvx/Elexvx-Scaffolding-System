package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.RoleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RoleMapper {
  List<RoleEntity> selectAll();
  RoleEntity selectById(@Param("id") Long id);
  RoleEntity selectByName(@Param("name") String name);
  long countByName(@Param("name") String name);
  int insert(RoleEntity entity);
  int update(RoleEntity entity);
  int deleteById(@Param("id") Long id);
}
