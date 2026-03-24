package elexvx.admin.model.req.setting;

public record UiLayoutSettingRequest(
  String defaultHome,
  Boolean showFooter,
  Boolean isSidebarCompact,
  Boolean showBreadcrumb,
  Boolean menuAutoCollapsed,
  String layout,
  Boolean splitMenu,
  String sideMode,
  Boolean isFooterAside,
  Boolean isSidebarFixed,
  Boolean isHeaderFixed,
  Boolean isUseTabsRouter,
  Boolean showHeader,
  String headerGithubUrl,
  String headerHelpUrl
) {}
