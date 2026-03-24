package elexvx.admin.entity;

public class UiLayoutSetting {
  private Long id;
  private String defaultHome;
  private Boolean showFooter;
  private Boolean isSidebarCompact;
  private Boolean showBreadcrumb;
  private Boolean menuAutoCollapsed;
  private String layout;
  private Boolean splitMenu;
  private String sideMode;
  private Boolean isFooterAside;
  private Boolean isSidebarFixed;
  private Boolean isHeaderFixed;
  private Boolean isUseTabsRouter;
  private Boolean showHeader;
  private String headerGithubUrl;
  private String headerHelpUrl;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getDefaultHome() { return defaultHome; }
  public void setDefaultHome(String defaultHome) { this.defaultHome = defaultHome; }
  public Boolean getShowFooter() { return showFooter; }
  public void setShowFooter(Boolean showFooter) { this.showFooter = showFooter; }
  public Boolean getIsSidebarCompact() { return isSidebarCompact; }
  public void setIsSidebarCompact(Boolean isSidebarCompact) { this.isSidebarCompact = isSidebarCompact; }
  public Boolean getShowBreadcrumb() { return showBreadcrumb; }
  public void setShowBreadcrumb(Boolean showBreadcrumb) { this.showBreadcrumb = showBreadcrumb; }
  public Boolean getMenuAutoCollapsed() { return menuAutoCollapsed; }
  public void setMenuAutoCollapsed(Boolean menuAutoCollapsed) { this.menuAutoCollapsed = menuAutoCollapsed; }
  public String getLayout() { return layout; }
  public void setLayout(String layout) { this.layout = layout; }
  public Boolean getSplitMenu() { return splitMenu; }
  public void setSplitMenu(Boolean splitMenu) { this.splitMenu = splitMenu; }
  public String getSideMode() { return sideMode; }
  public void setSideMode(String sideMode) { this.sideMode = sideMode; }
  public Boolean getIsFooterAside() { return isFooterAside; }
  public void setIsFooterAside(Boolean isFooterAside) { this.isFooterAside = isFooterAside; }
  public Boolean getIsSidebarFixed() { return isSidebarFixed; }
  public void setIsSidebarFixed(Boolean isSidebarFixed) { this.isSidebarFixed = isSidebarFixed; }
  public Boolean getIsHeaderFixed() { return isHeaderFixed; }
  public void setIsHeaderFixed(Boolean isHeaderFixed) { this.isHeaderFixed = isHeaderFixed; }
  public Boolean getIsUseTabsRouter() { return isUseTabsRouter; }
  public void setIsUseTabsRouter(Boolean isUseTabsRouter) { this.isUseTabsRouter = isUseTabsRouter; }
  public Boolean getShowHeader() { return showHeader; }
  public void setShowHeader(Boolean showHeader) { this.showHeader = showHeader; }
  public String getHeaderGithubUrl() { return headerGithubUrl; }
  public void setHeaderGithubUrl(String headerGithubUrl) { this.headerGithubUrl = headerGithubUrl; }
  public String getHeaderHelpUrl() { return headerHelpUrl; }
  public void setHeaderHelpUrl(String headerHelpUrl) { this.headerHelpUrl = headerHelpUrl; }
}
