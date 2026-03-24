package elexvx.admin.mapper;

import elexvx.admin.entity.FileResource;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileResourceMapper {
  FileResource selectById(@Param("id") Long id);
  FileResource selectByFileUrl(@Param("fileUrl") String fileUrl);
  List<FileResource> selectPage(@Param("offset") int offset, @Param("limit") int limit);
  long count();
  int insert(FileResource entity);
  int update(FileResource entity);
  int deleteById(@Param("id") Long id);
}
