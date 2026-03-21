package elexvx.admin.service.menu.query;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.mapper.MenuItemMapper;
import elexvx.admin.service.menu.support.MenuTreeBuilder;
import elexvx.admin.vo.MenuItemTreeNode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuQueryService {
  private final MenuItemMapper menuItemMapper;
  private final MenuTreeBuilder menuTreeBuilder;

  public MenuQueryService(MenuItemMapper menuItemMapper, MenuTreeBuilder menuTreeBuilder) {
    this.menuItemMapper = menuItemMapper;
    this.menuTreeBuilder = menuTreeBuilder;
  }

  public boolean isConfigured() {
    return menuItemMapper.count() > 0;
  }

  public List<MenuItemTreeNode> getAdminTree() {
    List<MenuItemEntity> items = menuItemMapper.selectAll();
    return menuTreeBuilder.toAdminTree(items);
  }
}
