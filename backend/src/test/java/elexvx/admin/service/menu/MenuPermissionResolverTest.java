package elexvx.admin.service.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.security.AuthContext;
import elexvx.admin.service.PermissionFacade;
import elexvx.admin.service.menu.support.MenuPermissionResolver;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MenuPermissionResolverTest {
  @Test
  void shouldIncludeEnabledParentsForAccessibleChild() {
    PermissionFacade permissionFacade = Mockito.mock(PermissionFacade.class);
    AuthContext authContext = Mockito.mock(AuthContext.class);
    when(authContext.requireUserId()).thenReturn(100L);
    when(permissionFacade.getAccessibleMenuIds(100L)).thenReturn(List.of(2L));
    MenuPermissionResolver resolver = new MenuPermissionResolver(permissionFacade, authContext);

    MenuItemEntity parent = new MenuItemEntity();
    parent.setId(1L);
    parent.setEnabled(true);
    parent.setParentId(null);

    MenuItemEntity child = new MenuItemEntity();
    child.setId(2L);
    child.setEnabled(true);
    child.setParentId(1L);

    List<MenuItemEntity> result = resolver.filterAccessible(List.of(parent, child));
    assertThat(result).extracting(MenuItemEntity::getId).containsExactlyInAnyOrder(1L, 2L);
  }
}
