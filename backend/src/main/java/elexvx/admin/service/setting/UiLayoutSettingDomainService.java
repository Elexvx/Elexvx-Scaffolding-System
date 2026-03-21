package elexvx.admin.service.setting;

import elexvx.admin.entity.UiLayoutSetting;
import elexvx.admin.entity.UiSetting;
import elexvx.admin.mapper.UiLayoutSettingMapper;
import elexvx.admin.model.req.setting.UiLayoutSettingRequest;
import org.springframework.stereotype.Service;

@Service
public class UiLayoutSettingDomainService {
  private final UiLayoutSettingMapper mapper;

  public UiLayoutSettingDomainService(UiLayoutSettingMapper mapper) {
    this.mapper = mapper;
  }

  public void fill(UiSetting out) {
    UiLayoutSetting layout = mapper.selectTop();
    if (layout == null) {
      return;
    }
    out.setDefaultHome(layout.getDefaultHome());
    out.setShowFooter(layout.getShowFooter());
    out.setIsSidebarCompact(layout.getIsSidebarCompact());
    out.setShowBreadcrumb(layout.getShowBreadcrumb());
    out.setMenuAutoCollapsed(layout.getMenuAutoCollapsed());
    out.setLayout(layout.getLayout());
    out.setSplitMenu(layout.getSplitMenu());
    out.setSideMode(layout.getSideMode());
    out.setIsFooterAside(layout.getIsFooterAside());
    out.setIsSidebarFixed(layout.getIsSidebarFixed());
    out.setIsHeaderFixed(layout.getIsHeaderFixed());
    out.setIsUseTabsRouter(layout.getIsUseTabsRouter());
    out.setShowHeader(layout.getShowHeader());
    out.setHeaderGithubUrl(layout.getHeaderGithubUrl());
    out.setHeaderHelpUrl(layout.getHeaderHelpUrl());
  }

  public void apply(UiLayoutSettingRequest req) {
    UiLayoutSetting s = mapper.selectTop();
    if (s == null) {
      s = new UiLayoutSetting();
    }
    boolean changed = false;
    if (req.defaultHome() != null) { s.setDefaultHome(req.defaultHome()); changed = true; }
    if (req.showFooter() != null) { s.setShowFooter(req.showFooter()); changed = true; }
    if (req.isSidebarCompact() != null) { s.setIsSidebarCompact(req.isSidebarCompact()); changed = true; }
    if (req.showBreadcrumb() != null) { s.setShowBreadcrumb(req.showBreadcrumb()); changed = true; }
    if (req.menuAutoCollapsed() != null) { s.setMenuAutoCollapsed(req.menuAutoCollapsed()); changed = true; }
    if (req.layout() != null) { s.setLayout(req.layout()); changed = true; }
    if (req.splitMenu() != null) { s.setSplitMenu(req.splitMenu()); changed = true; }
    if (req.sideMode() != null) { s.setSideMode(req.sideMode()); changed = true; }
    if (req.isFooterAside() != null) { s.setIsFooterAside(req.isFooterAside()); changed = true; }
    if (req.isSidebarFixed() != null) { s.setIsSidebarFixed(req.isSidebarFixed()); changed = true; }
    if (req.isHeaderFixed() != null) { s.setIsHeaderFixed(req.isHeaderFixed()); changed = true; }
    if (req.isUseTabsRouter() != null) { s.setIsUseTabsRouter(req.isUseTabsRouter()); changed = true; }
    if (req.showHeader() != null) { s.setShowHeader(req.showHeader()); changed = true; }
    if (req.headerGithubUrl() != null) { s.setHeaderGithubUrl(req.headerGithubUrl()); changed = true; }
    if (req.headerHelpUrl() != null) { s.setHeaderHelpUrl(req.headerHelpUrl()); changed = true; }
    if (changed) {
      upsert(s);
    }
  }

  private void upsert(UiLayoutSetting s) {
    if (s.getId() == null) {
      mapper.insert(s);
    } else {
      mapper.update(s);
    }
  }
}
