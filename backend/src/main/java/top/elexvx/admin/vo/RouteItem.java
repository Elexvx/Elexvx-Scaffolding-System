package top.elexvx.admin.vo;

import java.util.ArrayList;
import java.util.List;

public class RouteItem {
  private String path;
  private String name;
  private String component;
  private String redirect;
  private RouteMeta meta;
  private List<RouteItem> children;

  public RouteItem() {}

  public RouteItem(String path, String name, String component, RouteMeta meta) {
    this.path = path;
    this.name = name;
    this.component = component;
    this.meta = meta;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public String getRedirect() {
    return redirect;
  }

  public void setRedirect(String redirect) {
    this.redirect = redirect;
  }

  public RouteMeta getMeta() {
    return meta;
  }

  public void setMeta(RouteMeta meta) {
    this.meta = meta;
  }

  public List<RouteItem> getChildren() {
    return children;
  }

  public void setChildren(List<RouteItem> children) {
    this.children = children;
  }

  public void addChild(RouteItem child) {
    if (this.children == null) this.children = new ArrayList<>();
    this.children.add(child);
  }
}
