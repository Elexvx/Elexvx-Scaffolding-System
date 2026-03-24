package elexvx.admin.service.menu;

import static org.assertj.core.api.Assertions.assertThat;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.service.menu.support.MenuRouteAssembler;
import elexvx.admin.service.menu.support.MenuTreeBuilder;
import elexvx.admin.vo.RouteItem;
import java.util.List;
import org.junit.jupiter.api.Test;

class MenuRouteAssemblerTest {
  @Test
  void shouldBuildRoutePermissionsAndPruneEmptyLayout() {
    MenuRouteAssembler assembler = new MenuRouteAssembler(new MenuTreeBuilder());
    MenuItemEntity layout = new MenuItemEntity();
    layout.setId(1L);
    layout.setParentId(null);
    layout.setNodeType("DIR");
    layout.setPath("/system");
    layout.setRouteName("system");
    layout.setComponent("LAYOUT");
    layout.setTitleZhCn("系统");
    layout.setEnabled(true);

    MenuItemEntity page = new MenuItemEntity();
    page.setId(2L);
    page.setParentId(1L);
    page.setNodeType("PAGE");
    page.setPath("role");
    page.setRouteName("SystemRole");
    page.setComponent("/system/role/index");
    page.setTitleZhCn("角色管理");
    page.setEnabled(true);
    page.setActions("query,update");

    List<RouteItem> routes = assembler.pruneEmptyLayoutRoutes(assembler.toRouteTree(List.of(layout, page)));
    assertThat(routes).hasSize(1);
    RouteItem roleRoute = routes.get(0).getChildren().get(0);
    assertThat(roleRoute.getMeta().getRequiredPermissions()).containsExactly("system:SystemRole:query", "system:SystemRole:update");
  }
}
