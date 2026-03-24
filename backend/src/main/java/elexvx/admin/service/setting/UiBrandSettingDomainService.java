package elexvx.admin.service.setting;

import elexvx.admin.entity.UiBrandSetting;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.mapper.UiBrandSettingMapper;
import elexvx.admin.model.req.setting.UiBrandSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiBrandSettingDomainService {
  private final UiBrandSettingMapper mapper;

  public UiBrandSettingDomainService(UiBrandSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiBrandSetting brand = mapper.selectTop();
    if (brand == null) {
      return;
    }
    out.setWebsiteName(brand.getWebsiteName());
    out.setAppVersion(brand.getAppVersion());
    out.setLogoExpandedUrl(brand.getLogoExpandedUrl());
    out.setLogoCollapsedUrl(brand.getLogoCollapsedUrl());
    out.setFaviconUrl(brand.getFaviconUrl());
    out.setQrCodeUrl(brand.getQrCodeUrl());
  }

  public void apply(UiBrandSettingRequest req) {
    UiBrandSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiBrandSetting();
    }
    boolean changed = false;
    if (req.websiteName() != null) {
      s.setWebsiteName(req.websiteName());
      changed = true;
    }
    if (req.appVersion() != null) {
      s.setAppVersion(req.appVersion());
      changed = true;
    }
    if (req.logoExpandedUrl() != null) {
      s.setLogoExpandedUrl(req.logoExpandedUrl());
      changed = true;
    }
    if (req.logoCollapsedUrl() != null) {
      s.setLogoCollapsedUrl(req.logoCollapsedUrl());
      changed = true;
    }
    if (req.faviconUrl() != null) {
      s.setFaviconUrl(req.faviconUrl());
      changed = true;
    }
    if (req.qrCodeUrl() != null) {
      s.setQrCodeUrl(req.qrCodeUrl());
      changed = true;
    }
    if (changed) {
      upsert(s);
    }
  }

  private void upsert(UiBrandSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
