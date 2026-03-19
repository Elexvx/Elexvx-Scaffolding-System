package top.elexvx.admin.mapper;

import top.elexvx.admin.entity.StorageSetting;

public interface StorageSettingMapper {
  StorageSetting selectTop();
  int insert(StorageSetting setting);
  int update(StorageSetting setting);
}
