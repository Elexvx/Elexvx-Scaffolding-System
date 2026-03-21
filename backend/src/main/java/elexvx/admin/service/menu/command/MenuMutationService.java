package elexvx.admin.service.menu.command;

import elexvx.admin.dao.AuthQueryDao;
import elexvx.admin.dto.MenuItemCreateRequest;
import elexvx.admin.dto.MenuItemReorderRequest;
import elexvx.admin.dto.MenuItemUpdateRequest;
import elexvx.admin.entity.MenuItemEntity;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.MenuItemMapper;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.menu.support.MenuSeedNode;
import elexvx.admin.service.menu.support.MenuTreeBuilder;
import elexvx.admin.vo.MenuItemTreeNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuMutationService {
  private final MenuItemMapper menuItemMapper;
  private final OperationLogService operationLogService;
  private final AuthQueryDao authDao;
  private final MenuTreeBuilder menuTreeBuilder;

  public MenuMutationService(
    MenuItemMapper menuItemMapper,
    OperationLogService operationLogService,
    AuthQueryDao authDao,
    MenuTreeBuilder menuTreeBuilder
  ) {
    this.menuItemMapper = menuItemMapper;
    this.operationLogService = operationLogService;
    this.authDao = authDao;
    this.menuTreeBuilder = menuTreeBuilder;
  }

  @Transactional
  public MenuItemTreeNode create(MenuItemCreateRequest req) {
    MenuItemEntity e = new MenuItemEntity();
    e.setParentId(req.getParentId());
    e.setNodeType(normalizeNodeType(req.getNodeType()));
    e.setPath(normalizePath(e.getParentId(), req.getPath()));
    e.setRouteName(req.getRouteName().trim());
    e.setComponent(blankToNull(req.getComponent()));
    e.setRedirect(blankToNull(req.getRedirect()));
    e.setTitleZhCn(req.getTitleZhCn().trim());
    e.setTitleEnUs(blankToNull(req.getTitleEnUs()));
    e.setIcon(blankToNull(req.getIcon()));
    e.setHidden(Boolean.TRUE.equals(req.getHidden()));
    e.setFrameSrc(blankToNull(req.getFrameSrc()));
    e.setFrameBlank(Boolean.TRUE.equals(req.getFrameBlank()));
    e.setEnabled(req.getEnabled() == null || Boolean.TRUE.equals(req.getEnabled()));
    e.setRequiredModules(blankToNull(req.getRequiredModules()));
    e.setActions(blankToNull(req.getActions()));
    Integer orderNo = req.getOrderNo();
    if (orderNo == null) {
      orderNo = nextOrderNo(req.getParentId());
    }
    e.setOrderNo(orderNo);
    validateUpsert(e, null);
    try {
      e = saveMenuItem(e);
      authDao.ensureAdminHasAllMenus();
    } catch (DataIntegrityViolationException ex) {
      throw badRequest("routeName 重复或数据不合法");
    }
    operationLogService.log("CREATE", "菜单管理", "创建菜单: " + describeMenu(e));
    return menuTreeBuilder.toNode(e);
  }

  @Transactional
  public MenuItemTreeNode update(long id, MenuItemUpdateRequest req) {
    MenuItemEntity e = Optional.ofNullable(menuItemMapper.selectById(id)).orElseThrow(() -> badRequest("节点不存在"));
    if (!Objects.equals(req.getVersion(), e.getVersion())) {
      throw badRequest("数据已变更，请刷新后重试");
    }
    if (req.isParentIdSet()) {
      e.setParentId(req.getParentId());
    }
    if (req.getNodeType() != null) {
      e.setNodeType(normalizeNodeType(req.getNodeType()));
    }
    if (req.getPath() != null) {
      e.setPath(normalizePath(e.getParentId(), req.getPath()));
    }
    if (req.getRouteName() != null) {
      e.setRouteName(req.getRouteName().trim());
    }
    if (req.getComponent() != null) {
      e.setComponent(blankToNull(req.getComponent()));
    }
    if (req.getRedirect() != null) {
      e.setRedirect(blankToNull(req.getRedirect()));
    }
    if (req.getTitleZhCn() != null) {
      e.setTitleZhCn(req.getTitleZhCn().trim());
    }
    if (req.getTitleEnUs() != null) {
      e.setTitleEnUs(blankToNull(req.getTitleEnUs()));
    }
    if (req.getIcon() != null) {
      e.setIcon(blankToNull(req.getIcon()));
    }
    if (req.getHidden() != null) {
      e.setHidden(Boolean.TRUE.equals(req.getHidden()));
    }
    if (req.getFrameSrc() != null) {
      e.setFrameSrc(blankToNull(req.getFrameSrc()));
    }
    if (req.getFrameBlank() != null) {
      e.setFrameBlank(Boolean.TRUE.equals(req.getFrameBlank()));
    }
    if (req.getEnabled() != null) {
      e.setEnabled(Boolean.TRUE.equals(req.getEnabled()));
    }
    if (req.getRequiredModules() != null) {
      e.setRequiredModules(blankToNull(req.getRequiredModules()));
    }
    if (req.getOrderNo() != null) {
      e.setOrderNo(req.getOrderNo());
    }
    if (req.getActions() != null) {
      e.setActions(blankToNull(req.getActions()));
    }
    validateUpsert(e, id);
    try {
      e = saveMenuItem(e);
    } catch (DataIntegrityViolationException ex) {
      throw badRequest("routeName 重复或数据不合法");
    }
    operationLogService.log("UPDATE", "菜单管理", "更新菜单: " + describeMenu(e));
    return menuTreeBuilder.toNode(e);
  }

  @Transactional
  public boolean delete(long id, boolean cascade) {
    MenuItemEntity target = menuItemMapper.selectById(id);
    if (target == null) {
      return true;
    }
    String routeName = target.getRouteName();
    if ("system".equalsIgnoreCase(routeName)
      || "SystemMenu".equalsIgnoreCase(routeName)
      || "SystemRole".equalsIgnoreCase(routeName)
      || "SystemUser".equalsIgnoreCase(routeName)) {
      throw badRequest("系统核心菜单不允许删除");
    }
    List<MenuItemEntity> all = menuItemMapper.selectAll();
    Map<Long, List<MenuItemEntity>> children = menuTreeBuilder.groupByParent(all);
    if (!cascade && children.containsKey(id) && !children.get(id).isEmpty()) {
      throw badRequest("目录下存在子节点，无法删除");
    }
    Set<Long> ids = collectCascadeIds(id, children);
    authDao.deleteRoleMenusByMenuIds(ids);
    menuItemMapper.deleteByIds(ids);
    String suffix = cascade ? "（含子节点）" : "";
    operationLogService.log("DELETE", "菜单管理", "删除菜单: " + describeMenu(target) + suffix);
    return true;
  }

  @Transactional
  public boolean reorder(MenuItemReorderRequest req) {
    List<MenuItemReorderRequest.Item> items = req.getItems();
    Set<Long> ids = new HashSet<>();
    for (MenuItemReorderRequest.Item it : items) {
      if (!ids.add(it.getId())) {
        throw badRequest("items 存在重复 id");
      }
      if (it.getParentId() != null && Objects.equals(it.getId(), it.getParentId())) {
        throw badRequest("parentId 不能指向自己");
      }
    }

    List<MenuItemEntity> all = menuItemMapper.selectAll();
    Map<Long, MenuItemEntity> allMap = new HashMap<>();
    Map<Long, Long> parentMap = new HashMap<>();
    for (MenuItemEntity e : all) {
      allMap.put(e.getId(), e);
      parentMap.put(e.getId(), e.getParentId());
    }

    List<MenuItemEntity> targets = new ArrayList<>();
    for (Long id : ids) {
      MenuItemEntity e = allMap.get(id);
      if (e == null) {
        throw badRequest("存在无效节点 id");
      }
      targets.add(e);
    }

    for (MenuItemReorderRequest.Item it : items) {
      MenuItemEntity e = allMap.get(it.getId());
      if (it.getVersion() != null && !Objects.equals(it.getVersion(), e.getVersion())) {
        throw badRequest("数据已变更，请刷新后重试");
      }
      parentMap.put(it.getId(), it.getParentId());
    }

    for (MenuItemReorderRequest.Item it : items) {
      if (it.getParentId() == null) {
        continue;
      }
      MenuItemEntity parent = allMap.get(it.getParentId());
      if (parent == null) {
        throw badRequest("parentId 无效: " + it.getParentId());
      }
      MenuItemEntity me = allMap.get(it.getId());
      String pNt = normalizeNodeType(parent.getNodeType());
      String myNt = normalizeNodeType(me.getNodeType());
      if ("PAGE".equals(pNt)) {
        if (!"BTN".equals(myNt)) {
          throw badRequest("页面节点下仅允许添加按钮");
        }
      } else if (!"DIR".equals(pNt)) {
        throw badRequest("父节点必须为目录或页面");
      }
    }

    ensureNoCycles(parentMap);
    for (Map.Entry<Long, Long> entry : parentMap.entrySet()) {
      Long curId = entry.getKey();
      Long parentId = entry.getValue();
      if (parentId == null) {
        continue;
      }
      MenuItemEntity parent = allMap.get(parentId);
      if (parent == null) {
        continue;
      }
      if ("PAGE".equalsIgnoreCase(parent.getNodeType())) {
        MenuItemEntity child = allMap.get(curId);
        if (child != null && !"BTN".equalsIgnoreCase(normalizeNodeType(child.getNodeType()))) {
          throw badRequest("页面节点仅能包含按钮作为子节点");
        }
      } else if ("BTN".equalsIgnoreCase(parent.getNodeType())) {
        throw badRequest("按钮节点不能包含子节点");
      }
    }

    for (MenuItemReorderRequest.Item it : items) {
      MenuItemEntity e = allMap.get(it.getId());
      Long newParentId = it.getParentId();
      e.setParentId(newParentId);
      e.setOrderNo(it.getOrderNo());
      String path = e.getPath();
      if (path == null) {
        continue;
      }
      String p = path.trim();
      if (newParentId == null) {
        while (p.startsWith("//")) {
          p = p.substring(1);
        }
        if (!p.startsWith("/")) {
          p = "/" + p;
        }
        e.setPath(p);
      } else if (p.startsWith("/")) {
        e.setPath(normalizePath(newParentId, p));
      }
    }

    saveAllMenuItems(targets);
    operationLogService.log("UPDATE", "菜单管理", "调整菜单顺序");
    return true;
  }

  @Transactional
  public MenuItemEntity upsertSeed(MenuSeedNode seed, boolean overwriteExisting) {
    Optional<MenuItemEntity> existing = Optional.ofNullable(menuItemMapper.selectByRouteName(seed.routeName()));
    if (existing.isPresent() && !overwriteExisting) {
      return existing.get();
    }
    MenuItemEntity e = existing.orElseGet(MenuItemEntity::new);
    e.setParentId(seed.parentId());
    e.setNodeType(seed.nodeType());
    e.setPath(seed.path());
    e.setRouteName(seed.routeName());
    e.setComponent(seed.component());
    e.setRedirect(seed.redirect());
    e.setTitleZhCn(seed.titleZhCn());
    e.setTitleEnUs(seed.titleEnUs());
    e.setIcon(seed.icon());
    e.setHidden(seed.hidden());
    e.setFrameSrc(seed.frameSrc());
    e.setFrameBlank(seed.frameBlank());
    e.setEnabled(seed.enabled());
    e.setOrderNo(seed.orderNo());
    e.setActions(seed.actions());
    return saveSeedMenuItem(e);
  }

  public void ensureAdminOwnsAllMenus() {
    authDao.ensureAdminHasAllMenus();
  }

  private Set<Long> collectCascadeIds(Long rootId, Map<Long, List<MenuItemEntity>> children) {
    Set<Long> ids = new HashSet<>();
    ArrayDeque<Long> q = new ArrayDeque<>();
    q.add(rootId);
    while (!q.isEmpty()) {
      Long cur = q.removeFirst();
      if (!ids.add(cur)) {
        continue;
      }
      for (MenuItemEntity c : children.getOrDefault(cur, List.of())) {
        q.addLast(c.getId());
      }
    }
    return ids;
  }

  private void ensureNoCycles(Map<Long, Long> parentMap) {
    Set<Long> visiting = new HashSet<>();
    Set<Long> visited = new HashSet<>();
    for (Long id : parentMap.keySet()) {
      if (visited.contains(id)) {
        continue;
      }
      Long cur = id;
      visiting.clear();
      while (cur != null) {
        if (!parentMap.containsKey(cur)) {
          break;
        }
        if (!visiting.add(cur)) {
          throw badRequest("检测到循环引用");
        }
        cur = parentMap.get(cur);
      }
      visited.addAll(visiting);
    }
  }

  private Integer nextOrderNo(Long parentId) {
    List<MenuItemEntity> all = menuItemMapper.selectAll();
    int max = -1;
    for (MenuItemEntity e : all) {
      if (Objects.equals(parentId, e.getParentId())) {
        max = Math.max(max, e.getOrderNo() == null ? 0 : e.getOrderNo());
      }
    }
    return max + 1;
  }

  private void validateUpsert(MenuItemEntity e, Long selfId) {
    if (e.getNodeType() == null || e.getNodeType().isBlank()) {
      throw badRequest("nodeType 不能为空");
    }
    String nt = normalizeNodeType(e.getNodeType());
    e.setNodeType(nt);
    if (e.getRouteName() == null || e.getRouteName().isBlank()) {
      throw badRequest("routeName 不能为空");
    }
    if (e.getPath() == null || e.getPath().isBlank()) {
      throw badRequest("path 不能为空");
    }
    if (e.getTitleZhCn() == null || e.getTitleZhCn().isBlank()) {
      throw badRequest("titleZhCn 不能为空");
    }
    Optional<MenuItemEntity> existing = Optional.ofNullable(menuItemMapper.selectByRouteName(e.getRouteName()));
    if (existing.isPresent()) {
      Long existingId = existing.get().getId();
      if (selfId == null || !existingId.equals(selfId)) {
        throw badRequest("路由Name(" + e.getRouteName() + ")已存在，请修改为其他名称");
      }
    }
    if ("PAGE".equalsIgnoreCase(nt) && (e.getComponent() == null || e.getComponent().isBlank())) {
      throw badRequest("页面 component 不能为空");
    }
    if (e.getParentId() == null) {
      if (!e.getPath().startsWith("/")) {
        throw badRequest("根节点 path 必须以 / 开头");
      }
      if ("DIR".equalsIgnoreCase(nt) && (e.getComponent() == null || e.getComponent().isBlank())) {
        e.setComponent("LAYOUT");
      }
    } else {
      if (e.getPath().startsWith("/")) {
        throw badRequest("非根节点 path 不能以 / 开头");
      }
      MenuItemEntity parent = Optional.ofNullable(menuItemMapper.selectById(e.getParentId())).orElseThrow(() -> badRequest("parentId 无效"));
      String pNt = normalizeNodeType(parent.getNodeType());
      if ("PAGE".equals(pNt)) {
        if (!"BTN".equalsIgnoreCase(nt)) {
          throw badRequest("页面节点下仅允许添加按钮");
        }
      } else if (!"DIR".equals(pNt)) {
        throw badRequest("父节点必须为目录或页面");
      }
    }
    if ("BTN".equalsIgnoreCase(nt)) {
      Long id = selfId == null ? e.getId() : selfId;
      if (id != null) {
        for (MenuItemEntity c : menuItemMapper.selectAll()) {
          if (Objects.equals(c.getParentId(), id)) {
            throw badRequest("按钮不能包含子节点");
          }
        }
      }
    } else if ("PAGE".equalsIgnoreCase(nt)) {
      Long id = selfId == null ? e.getId() : selfId;
      if (id != null) {
        for (MenuItemEntity c : menuItemMapper.selectAll()) {
          if (Objects.equals(c.getParentId(), id) && !"BTN".equalsIgnoreCase(normalizeNodeType(c.getNodeType()))) {
            throw badRequest("页面节点仅能包含按钮作为子节点");
          }
        }
      }
    }
    if (selfId != null) {
      Map<Long, Long> parentMap = new HashMap<>();
      for (MenuItemEntity item : menuItemMapper.selectAll()) {
        parentMap.put(item.getId(), item.getParentId());
      }
      parentMap.put(selfId, e.getParentId());
      ensureNoCycles(parentMap);
    }
  }

  private String normalizeNodeType(String nodeType) {
    String nt = nodeType.trim().toUpperCase(Locale.ROOT);
    if (!"DIR".equals(nt) && !"PAGE".equals(nt) && !"BTN".equals(nt) && !"BUTTON".equals(nt)) {
      throw badRequest("nodeType 仅支持 DIR/PAGE/BTN");
    }
    if ("BUTTON".equals(nt)) {
      return "BTN";
    }
    return nt;
  }

  private String normalizePath(Long parentId, String path) {
    String p = path.trim();
    if (p.isEmpty()) {
      return p;
    }
    if (parentId != null && p.startsWith("/")) {
      int idx = p.lastIndexOf('/');
      if (idx >= 0 && idx < p.length() - 1) {
        return p.substring(idx + 1);
      }
    }
    return p;
  }

  private String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String t = value.trim();
    return t.isBlank() ? null : t;
  }

  private String describeMenu(MenuItemEntity e) {
    String title = e.getTitleZhCn();
    if (title == null || title.isBlank()) {
      title = e.getRouteName();
    }
    if (title == null || title.isBlank()) {
      title = "菜单";
    }
    if (e.getRouteName() != null && !e.getRouteName().isBlank() && !title.contains(e.getRouteName())) {
      return title + " (" + e.getRouteName() + ")";
    }
    return title;
  }

  private MenuItemEntity saveSeedMenuItem(MenuItemEntity menuItem) {
    try {
      return saveMenuItem(menuItem);
    } catch (DataIntegrityViolationException ex) {
      MenuItemEntity existing = menuItemMapper.selectByRouteName(menuItem.getRouteName());
      if (existing != null) {
        return existing;
      }
      throw ex;
    }
  }

  private MenuItemEntity saveMenuItem(MenuItemEntity menuItem) {
    if (menuItem.getId() == null) {
      if (menuItem.getVersion() == null) {
        menuItem.setVersion(0);
      }
      menuItemMapper.insert(menuItem);
      return menuItem;
    }
    Integer currentVersion = menuItem.getVersion();
    int updated = menuItemMapper.update(menuItem);
    if (updated == 0) {
      throw badRequest("数据已变更，请刷新后重试");
    }
    if (currentVersion != null) {
      menuItem.setVersion(currentVersion + 1);
    }
    return menuItem;
  }

  private void saveAllMenuItems(List<MenuItemEntity> items) {
    for (MenuItemEntity item : items) {
      saveMenuItem(item);
    }
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
}
