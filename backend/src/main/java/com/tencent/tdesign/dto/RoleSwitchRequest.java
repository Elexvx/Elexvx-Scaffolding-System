package com.tencent.tdesign.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;

public class RoleSwitchRequest {
  @NotEmpty(message = "roles 不能为空")
  private List<String> roles;

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
