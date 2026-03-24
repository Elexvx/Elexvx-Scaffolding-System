package elexvx.admin.service.auth.model.command;

import elexvx.admin.entity.UserEntity;

public class AuthLoginCommand {
  private UserEntity user;
  private Boolean force;

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public Boolean getForce() {
    return force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }
}
