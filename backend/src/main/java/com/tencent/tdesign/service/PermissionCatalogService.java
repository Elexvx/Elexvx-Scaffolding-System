package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.MenuItemEntity;
import com.tencent.tdesign.mapper.MenuItemMapper;
import com.tencent.tdesign.vo.PermissionCatalogItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 权限目录服务。
 *
 * <p>从菜单配置自动生成页面动作权限目录，供角色管理页面勾选使用。
 */
@Service
public class PermissionCatalogService {
  private static final List<String> DEFAULT_PAGE_ACTIONS = List.of("query");
  private final MenuItemMapper menuItemMapper;
  private final PermissionCodeService permissionCodeService;

  public PermissionCatalogService(MenuItemMapper menuItemMapper, PermissionCodeService permissionCodeService) {
    this.menuItemMapper = menuItemMapper;
    this.permissionCodeService = permissionCodeService;
  }

  public List<PermissionCatalogItem> listCatalog() {
    List<MenuItemEntity> menus = menuItemMapper.selectAllEnabled();
    List<PermissionCatalogItem> out = new ArrayList<>();
    for (MenuItemEntity menu : menus) {
      if (menu == null) continue;
      if (!"PAGE".equalsIgnoreCase(String.valueOf(menu.getNodeType()))) continue;
      String routeName = normalize(menu.getRouteName());
      if (routeName == null) continue;

      List<String> actions = permissionCodeService.parseActions(menu.getActions());
      if (actions.isEmpty()) {
        actions = DEFAULT_PAGE_ACTIONS;
      }
      Map<String, String> permissionCodes = new LinkedHashMap<>();
      for (String action : actions) {
        String code = permissionCodeService.buildPermissionCode(routeName, action);
        if (code != null) {
          permissionCodes.put(action, code);
        }
      }
      if (permissionCodes.isEmpty()) continue;

      PermissionCatalogItem item = new PermissionCatalogItem();
      item.setMenuId(menu.getId());
      item.setRouteName(routeName);
      item.setTitle(resolveTitle(menu));
      item.setActions(List.copyOf(permissionCodes.keySet()));
      item.setPermissionCodes(permissionCodes);
      out.add(item);
    }
    return out;
  }

  private String resolveTitle(MenuItemEntity menu) {
    String title = normalize(menu.getTitleZhCn());
    if (title != null) return title;
    title = normalize(menu.getTitleEnUs());
    if (title != null) return title;
    return Objects.toString(menu.getRouteName(), "");
  }

  private String normalize(String value) {
    if (value == null) return null;
    String text = value.trim();
    return text.isEmpty() ? null : text;
  }
}
