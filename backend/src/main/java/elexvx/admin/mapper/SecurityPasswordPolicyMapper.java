package elexvx.admin.mapper;

import elexvx.admin.entity.SecurityPasswordPolicy;

public interface SecurityPasswordPolicyMapper {
  SecurityPasswordPolicy selectTop();
  int insert(SecurityPasswordPolicy setting);
  int update(SecurityPasswordPolicy setting);
}
