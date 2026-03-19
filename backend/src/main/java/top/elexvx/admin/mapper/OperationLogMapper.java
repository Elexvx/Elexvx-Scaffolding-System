package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.OperationLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperationLogMapper {
  List<OperationLogEntity> selectPage(
    @Param("keyword") String keyword,
    @Param("action") String action,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime,
    @Param("userId") Long userId,
    @Param("offset") int offset,
    @Param("limit") int limit
  );

  long count(
    @Param("keyword") String keyword,
    @Param("action") String action,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime,
    @Param("userId") Long userId
  );

  List<OperationLogEntity> selectAll(
    @Param("keyword") String keyword,
    @Param("action") String action,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime,
    @Param("userId") Long userId
  );

  int insert(OperationLogEntity entity);

  int deleteBefore(@Param("threshold") LocalDateTime threshold);
}
