package com.tencent.tdesign.mapper;

import com.tencent.tdesign.dto.UserGuidPair;
import com.tencent.tdesign.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
  UserEntity selectById(@Param("id") Long id);
  UserEntity selectByAccount(@Param("account") String account);
  UserEntity selectByMobile(@Param("mobile") String mobile);
  UserEntity selectByPhone(@Param("phone") String phone);
  UserEntity selectByEmail(@Param("email") String email);
  List<UserEntity> selectAll();
  List<UserEntity> selectByIds(@Param("ids") Collection<Long> ids);
  List<Long> selectAllIds();
  List<UserEntity> selectMissingGuidUsers(@Param("offset") int offset, @Param("limit") int limit);
  int updateGuidBatch(@Param("pairs") List<UserGuidPair> pairs);
  List<UserEntity> selectPage(
    @Param("keyword") String keyword,
    @Param("mobile") String mobile,
    @Param("orgUnitId") Long orgUnitId,
    @Param("departmentId") Long departmentId,
    @Param("status") Integer status,
    @Param("startTime") java.time.LocalDateTime startTime,
    @Param("endTime") java.time.LocalDateTime endTime,
    @Param("offset") int offset,
    @Param("limit") int limit
  );
  long countByKeyword(
    @Param("keyword") String keyword,
    @Param("mobile") String mobile,
    @Param("orgUnitId") Long orgUnitId,
    @Param("departmentId") Long departmentId,
    @Param("status") Integer status,
    @Param("startTime") java.time.LocalDateTime startTime,
    @Param("endTime") java.time.LocalDateTime endTime
  );
  long countByAccount(@Param("account") String account);
  long countByEmailIgnoreCase(@Param("email") String email);
  long countByIdCard(@Param("idCard") String idCard);
  int insert(UserEntity entity);
  int update(UserEntity entity);
  int deleteById(@Param("id") Long id);
}
