package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.Notification;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NotificationMapper {
  Notification selectById(@Param("id") Long id);
  List<Notification> selectPage(
    @Param("statuses") List<String> statuses,
    @Param("keywordLike") String keywordLike,
    @Param("priority") String priority,
    @Param("offset") int offset,
    @Param("limit") int limit
  );
  long count(
    @Param("statuses") List<String> statuses,
    @Param("keywordLike") String keywordLike,
    @Param("priority") String priority
  );
  List<Notification> selectLatestPublished(@Param("limit") int limit);
  int insert(Notification entity);
  int update(Notification entity);
  int deleteById(@Param("id") Long id);
}
