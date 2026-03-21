package elexvx.admin.service.menu.support;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.vo.MenuItemTreeNode;
import elexvx.admin.vo.RouteItem;
import elexvx.admin.vo.RouteMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MenuRouteAssembler {
  private static final Pattern PLACEHOLDER_TITLE_PATTERN = Pattern.compile("^[\\s?？._\\-*/\\\\|]+$");
  private static final Map<String, String> DEFAULT_ZH_TITLE_BY_ROUTE = buildDefaultTitleMap(true);
  private static final Map<String, String> DEFAULT_EN_TITLE_BY_ROUTE = buildDefaultTitleMap(false);

  private final MenuTreeBuilder treeBuilder;

  public MenuRouteAssembler(MenuTreeBuilder treeBuilder) {
    this.treeBuilder = treeBuilder;
  }

  public List<RouteItem> toRouteTree(List<MenuItemEntity> items) {
    List<MenuItemEntity> routable = new ArrayList<>();
    for (MenuItemEntity e : items) {
      if (!"BTN".equalsIgnoreCase(e.getNodeType())) {
        routable.add(e);
      }
    }
    List<MenuItemTreeNode> tree = treeBuilder.toStrictTree(routable);
    List<RouteItem> out = new ArrayList<>();
    for (MenuItemTreeNode n : tree) {
      out.add(toRoute(n));
    }
    return out;
  }

  public List<RouteItem> pruneEmptyLayoutRoutes(List<RouteItem> routes) {
    if (routes == null || routes.isEmpty()) {
      return routes;
    }
    List<RouteItem> out = new ArrayList<>();
    for (RouteItem r : routes) {
      List<RouteItem> children = r.getChildren();
      if (children != null && !children.isEmpty()) {
        r.setChildren(pruneEmptyLayoutRoutes(children));
      }
      boolean isLayout = r.getComponent() != null && "LAYOUT".equalsIgnoreCase(r.getComponent().trim());
      boolean hasChildren = r.getChildren() != null && !r.getChildren().isEmpty();
      if (isLayout && !hasChildren) {
        continue;
      }
      out.add(r);
    }
    return out;
  }

  private RouteItem toRoute(MenuItemTreeNode n) {
    RouteMeta meta = new RouteMeta();
    Map<String, String> title = new HashMap<>();
    String zhTitle = resolveRouteTitle(n.getTitleZhCn(), n.getRouteName(), true);
    String enTitle = resolveRouteTitle(n.getTitleEnUs(), n.getRouteName(), false);
    title.put("zh_CN", zhTitle);
    title.put("en_US", isPlaceholderTitle(enTitle) ? zhTitle : enTitle);
    meta.setTitle(title);
    meta.setIcon(n.getIcon());
    meta.setHidden(Boolean.TRUE.equals(n.getHidden()));
    meta.setOrderNo(n.getOrderNo());
    meta.setFrameSrc(n.getFrameSrc());
    meta.setFrameBlank(n.getFrameBlank());
    List<String> actions = parseActions(n.getActions());
    meta.setActions(actions);
    String resource = resolveResource(n);
    meta.setResource(resource);
    boolean isDir = "DIR".equalsIgnoreCase(String.valueOf(n.getNodeType()));
    if (resource != null && !isDir) {
      if (actions.isEmpty()) {
        meta.setRequiredPermissions(List.of(buildPermission(resource, "query")));
      } else {
        List<String> perms = new ArrayList<>();
        for (String act : actions) {
          perms.add(buildPermission(resource, act));
        }
        meta.setRequiredPermissions(perms);
      }
    }

    RouteItem ri = new RouteItem();
    ri.setPath(n.getPath());
    ri.setName(n.getRouteName());
    ri.setComponent(n.getComponent());
    ri.setRedirect(n.getRedirect());
    ri.setMeta(meta);
    if (n.getChildren() != null && !n.getChildren().isEmpty()) {
      List<RouteItem> children = new ArrayList<>();
      for (MenuItemTreeNode c : n.getChildren()) {
        children.add(toRoute(c));
      }
      ri.setChildren(children);
    }
    return ri;
  }

  private List<String> parseActions(String actions) {
    if (actions == null || actions.isBlank()) {
      return List.of();
    }
    String[] parts = actions.split(",");
    List<String> list = new ArrayList<>();
    for (String p : parts) {
      String v = p.trim();
      if (!v.isEmpty()) {
        list.add(v);
      }
    }
    return list;
  }

  private String resolveResource(MenuItemTreeNode n) {
    if (n.getRouteName() != null && !n.getRouteName().isBlank()) {
      return n.getRouteName();
    }
    if (n.getPath() == null) {
      return null;
    }
    String[] segs = n.getPath().split("/");
    for (int i = segs.length - 1; i >= 0; i--) {
      if (segs[i] != null && !segs[i].isBlank()) {
        return segs[i];
      }
    }
    return null;
  }

  private String buildPermission(String resource, String action) {
    String res = resource == null ? "" : resource.trim();
    String act = action == null ? "" : action.trim();
    if (res.isEmpty()) {
      return act;
    }
    if (act.isEmpty()) {
      return res;
    }
    return "system:" + res + ":" + act;
  }

  private static Map<String, String> buildDefaultTitleMap(boolean zh) {
    Map<String, String> map = new HashMap<>();
    for (MenuSeedNode seed : MenuSeedCatalog.defaults()) {
      if (seed.routeName() == null || seed.routeName().isBlank()) {
        continue;
      }
      String title = zh ? seed.titleZhCn() : seed.titleEnUs();
      if (title == null || title.isBlank()) {
        continue;
      }
      map.putIfAbsent(seed.routeName(), title.trim());
    }
    return Map.copyOf(map);
  }

  private String resolveRouteTitle(String title, String routeName, boolean zh) {
    if (!isPlaceholderTitle(title)) {
      return title.trim();
    }
    String key = routeName == null ? "" : routeName.trim();
    if (!key.isBlank()) {
      String fallback = (zh ? DEFAULT_ZH_TITLE_BY_ROUTE : DEFAULT_EN_TITLE_BY_ROUTE).get(key);
      if (!isPlaceholderTitle(fallback)) {
        return fallback.trim();
      }
      if (!zh) {
        String zhFallback = DEFAULT_ZH_TITLE_BY_ROUTE.get(key);
        if (!isPlaceholderTitle(zhFallback)) {
          return zhFallback.trim();
        }
      }
    }
    if (!isPlaceholderTitle(routeName)) {
      return routeName.trim();
    }
    return "菜单";
  }

  private boolean isPlaceholderTitle(String title) {
    if (title == null) {
      return true;
    }
    String normalized = title.trim();
    if (normalized.isEmpty()) {
      return true;
    }
    return PLACEHOLDER_TITLE_PATTERN.matcher(normalized).matches();
  }
}
