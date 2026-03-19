package elexvx.admin.mapper;

import elexvx.admin.entity.SysDict;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysDictMapper {
  List<SysDict> selectAll();

  List<SysDict> selectPage(
    @Param("keyword") String keyword,
    @Param("status") Integer status,
    @Param("offset") int offset,
    @Param("limit") int limit
  );

  long countByKeyword(@Param("keyword") String keyword, @Param("status") Integer status);

  SysDict selectById(@Param("id") Long id);

  SysDict selectByCode(@Param("code") String code);

  int insert(SysDict dict);

  int update(SysDict dict);

  int deleteById(@Param("id") Long id);
}
