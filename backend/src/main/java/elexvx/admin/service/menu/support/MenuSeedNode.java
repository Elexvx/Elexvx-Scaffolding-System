package elexvx.admin.service.menu.support;

public record MenuSeedNode(
  String parentRouteName,
  Long parentId,
  String nodeType,
  String path,
  String routeName,
  String component,
  String redirect,
  String titleZhCn,
  String titleEnUs,
  String icon,
  boolean hidden,
  String frameSrc,
  boolean frameBlank,
  boolean enabled,
  int orderNo,
  String actions
) {
  public MenuSeedNode withParentId(Long newParentId) {
    return new MenuSeedNode(
      parentRouteName,
      newParentId,
      nodeType,
      path,
      routeName,
      component,
      redirect,
      titleZhCn,
      titleEnUs,
      icon,
      hidden,
      frameSrc,
      frameBlank,
      enabled,
      orderNo,
      actions
    );
  }
}
