package top.elexvx.admin.vo;

import java.util.List;

public class MenuListResult {
  private List<RouteItem> list;

  public MenuListResult() {}

  public MenuListResult(List<RouteItem> list) {
    this.list = list;
  }

  public List<RouteItem> getList() {
    return list;
  }

  public void setList(List<RouteItem> list) {
    this.list = list;
  }
}
