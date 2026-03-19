package top.elexvx.admin.vo;

import java.util.Map;
import java.util.List;

public class RouteMeta {
  private Map<String, String> title;
  private String icon;
  private Boolean hidden;
  private Integer orderNo;
  private String frameSrc;
  private Boolean frameBlank;
  private String resource;
  private List<String> actions;
  private List<String> requiredPermissions;
  private List<String> requiredModules;

  public Map<String, String> getTitle() {
    return title;
  }

  public void setTitle(Map<String, String> title) {
    this.title = title;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public Boolean getHidden() {
    return hidden;
  }

  public void setHidden(Boolean hidden) {
    this.hidden = hidden;
  }

  public Integer getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(Integer orderNo) {
    this.orderNo = orderNo;
  }

  public String getFrameSrc() {
    return frameSrc;
  }

  public void setFrameSrc(String frameSrc) {
    this.frameSrc = frameSrc;
  }

  public Boolean getFrameBlank() {
    return frameBlank;
  }

  public void setFrameBlank(Boolean frameBlank) {
    this.frameBlank = frameBlank;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public List<String> getActions() {
    return actions;
  }

  public void setActions(List<String> actions) {
    this.actions = actions;
  }

  public List<String> getRequiredPermissions() {
    return requiredPermissions;
  }

  public void setRequiredPermissions(List<String> requiredPermissions) {
    this.requiredPermissions = requiredPermissions;
  }

  public List<String> getRequiredModules() {
    return requiredModules;
  }

  public void setRequiredModules(List<String> requiredModules) {
    this.requiredModules = requiredModules;
  }
}
