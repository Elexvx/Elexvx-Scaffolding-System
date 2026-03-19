package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.SensitivePageSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SensitivePageSettingMapper {
  List<SensitivePageSetting> selectAll();
  SensitivePageSetting selectByPageKey(@Param("pageKey") String pageKey);
  SensitivePageSetting selectById(@Param("id") Long id);
  int insert(SensitivePageSetting setting);
  int update(SensitivePageSetting setting);
}
