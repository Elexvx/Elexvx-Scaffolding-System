package elexvx.admin.mapper;

import elexvx.admin.entity.Announcement;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AnnouncementMapper {
  Announcement selectById(@Param("id") Long id);
  List<Announcement> selectPageByStatus(@Param("statuses") List<String> statuses, @Param("offset") int offset, @Param("limit") int limit);
  long countByStatus(@Param("statuses") List<String> statuses);
  List<Announcement> selectLatestPublished(@Param("limit") int limit);
  int insert(Announcement entity);
  int update(Announcement entity);
  int deleteById(@Param("id") Long id);
}
