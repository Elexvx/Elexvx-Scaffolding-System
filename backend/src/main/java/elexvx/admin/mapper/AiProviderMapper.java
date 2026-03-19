package elexvx.admin.mapper;

import elexvx.admin.entity.AiProviderSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AiProviderMapper {
  List<AiProviderSetting> selectAll();
  AiProviderSetting selectById(@Param("id") Long id);
  AiProviderSetting selectFirstDefaultEnabled();
  AiProviderSetting selectFirstEnabled();
  int insert(AiProviderSetting entity);
  int update(AiProviderSetting entity);
  int deleteById(@Param("id") Long id);
  int clearDefaultExcept(@Param("excludeId") Long excludeId);
}
