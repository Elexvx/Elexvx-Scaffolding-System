package elexvx.admin.entity;

public class UserOrgUnitRelation {
  private Long userId;
  private Long orgUnitId;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getOrgUnitId() {
    return orgUnitId;
  }

  public void setOrgUnitId(Long orgUnitId) {
    this.orgUnitId = orgUnitId;
  }
}
