package top.elexvx.admin.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrgUnitNode {
  private Long id;
  private Long parentId;
  private String name;
  private String shortName;
  private String type;
  private String typeLabel;
  private Integer sortOrder;
  private Integer status;
  private Integer userCount;
  private String phone;
  private String email;
  private List<Long> leaderIds = new ArrayList<>();
  private List<String> leaderNames = new ArrayList<>();
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<OrgUnitNode> children = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTypeLabel() {
    return typeLabel;
  }

  public void setTypeLabel(String typeLabel) {
    this.typeLabel = typeLabel;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getUserCount() {
    return userCount;
  }

  public void setUserCount(Integer userCount) {
    this.userCount = userCount;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<Long> getLeaderIds() {
    return leaderIds;
  }

  public void setLeaderIds(List<Long> leaderIds) {
    this.leaderIds = leaderIds;
  }

  public List<String> getLeaderNames() {
    return leaderNames;
  }

  public void setLeaderNames(List<String> leaderNames) {
    this.leaderNames = leaderNames;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<OrgUnitNode> getChildren() {
    return children;
  }

  public void setChildren(List<OrgUnitNode> children) {
    this.children = children;
  }
}
