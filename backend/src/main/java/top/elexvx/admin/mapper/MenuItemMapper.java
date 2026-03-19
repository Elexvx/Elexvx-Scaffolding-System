package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.MenuItemEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MenuItemMapper {
  long count();
  List<MenuItemEntity> selectAll();
  List<MenuItemEntity> selectAllEnabled();
  MenuItemEntity selectById(@Param("id") Long id);
  List<MenuItemEntity> selectByIds(@Param("ids") Collection<Long> ids);
  MenuItemEntity selectByRouteName(@Param("routeName") String routeName);
  int insert(MenuItemEntity entity);
  int update(MenuItemEntity entity);
  int deleteById(@Param("id") Long id);
  int deleteByIds(@Param("ids") Collection<Long> ids);
}
