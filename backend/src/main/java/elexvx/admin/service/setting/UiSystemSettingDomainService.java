package elexvx.admin.service.setting;

import elexvx.admin.entity.UiSetting;
import elexvx.admin.entity.UiSystemSetting;
import elexvx.admin.mapper.UiSystemSettingMapper;
import elexvx.admin.model.req.setting.UiSystemSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiSystemSettingDomainService {
  private final UiSystemSettingMapper mapper;

  public UiSystemSettingDomainService(UiSystemSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiSystemSetting system = mapper.selectTop();
    if (system == null) {
      return;
    }
    out.setLogRetentionDays(system.getLogRetentionDays());
    out.setMaintenanceEnabled(system.getMaintenanceEnabled());
    out.setMaintenanceMessage(system.getMaintenanceMessage());
  }

  public void apply(UiSystemSettingRequest req) {
    UiSystemSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiSystemSetting();
    }
    boolean changed = false;
    if (req.logRetentionDays() != null) { s.setLogRetentionDays(req.logRetentionDays()); changed = true; }
    if (req.maintenanceEnabled() != null) { s.setMaintenanceEnabled(req.maintenanceEnabled()); changed = true; }
    if (req.maintenanceMessage() != null) { s.setMaintenanceMessage(req.maintenanceMessage()); changed = true; }
    if (changed) {
      upsert(s);
    }
  }

  public Integer getLogRetentionDays() {
    UiSystemSetting setting = mapper.selectTop();
    return setting == null ? null : setting.getLogRetentionDays();
  }

  private void upsert(UiSystemSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
