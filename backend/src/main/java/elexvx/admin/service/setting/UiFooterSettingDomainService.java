package elexvx.admin.service.setting;

import elexvx.admin.entity.UiFooterSetting;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.mapper.UiFooterSettingMapper;
import elexvx.admin.model.req.setting.UiFooterSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiFooterSettingDomainService {
  private final UiFooterSettingMapper mapper;

  public UiFooterSettingDomainService(UiFooterSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiFooterSetting footer = mapper.selectTop();
    if (footer == null) {
      return;
    }
    out.setFooterCompany(footer.getFooterCompany());
    out.setFooterIcp(footer.getFooterIcp());
    out.setCopyrightStartYear(footer.getCopyrightStartYear());
  }

  public void apply(UiFooterSettingRequest req) {
    UiFooterSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiFooterSetting();
    }
    boolean changed = false;
    if (req.footerCompany() != null) { s.setFooterCompany(req.footerCompany()); changed = true; }
    if (req.footerIcp() != null) { s.setFooterIcp(req.footerIcp()); changed = true; }
    if (req.copyrightStartYear() != null) { s.setCopyrightStartYear(req.copyrightStartYear()); changed = true; }
    if (changed) {
      upsert(s);
    }
  }

  private void upsert(UiFooterSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
