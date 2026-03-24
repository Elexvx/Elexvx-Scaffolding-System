package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class MenuItemCreateRequest {
  private Long parentId;

  @NotBlank(message = "nodeType 不能为空")
  private String nodeType;

  @NotBlank(message = "path 不能为空")
  private String path;

  @NotBlank(message = "routeName 不能为空")
  private String routeName;

  private String component;
  private String redirect;

  @NotBlank(message = "titleZhCn 不能为空")
  private String titleZhCn;

  private String titleEnUs;
  private String icon;
  private Boolean hidden;
  private String frameSrc;
  private Boolean frameBlank;
  private Boolean enabled;
  private String requiredModules;
  private Integer orderNo;
  private String actions;

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public String getNodeType() {
    return nodeType;
  }

  public void setNodeType(String nodeType) {
    this.nodeType = nodeType;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
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

  public String getTitleZhCn() {
    return titleZhCn;
  }

  public void setTitleZhCn(String titleZhCn) {
    this.titleZhCn = titleZhCn;
  }

  public String getTitleEnUs() {
    return titleEnUs;
  }

  public void setTitleEnUs(String titleEnUs) {
    this.titleEnUs = titleEnUs;
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

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getRequiredModules() {
    return requiredModules;
  }

  public void setRequiredModules(String requiredModules) {
    this.requiredModules = requiredModules;
  }

  public Integer getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(Integer orderNo) {
    this.orderNo = orderNo;
  }

  public String getActions() {
    return actions;
  }

  public void setActions(String actions) {
    this.actions = actions;
  }
}

