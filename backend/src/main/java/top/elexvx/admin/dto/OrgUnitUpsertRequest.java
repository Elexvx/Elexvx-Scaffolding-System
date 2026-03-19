package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class OrgUnitUpsertRequest {
  private Long parentId;

  @NotBlank(message = "机构名称不能为空")
  private String name;

  private String shortName;

  @NotBlank(message = "机构类型不能为空")
  private String type;

  private Integer sortOrder;
  private Integer status;
  private List<Long> leaderIds;
  private String phone;
  private String email;

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

  public List<Long> getLeaderIds() {
    return leaderIds;
  }

  public void setLeaderIds(List<Long> leaderIds) {
    this.leaderIds = leaderIds;
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
}
