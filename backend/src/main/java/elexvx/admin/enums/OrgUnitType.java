package elexvx.admin.enums;

public enum OrgUnitType {
  UNIT("单位"),
  DEPARTMENT("部门"),
  SECTION("科室"),
  TEAM("班组"),
  USER("用户");

  private final String label;

  OrgUnitType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static OrgUnitType fromValue(String value) {
    if (value == null || value.isBlank()) return null;
    for (OrgUnitType type : values()) {
      if (type.name().equalsIgnoreCase(value)) return type;
    }
    return null;
  }
}
