package top.elexvx.admin.vo;

import java.util.List;

public class UserInfoResponse {
  private Long id;
  private String guid;
  private String name;
  private String avatar;
  private List<String> roles;
  private List<String> orgUnitNames;
  private List<String> permissions;
  private List<String> assignedRoles;
  private boolean roleSimulated;

  public UserInfoResponse() {}

  public UserInfoResponse(String name, String avatar, List<String> roles, List<String> permissions) {
    this.name = name;
    this.avatar = avatar;
    this.roles = roles;
    this.permissions = permissions;
  }

  public String getName() {
    return name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public List<String> getOrgUnitNames() {
    return orgUnitNames;
  }

  public void setOrgUnitNames(List<String> orgUnitNames) {
    this.orgUnitNames = orgUnitNames;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public List<String> getAssignedRoles() {
    return assignedRoles;
  }

  public void setAssignedRoles(List<String> assignedRoles) {
    this.assignedRoles = assignedRoles;
  }

  public boolean isRoleSimulated() {
    return roleSimulated;
  }

  public void setRoleSimulated(boolean roleSimulated) {
    this.roleSimulated = roleSimulated;
  }
}
