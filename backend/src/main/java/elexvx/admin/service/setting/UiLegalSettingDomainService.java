package elexvx.admin.service.setting;

import elexvx.admin.entity.UiLegalSetting;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.mapper.UiLegalSettingMapper;
import elexvx.admin.model.req.setting.UiLegalSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiLegalSettingDomainService {
  private final UiLegalSettingMapper mapper;

  public UiLegalSettingDomainService(UiLegalSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiLegalSetting legal = mapper.selectTop();
    if (legal == null) {
      return;
    }
    out.setUserAgreement(legal.getUserAgreement());
    out.setPrivacyAgreement(legal.getPrivacyAgreement());
  }

  public void apply(UiLegalSettingRequest req) {
    UiLegalSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiLegalSetting();
    }
    boolean changed = false;
    if (req.userAgreement() != null) { s.setUserAgreement(req.userAgreement()); changed = true; }
    if (req.privacyAgreement() != null) { s.setPrivacyAgreement(req.privacyAgreement()); changed = true; }
    if (changed) {
      upsert(s);
    }
  }

  private void upsert(UiLegalSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
