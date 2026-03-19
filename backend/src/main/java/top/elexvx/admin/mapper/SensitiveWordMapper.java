package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.SensitiveWord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SensitiveWordMapper {
  List<SensitiveWord> selectAll();
  List<SensitiveWord> selectAllEnabled();
  List<SensitiveWord> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
  long countByKeyword(@Param("keyword") String keyword);
  SensitiveWord selectById(@Param("id") Long id);
  SensitiveWord selectByWord(@Param("word") String word);
  int insert(SensitiveWord word);
  int update(SensitiveWord word);
  int deleteById(@Param("id") Long id);
}
