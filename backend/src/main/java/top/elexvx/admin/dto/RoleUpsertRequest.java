package top.elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class RoleUpsertRequest {
  @NotBlank(message = "角色名不能为空")
  private String name;

  private String description;
  /**
   * 兼容旧版前端直接传入权限码，格式如 system:SystemUser:create。
   *
   * <p>当 menuActionMap 传值时，以 menuActionMap 计算结果为准。
   */
  private List<String> permissions;
  /**
   * 页面（菜单）权限，最终写入 role_menus(role_id, menu_id)。
   */
  private List<Long> menuIds;
  /**
   * 页面动作权限映射：menuId -> actions。
   *
   * <p>服务层会将 actions 转换为 system:{routeName}:{action} 后落库 role_permissions。
   */
  private Map<Long, List<String>> menuActionMap;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public List<Long> getMenuIds() {
    return menuIds;
  }

  public void setMenuIds(List<Long> menuIds) {
    this.menuIds = menuIds;
  }

  public Map<Long, List<String>> getMenuActionMap() {
    return menuActionMap;
  }

  public void setMenuActionMap(Map<Long, List<String>> menuActionMap) {
    this.menuActionMap = menuActionMap;
  }
}
