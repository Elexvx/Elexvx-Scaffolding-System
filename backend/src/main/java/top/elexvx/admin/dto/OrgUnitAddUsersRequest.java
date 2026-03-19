package top.elexvx.admin.dto;

import java.util.List;

public class OrgUnitAddUsersRequest {
  private List<Long> userIds;

  public List<Long> getUserIds() {
    return userIds;
  }

  public void setUserIds(List<Long> userIds) {
    this.userIds = userIds;
  }
}
