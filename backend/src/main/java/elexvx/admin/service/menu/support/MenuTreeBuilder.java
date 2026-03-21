package elexvx.admin.service.menu.support;

import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.vo.MenuItemTreeNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MenuTreeBuilder {
  public List<MenuItemTreeNode> toAdminTree(List<MenuItemEntity> items) {
    List<MenuItemEntity> sorted = new ArrayList<>(items);
    sortByParentAndOrder(sorted);
    return buildTree(sorted, true);
  }

  public List<MenuItemTreeNode> toStrictTree(List<MenuItemEntity> items) {
    List<MenuItemEntity> sorted = new ArrayList<>(items);
    sortByParentAndOrder(sorted);
    return buildTree(sorted, false);
  }

  public Map<Long, List<MenuItemEntity>> groupByParent(List<MenuItemEntity> all) {
    Map<Long, List<MenuItemEntity>> m = new HashMap<>();
    for (MenuItemEntity e : all) {
      Long pid = e.getParentId();
      if (pid == null) {
        continue;
      }
      m.computeIfAbsent(pid, k -> new ArrayList<>()).add(e);
    }
    return m;
  }

  public MenuItemTreeNode toNode(MenuItemEntity e) {
    MenuItemTreeNode n = new MenuItemTreeNode();
    n.setId(e.getId());
    n.setParentId(e.getParentId());
    n.setNodeType(e.getNodeType());
    n.setPath(e.getPath());
    n.setRouteName(e.getRouteName());
    n.setComponent(e.getComponent());
    n.setRedirect(e.getRedirect());
    n.setTitleZhCn(e.getTitleZhCn());
    n.setTitleEnUs(e.getTitleEnUs());
    n.setIcon(e.getIcon());
    n.setHidden(e.getHidden());
    n.setFrameSrc(e.getFrameSrc());
    n.setFrameBlank(e.getFrameBlank());
    n.setEnabled(e.getEnabled());
    n.setRequiredModules(e.getRequiredModules());
    n.setOrderNo(e.getOrderNo());
    n.setActions(e.getActions());
    n.setVersion(e.getVersion());
    return n;
  }

  private List<MenuItemTreeNode> buildTree(List<MenuItemEntity> items, boolean allowOrphansAsRoot) {
    Map<Long, MenuItemTreeNode> map = new HashMap<>();
    for (MenuItemEntity e : items) {
      map.put(e.getId(), toNode(e));
    }
    List<MenuItemTreeNode> roots = new ArrayList<>();
    for (MenuItemEntity e : items) {
      MenuItemTreeNode n = map.get(e.getId());
      if (e.getParentId() == null) {
        roots.add(n);
      } else {
        MenuItemTreeNode p = map.get(e.getParentId());
        if (p != null) {
          p.addChild(n);
        } else if (allowOrphansAsRoot) {
          roots.add(n);
        }
      }
    }
    return roots;
  }

  private void sortByParentAndOrder(List<MenuItemEntity> items) {
    items.sort(
      Comparator.comparing((MenuItemEntity e) -> e.getParentId() == null ? Long.MIN_VALUE : e.getParentId())
        .thenComparing(e -> e.getOrderNo() == null ? 0 : e.getOrderNo())
        .thenComparing(e -> e.getId() == null ? 0 : e.getId())
    );
  }
}
