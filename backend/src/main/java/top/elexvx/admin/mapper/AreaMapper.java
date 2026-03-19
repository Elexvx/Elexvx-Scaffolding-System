package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.AreaEntity;
import top.elexvx.admin.vo.AreaNodeRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AreaMapper {
  AreaEntity selectById(@Param("id") Integer id);

  List<AreaEntity> selectByParentAndName(@Param("parentId") Integer parentId, @Param("name") String name);

  List<AreaNodeRow> selectChildren(@Param("parentId") Integer parentId);
}
