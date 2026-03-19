package elexvx.admin.mapper;

import elexvx.admin.entity.StorageSetting;

public interface StorageSettingMapper {
  StorageSetting selectTop();
  int insert(StorageSetting setting);
  int update(StorageSetting setting);
}
