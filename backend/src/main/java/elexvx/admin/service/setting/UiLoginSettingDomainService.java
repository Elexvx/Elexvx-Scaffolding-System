package elexvx.admin.service.setting;

import elexvx.admin.entity.UiLoginSetting;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.mapper.UiLoginSettingMapper;
import elexvx.admin.model.req.setting.UiLoginSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiLoginSettingDomainService {
  private final UiLoginSettingMapper mapper;

  public UiLoginSettingDomainService(UiLoginSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiLoginSetting login = mapper.selectTop();
    if (login == null) {
      return;
    }
    out.setLoginBgUrl(login.getLoginBgUrl());
    out.setAllowMultiDeviceLogin(login.getAllowMultiDeviceLogin());
  }

  public void apply(UiLoginSettingRequest req) {
    UiLoginSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiLoginSetting();
    }
    boolean changed = false;
    if (req.loginBgUrl() != null) { s.setLoginBgUrl(req.loginBgUrl()); changed = true; }
    if (req.allowMultiDeviceLogin() != null) { s.setAllowMultiDeviceLogin(req.allowMultiDeviceLogin()); changed = true; }
    if (changed) {
      upsert(s);
    }
  }

  public boolean isMultiDeviceLoginAllowed() {
    UiLoginSetting setting = mapper.selectTop();
    return setting != null && Boolean.TRUE.equals(setting.getAllowMultiDeviceLogin());
  }

  private void upsert(UiLoginSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
