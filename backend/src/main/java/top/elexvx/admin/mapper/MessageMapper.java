package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.MessageEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageMapper {
  List<MessageEntity> listByUserId(@Param("userId") Long userId);
  MessageEntity selectById(@Param("id") String id);
  List<Long> selectUserIdsByPage(@Param("offset") int offset, @Param("limit") int limit);
  int insert(MessageEntity entity);
  int insertBatch(@Param("list") List<MessageEntity> list);
  int update(MessageEntity entity);
  int markAllReadByUserId(@Param("userId") Long userId);
  int deleteById(@Param("id") String id);
}
