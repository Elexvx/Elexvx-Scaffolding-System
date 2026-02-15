package com.tencent.tdesign.vo;

import java.util.List;
import java.util.Map;

/**
 * 权限目录项（页面 + 动作 + 权限点）。
 */
public class PermissionCatalogItem {
  private Long menuId;
  private String routeName;
  private String title;
  private List<String> actions;
  private Map<String, String> permissionCodes;

  public Long getMenuId() {
    return menuId;
  }

  public void setMenuId(Long menuId) {
    this.menuId = menuId;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<String> getActions() {
    return actions;
  }

  public void setActions(List<String> actions) {
    this.actions = actions;
  }

  public Map<String, String> getPermissionCodes() {
    return permissionCodes;
  }

  public void setPermissionCodes(Map<String, String> permissionCodes) {
    this.permissionCodes = permissionCodes;
  }
}
