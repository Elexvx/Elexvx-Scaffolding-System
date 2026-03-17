package com.tencent.tdesign.service;

import com.tencent.tdesign.dao.AuthQueryDao;
import com.tencent.tdesign.dto.MenuItemCreateRequest;
import com.tencent.tdesign.dto.MenuItemReorderRequest;
import com.tencent.tdesign.dto.MenuItemUpdateRequest;
import com.tencent.tdesign.entity.MenuItemEntity;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.mapper.MenuItemMapper;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.vo.MenuItemTreeNode;
import com.tencent.tdesign.vo.RouteItem;
import com.tencent.tdesign.vo.RouteMeta;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class MenuItemService {
  private static final Pattern PLACEHOLDER_TITLE_PATTERN = Pattern.compile("^[\\s?？._\\-*/\\\\|]+$");
  private static final Map<String, String> DEFAULT_ZH_TITLE_BY_ROUTE = buildDefaultTitleMap(true);
  private static final Map<String, String> DEFAULT_EN_TITLE_BY_ROUTE = buildDefaultTitleMap(false);

  private final MenuItemMapper menuItemMapper;
  private final OperationLogService operationLogService;
  private final AuthQueryDao authDao;
  private final PermissionFacade permissionFacade;
  private final AuthContext authContext;
  private final ModuleRegistryService moduleRegistryService;

  public MenuItemService(
    MenuItemMapper menuItemMapper,
    OperationLogService operationLogService,
    AuthQueryDao authDao,
    PermissionFacade permissionFacade,
    AuthContext authContext,
    ModuleRegistryService moduleRegistryService
  ) {
    this.menuItemMapper = menuItemMapper;
    this.operationLogService = operationLogService;
    this.authDao = authDao;
    this.permissionFacade = permissionFacade;
    this.authContext = authContext;
    this.moduleRegistryService = moduleRegistryService;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public boolean isConfigured() {
    return menuItemMapper.count() > 0;
  }

  public List<RouteItem> getMenuRoutesForCurrentUser() {
    List<MenuItemEntity> items = menuItemMapper.selectAllEnabled();
    List<MenuItemEntity> accessible = filterAccessible(items);
    return pruneEmptyLayoutRoutes(toRouteTree(accessible));
  }

  public List<MenuItemTreeNode> getAdminTree() {
    List<MenuItemEntity> items = menuItemMapper.selectAll();
    return toAdminTree(items);
  }

  @Transactional
  public int seedDefaultSidebarMenus(boolean overwriteExisting) {
    List<SeedNode> seeds = SeedNode.defaults();
    Map<String, MenuItemEntity> created = new HashMap<>();

    // 先创建/更新根节点（无 parentRouteName）
    for (SeedNode s : seeds) {
      if (s.parentRouteName != null) continue;
      MenuItemEntity e = upsertSeed(s, overwriteExisting);
      created.put(s.routeName, e);
    }

    // 再处理子节点（依赖 parentRouteName）
    for (SeedNode s : seeds) {
      if (s.parentRouteName == null) continue;
      MenuItemEntity parent = created.get(s.parentRouteName);
      if (parent == null) {
        Optional<MenuItemEntity> p = Optional.ofNullable(menuItemMapper.selectByRouteName(s.parentRouteName));
        if (p.isEmpty()) continue;
        parent = p.get();
        created.put(s.parentRouteName, parent);
      }
      MenuItemEntity e = upsertSeed(s.withParentId(parent.getId()), overwriteExisting);
      created.put(s.routeName, e);
    }
    int total = created.size();
    authDao.ensureAdminHasAllMenus();
    operationLogService.log("UPDATE", "菜单管理", "初始化默认菜单: " + total + "项");
    return total;
  }

  private MenuItemEntity upsertSeed(SeedNode s, boolean overwriteExisting) {
    Optional<MenuItemEntity> existing = Optional.ofNullable(menuItemMapper.selectByRouteName(s.routeName));
    if (existing.isPresent() && !overwriteExisting) {
      return existing.get();
    }

    MenuItemEntity e = existing.orElseGet(MenuItemEntity::new);
    e.setParentId(s.parentId);
    e.setNodeType(s.nodeType);
    e.setPath(s.path);
    e.setRouteName(s.routeName);
    e.setComponent(s.component);
    e.setRedirect(s.redirect);
    e.setTitleZhCn(s.titleZhCn);
    e.setTitleEnUs(s.titleEnUs);
    e.setIcon(s.icon);
    e.setHidden(s.hidden);
    e.setFrameSrc(s.frameSrc);
    e.setFrameBlank(s.frameBlank);
    e.setEnabled(s.enabled);
    e.setOrderNo(s.orderNo);
    e.setActions(s.actions);
    return saveSeedMenuItem(e);
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
    e.setEnabled(req.getEnabled() == null ? true : Boolean.TRUE.equals(req.getEnabled()));
    e.setRequiredModules(blankToNull(req.getRequiredModules()));
    e.setActions(blankToNull(req.getActions()));

    Integer orderNo = req.getOrderNo();
    if (orderNo == null) orderNo = nextOrderNo(req.getParentId());
    e.setOrderNo(orderNo);

    validateUpsert(e, null);
    try {
      e = saveMenuItem(e);
      // 确保 admin 拥有新菜单权限
      authDao.ensureAdminHasAllMenus();
    } catch (DataIntegrityViolationException ex) {
      throw badRequest("routeName 重复或数据不合法");
    }
    operationLogService.log("CREATE", "菜单管理", "创建菜单: " + describeMenu(e));
    return toNode(e);
  }

  @Transactional
  public MenuItemTreeNode update(long id, MenuItemUpdateRequest req) {
    MenuItemEntity e = Optional.ofNullable(menuItemMapper.selectById(id)).orElseThrow(() -> badRequest("节点不存在"));
    if (!Objects.equals(req.getVersion(), e.getVersion())) {
      throw badRequest("数据已变更，请刷新后重试");
    }

    if (req.isParentIdSet()) e.setParentId(req.getParentId());
    if (req.getNodeType() != null) e.setNodeType(normalizeNodeType(req.getNodeType()));
    if (req.getPath() != null) e.setPath(normalizePath(e.getParentId(), req.getPath()));
    if (req.getRouteName() != null) e.setRouteName(req.getRouteName().trim());
    if (req.getComponent() != null) e.setComponent(blankToNull(req.getComponent()));
    if (req.getRedirect() != null) e.setRedirect(blankToNull(req.getRedirect()));
    if (req.getTitleZhCn() != null) e.setTitleZhCn(req.getTitleZhCn().trim());
    if (req.getTitleEnUs() != null) e.setTitleEnUs(blankToNull(req.getTitleEnUs()));
    if (req.getIcon() != null) e.setIcon(blankToNull(req.getIcon()));
    if (req.getHidden() != null) e.setHidden(Boolean.TRUE.equals(req.getHidden()));
    if (req.getFrameSrc() != null) e.setFrameSrc(blankToNull(req.getFrameSrc()));
    if (req.getFrameBlank() != null) e.setFrameBlank(Boolean.TRUE.equals(req.getFrameBlank()));
    if (req.getEnabled() != null) e.setEnabled(Boolean.TRUE.equals(req.getEnabled()));
    if (req.getRequiredModules() != null) e.setRequiredModules(blankToNull(req.getRequiredModules()));
    if (req.getOrderNo() != null) e.setOrderNo(req.getOrderNo());
    if (req.getActions() != null) e.setActions(blankToNull(req.getActions()));

    validateUpsert(e, id);
    try {
      e = saveMenuItem(e);
    } catch (DataIntegrityViolationException ex) {
      throw badRequest("routeName 重复或数据不合法");
    }
    operationLogService.log("UPDATE", "菜单管理", "更新菜单: " + describeMenu(e));
    return toNode(e);
  }

  @Transactional
  public boolean delete(long id, boolean cascade) {
    MenuItemEntity target = menuItemMapper.selectById(id);
    if (target == null) return true;

    // 保护系统关键菜单，防止误删导致无法管理系统
    String rn = target.getRouteName();
    if ("system".equalsIgnoreCase(rn) || "SystemMenu".equalsIgnoreCase(rn) || "SystemRole".equalsIgnoreCase(rn) || "SystemUser".equalsIgnoreCase(rn)) {
      throw badRequest("系统核心菜单不允许删除");
    }

    List<MenuItemEntity> all = menuItemMapper.selectAll();
    Map<Long, List<MenuItemEntity>> children = groupByParent(all);
    if (!cascade && children.containsKey(id) && !children.get(id).isEmpty()) {
      throw badRequest("目录下存在子节点，无法删除");
    }
    Set<Long> ids = new HashSet<>();
    ArrayDeque<Long> q = new ArrayDeque<>();
    q.add(id);
    while (!q.isEmpty()) {
      Long cur = q.removeFirst();
      if (!ids.add(cur)) continue;
      for (MenuItemEntity c : children.getOrDefault(cur, List.of())) {
        q.addLast(c.getId());
      }
    }

    // 先清理角色关联表，防止数据孤儿
    authDao.deleteRoleMenusByMenuIds(ids);

    menuItemMapper.deleteByIds(ids);
    String suffix = cascade ? "（含子节点）" : "";
    operationLogService.log("DELETE", "菜单管理", "删除菜单: " + describeMenu(target) + suffix);
    return true;
  }

  @Transactional
  public boolean removeObsoleteWatermarkRoute() {
    MenuItemEntity obsolete = menuItemMapper.selectByRouteName("SystemWatermark");
    if (obsolete == null) return false;
    return delete(obsolete.getId(), true);
  }


  @Transactional
  public boolean removeObsoleteNotificationRoute() {
    MenuItemEntity group = menuItemMapper.selectByRouteName("notification");
    if (group != null) return delete(group.getId(), true);
    MenuItemEntity page = menuItemMapper.selectByRouteName("NotificationTable");
    if (page != null) return delete(page.getId(), true);
    return false;
  }

  @Transactional
  public boolean removeObsoleteTeamRoute() {
    MenuItemEntity teamPage = menuItemMapper.selectByRouteName("SystemTeam");
    if (teamPage != null) return delete(teamPage.getId(), true);
    MenuItemEntity teamRoute = menuItemMapper.selectByRouteName("team");
    if (teamRoute != null) return delete(teamRoute.getId(), true);
    return false;
  }

  @Transactional
  public boolean ensureOrgManagementMenuSeeded() {
    if (menuItemMapper.count() == 0) return false;
    if (menuItemMapper.selectByRouteName("SystemOrg") != null) return false;
    seedDefaultSidebarMenus(false);
    return true;
  }


  @Transactional
  public boolean reorder(MenuItemReorderRequest req) {
    List<MenuItemReorderRequest.Item> items = req.getItems();
    Set<Long> ids = new HashSet<>();
    for (MenuItemReorderRequest.Item it : items) {
      if (!ids.add(it.getId())) throw badRequest("items 存在重复 id");
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
      if (e == null) throw badRequest("存在无效节点 id");
      targets.add(e);
    }

    for (MenuItemReorderRequest.Item it : items) {
      MenuItemEntity e = allMap.get(it.getId());
      if (it.getVersion() != null && !Objects.equals(it.getVersion(), e.getVersion())) {
        throw badRequest("数据已变更，请刷新后重试");
      }
      parentMap.put(it.getId(), it.getParentId());
    }

    // parent 校验（基于合并后的数据）
    for (MenuItemReorderRequest.Item it : items) {
      if (it.getParentId() == null) continue;
      MenuItemEntity parent = allMap.get(it.getParentId());
      if (parent == null) throw badRequest("parentId 无效: " + it.getParentId());

      MenuItemEntity me = allMap.get(it.getId());
      String pNt = normalizeNodeType(parent.getNodeType());
      String myNt = normalizeNodeType(me.getNodeType());

      if ("PAGE".equals(pNt)) {
        if (!"BTN".equals(myNt)) throw badRequest("页面节点下仅允许添加按钮");
      } else if (!"DIR".equals(pNt)) {
        throw badRequest("父节点必须为目录或页面");
      }
    }

    ensureNoCycles(parentMap);

    // PAGE 仅能包含 BTN 子节点
    for (Map.Entry<Long, Long> entry : parentMap.entrySet()) {
      Long id = entry.getKey();
      Long pid = entry.getValue();
      if (pid == null) continue;
      MenuItemEntity parent = allMap.get(pid);
      if (parent == null) continue;

      if ("PAGE".equalsIgnoreCase(parent.getNodeType())) {
        MenuItemEntity child = allMap.get(id);
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

      // 拖拽调整父子关系时，同步修正 path 的根/子节点格式，避免刷新后路由 404
      String path = e.getPath();
      if (path != null) {
        String p = path.trim();
        if (newParentId == null) {
          // 根节点 path 必须以 / 开头
          while (p.startsWith("//")) p = p.substring(1);
          if (!p.startsWith("/")) p = "/" + p;
          e.setPath(p);
        } else {
          // 非根节点 path 不能以 / 开头
          if (p.startsWith("/")) {
            e.setPath(normalizePath(newParentId, p));
          }
        }
      }
    }
    saveAllMenuItems(targets);
    operationLogService.log("UPDATE", "菜单管理", "调整菜单顺序");
    return true;
  }

  private void ensureNoCycles(Map<Long, Long> parentMap) {
    Set<Long> visiting = new HashSet<>();
    Set<Long> visited = new HashSet<>();
    for (Long id : parentMap.keySet()) {
      if (visited.contains(id)) continue;
      Long cur = id;
      visiting.clear();
      while (cur != null) {
        if (!parentMap.containsKey(cur)) break;
        if (!visiting.add(cur)) throw badRequest("检测到循环引用");
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
    if (e.getNodeType() == null || e.getNodeType().isBlank()) throw badRequest("nodeType 不能为空");
    String nt = normalizeNodeType(e.getNodeType());
    e.setNodeType(nt);

    if (e.getRouteName() == null || e.getRouteName().isBlank()) throw badRequest("routeName 不能为空");
    if (e.getPath() == null || e.getPath().isBlank()) throw badRequest("path 不能为空");
    if (e.getTitleZhCn() == null || e.getTitleZhCn().isBlank()) throw badRequest("titleZhCn 不能为空");

    // 检查 routeName 是否重复
    Optional<MenuItemEntity> existing = Optional.ofNullable(menuItemMapper.selectByRouteName(e.getRouteName()));
    if (existing.isPresent()) {
      Long existingId = existing.get().getId();
      // 如果是更新操作，允许保持自己原来的 routeName
      if (selfId == null || !existingId.equals(selfId)) {
        throw badRequest("路由Name(" + e.getRouteName() + ")已存在，请修改为其他名称");
      }
    }

    if ("PAGE".equalsIgnoreCase(nt)) {
      if (e.getComponent() == null || e.getComponent().isBlank()) throw badRequest("页面 component 不能为空");
    }

    if (e.getParentId() == null) {
      if (!e.getPath().startsWith("/")) throw badRequest("根节点 path 必须以 / 开头");
      if ("DIR".equalsIgnoreCase(nt) && (e.getComponent() == null || e.getComponent().isBlank())) {
        e.setComponent("LAYOUT");
      }
    } else {
      if (e.getPath().startsWith("/")) throw badRequest("非根节点 path 不能以 / 开头");
      MenuItemEntity parent = Optional.ofNullable(menuItemMapper.selectById(e.getParentId())).orElseThrow(() -> badRequest("parentId 无效"));
      
      String pNt = normalizeNodeType(parent.getNodeType());
      if ("PAGE".equals(pNt)) {
        if (!"BTN".equalsIgnoreCase(nt)) throw badRequest("页面节点下仅允许添加按钮");
      } else if (!"DIR".equals(pNt)) {
        throw badRequest("父节点必须为目录或页面");
      }
    }

    if ("BTN".equalsIgnoreCase(nt)) {
      Long id = (selfId == null) ? e.getId() : selfId;
      if (id != null) {
        for (MenuItemEntity c : menuItemMapper.selectAll()) {
          if (Objects.equals(c.getParentId(), id)) throw badRequest("按钮不能包含子节点");
        }
      }
    } else if ("PAGE".equalsIgnoreCase(nt)) {
      Long id = (selfId == null) ? e.getId() : selfId;
      if (id != null) {
        for (MenuItemEntity c : menuItemMapper.selectAll()) {
          if (Objects.equals(c.getParentId(), id)) {
            if (!"BTN".equalsIgnoreCase(normalizeNodeType(c.getNodeType()))) {
              throw badRequest("页面节点仅能包含按钮作为子节点");
            }
          }
        }
      }
    }

    // 防止移动/更新造成循环引用
    if (selfId != null) {
      Map<Long, Long> parentMap = new HashMap<>();
      for (MenuItemEntity it : menuItemMapper.selectAll()) parentMap.put(it.getId(), it.getParentId());
      parentMap.put(selfId, e.getParentId());
      ensureNoCycles(parentMap);
    }
  }

  private List<MenuItemEntity> filterAccessible(List<MenuItemEntity> items) {
    long userId = authContext.requireUserId();
    Set<Long> accessibleMenuIds = new HashSet<>(permissionFacade.getAccessibleMenuIds(userId));

    Map<Long, MenuItemEntity> idMap = new HashMap<>();
    for (MenuItemEntity e : items) {
      idMap.put(e.getId(), e);
    }

    Set<Long> visibleIds = new HashSet<>();
    for (MenuItemEntity e : items) {
      if (!Boolean.TRUE.equals(e.getEnabled())) continue;
      if (!isModulesAvailable(e.getRequiredModules())) continue;

      // 检查角色配置 (只校验是否分配给了该用户，以 role_menus 为准)
      if (accessibleMenuIds.contains(e.getId())) {
        visibleIds.add(e.getId());

        // 递归添加父节点，保证树结构完整
        Long pid = e.getParentId();
        while (pid != null) {
          if (visibleIds.contains(pid)) break;
          MenuItemEntity p = idMap.get(pid);
          if (p == null || !Boolean.TRUE.equals(p.getEnabled())) break;
          if (!isModulesAvailable(p.getRequiredModules())) break;
          visibleIds.add(pid);
          pid = p.getParentId();
        }
      }
    }

    List<MenuItemEntity> list = new ArrayList<>();
    for (MenuItemEntity e : items) {
      if (visibleIds.contains(e.getId())) {
        list.add(e);
      }
    }
    return list;
  }

  private List<RouteItem> pruneEmptyLayoutRoutes(List<RouteItem> routes) {
    if (routes == null || routes.isEmpty()) return routes;
    List<RouteItem> out = new ArrayList<>();
    for (RouteItem r : routes) {
      List<RouteItem> children = r.getChildren();
      if (children != null && !children.isEmpty()) {
        r.setChildren(pruneEmptyLayoutRoutes(children));
      }
      boolean isLayout = r.getComponent() != null && "LAYOUT".equalsIgnoreCase(r.getComponent().trim());
      boolean hasChildren = r.getChildren() != null && !r.getChildren().isEmpty();
      if (isLayout && !hasChildren) continue;
      out.add(r);
    }
    return out;
  }

  private List<RouteItem> toRouteTree(List<MenuItemEntity> items) {
    List<MenuItemEntity> routable = new ArrayList<>();
    for (MenuItemEntity e : items) {
      if (!"BTN".equalsIgnoreCase(e.getNodeType())) routable.add(e);
    }
    List<MenuItemTreeNode> tree = toStrictTree(routable);
    List<RouteItem> out = new ArrayList<>();
    for (MenuItemTreeNode n : tree) out.add(toRoute(n));
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
    meta.setRequiredModules(parseModules(n.getRequiredModules()));
    String resource = resolveResource(n);
    meta.setResource(resource);
    boolean isDir = "DIR".equalsIgnoreCase(String.valueOf(n.getNodeType()));
    if (resource != null && !isDir) {
      if (actions.isEmpty()) {
        meta.setRequiredPermissions(List.of(buildPermission(resource, "query")));
      } else {
        List<String> perms = new ArrayList<>();
        for (String act : actions) perms.add(buildPermission(resource, act));
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
      for (MenuItemTreeNode c : n.getChildren()) children.add(toRoute(c));
      ri.setChildren(children);
    }
    return ri;
  }

  private boolean isModulesAvailable(String requiredModules) {
    List<String> modules = parseModules(requiredModules);
    if (modules.isEmpty()) return true;
    for (String key : modules) {
      if (!moduleRegistryService.isModuleAvailable(key)) return false;
    }
    return true;
  }

  private List<String> parseModules(String requiredModules) {
    if (requiredModules == null || requiredModules.isBlank()) return List.of();
    String[] parts = requiredModules.split(",");
    List<String> out = new ArrayList<>();
    for (String p : parts) {
      String v = p == null ? "" : p.trim();
      if (!v.isEmpty()) out.add(v.toLowerCase(Locale.ROOT));
    }
    return out;
  }

  private List<String> parseActions(String actions) {
    if (actions == null || actions.isBlank()) return List.of();
    String[] parts = actions.split(",");
    List<String> list = new ArrayList<>();
    for (String p : parts) {
      String v = p.trim();
      if (!v.isEmpty()) list.add(v);
    }
    return list;
  }

  private String resolveResource(MenuItemTreeNode n) {
    if (n.getRouteName() != null && !n.getRouteName().isBlank()) {
      return n.getRouteName();
    }
    if (n.getPath() == null) return null;
    String[] segs = n.getPath().split("/");
    for (int i = segs.length - 1; i >= 0; i--) {
      if (segs[i] != null && !segs[i].isBlank()) return segs[i];
    }
    return null;
  }

  private String buildPermission(String resource, String action) {
    String res = resource == null ? "" : resource.trim();
    String act = action == null ? "" : action.trim();
    if (res.isEmpty()) return act;
    if (act.isEmpty()) return res;
    return "system:" + res + ":" + act;
  }

  private List<MenuItemTreeNode> toAdminTree(List<MenuItemEntity> items) {
    items.sort(
      Comparator.comparing((MenuItemEntity e) -> e.getParentId() == null ? Long.MIN_VALUE : e.getParentId())
        .thenComparing(e -> e.getOrderNo() == null ? 0 : e.getOrderNo())
        .thenComparing(e -> e.getId() == null ? 0 : e.getId())
    );

    Map<Long, MenuItemTreeNode> map = new HashMap<>();
    for (MenuItemEntity e : items) map.put(e.getId(), toNode(e));

    List<MenuItemTreeNode> roots = new ArrayList<>();
    for (MenuItemEntity e : items) {
      MenuItemTreeNode n = map.get(e.getId());
      if (e.getParentId() == null) {
        roots.add(n);
      } else {
        MenuItemTreeNode p = map.get(e.getParentId());
        if (p != null) p.addChild(n);
        else roots.add(n);
      }
    }
    return roots;
  }

  private List<MenuItemTreeNode> toStrictTree(List<MenuItemEntity> items) {
    items.sort(
      Comparator.comparing((MenuItemEntity e) -> e.getParentId() == null ? Long.MIN_VALUE : e.getParentId())
        .thenComparing(e -> e.getOrderNo() == null ? 0 : e.getOrderNo())
        .thenComparing(e -> e.getId() == null ? 0 : e.getId())
    );

    Map<Long, MenuItemTreeNode> map = new HashMap<>();
    for (MenuItemEntity e : items) map.put(e.getId(), toNode(e));

    List<MenuItemTreeNode> roots = new ArrayList<>();
    for (MenuItemEntity e : items) {
      MenuItemTreeNode n = map.get(e.getId());
      if (e.getParentId() == null) {
        roots.add(n);
      } else {
        MenuItemTreeNode p = map.get(e.getParentId());
        if (p != null) p.addChild(n);
      }
    }
    return roots;
  }

  private Map<Long, List<MenuItemEntity>> groupByParent(List<MenuItemEntity> all) {
    Map<Long, List<MenuItemEntity>> m = new HashMap<>();
    for (MenuItemEntity e : all) {
      Long pid = e.getParentId();
      if (pid == null) continue;
      m.computeIfAbsent(pid, k -> new ArrayList<>()).add(e);
    }
    return m;
  }

  private MenuItemTreeNode toNode(MenuItemEntity e) {
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

  private String normalizeNodeType(String nodeType) {
    String nt = nodeType.trim().toUpperCase(Locale.ROOT);
    if (!"DIR".equals(nt) && !"PAGE".equals(nt) && !"BTN".equals(nt) && !"BUTTON".equals(nt)) {
      throw badRequest("nodeType 仅支持 DIR/PAGE/BTN");
    }
    if ("BUTTON".equals(nt)) nt = "BTN";
    return nt;
  }

  private String normalizePath(Long parentId, String path) {
    String p = path.trim();
    if (p.isEmpty()) return p;
    if (parentId != null && p.startsWith("/")) {
      int idx = p.lastIndexOf('/');
      if (idx >= 0 && idx < p.length() - 1) p = p.substring(idx + 1);
    }
    return p;
  }

  private String blankToNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isBlank() ? null : t;
  }

  private String describeMenu(MenuItemEntity e) {
    String title = e.getTitleZhCn();
    if (title == null || title.isBlank()) title = e.getRouteName();
    if (title == null || title.isBlank()) title = "菜单";
    if (e.getRouteName() != null && !e.getRouteName().isBlank() && !title.contains(e.getRouteName())) {
      return title + " (" + e.getRouteName() + ")";
    }
    return title;
  }

  private static Map<String, String> buildDefaultTitleMap(boolean zh) {
    Map<String, String> map = new HashMap<>();
    for (SeedNode seed : SeedNode.defaults()) {
      if (seed == null || seed.routeName == null || seed.routeName.isBlank()) continue;
      String title = zh ? seed.titleZhCn : seed.titleEnUs;
      if (title == null || title.isBlank()) continue;
      map.putIfAbsent(seed.routeName, title.trim());
    }
    return Map.copyOf(map);
  }

  private String resolveRouteTitle(String title, String routeName, boolean zh) {
    if (!isPlaceholderTitle(title)) return title.trim();

    String key = routeName == null ? "" : routeName.trim();
    if (!key.isBlank()) {
      String fallback = (zh ? DEFAULT_ZH_TITLE_BY_ROUTE : DEFAULT_EN_TITLE_BY_ROUTE).get(key);
      if (!isPlaceholderTitle(fallback)) return fallback.trim();

      if (!zh) {
        String zhFallback = DEFAULT_ZH_TITLE_BY_ROUTE.get(key);
        if (!isPlaceholderTitle(zhFallback)) return zhFallback.trim();
      }
    }

    if (!isPlaceholderTitle(routeName)) return routeName.trim();
    return "菜单";
  }

  private boolean isPlaceholderTitle(String title) {
    if (title == null) return true;
    String normalized = title.trim();
    if (normalized.isEmpty()) return true;
    return PLACEHOLDER_TITLE_PATTERN.matcher(normalized).matches();
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
      if (menuItem.getVersion() == null) menuItem.setVersion(0);
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

  private static class SeedNode {
    private final String parentRouteName;
    private final Long parentId;
    private final String nodeType;
    private final String path;
    private final String routeName;
    private final String component;
    private final String redirect;
    private final String titleZhCn;
    private final String titleEnUs;
    private final String icon;
    private final boolean hidden;
    private final String frameSrc;
    private final boolean frameBlank;
    private final boolean enabled;
    private final int orderNo;
    private final String actions;

    private SeedNode(
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
      this.parentRouteName = parentRouteName;
      this.parentId = parentId;
      this.nodeType = nodeType;
      this.path = path;
      this.routeName = routeName;
      this.component = component;
      this.redirect = redirect;
      this.titleZhCn = titleZhCn;
      this.titleEnUs = titleEnUs;
      this.icon = icon;
      this.hidden = hidden;
      this.frameSrc = frameSrc;
      this.frameBlank = frameBlank;
      this.enabled = enabled;
      this.orderNo = orderNo;
      this.actions = actions;
    }

    SeedNode withParentId(Long parentId) {
      return new SeedNode(
        this.parentRouteName,
        parentId,
        this.nodeType,
        this.path,
        this.routeName,
        this.component,
        this.redirect,
        this.titleZhCn,
        this.titleEnUs,
        this.icon,
        this.hidden,
        this.frameSrc,
        this.frameBlank,
        this.enabled,
        this.orderNo,
        this.actions
      );
    }

    static List<SeedNode> defaults() {
      List<SeedNode> list = new ArrayList<>();
      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/user",
          "user",
          "LAYOUT",
          "/user/index",
          "个人中心",
          "User Center",
          "user-safety-filled",
          false,
          null,
          false,
          true,
          2,
          "query,create,update,delete"
        )
      );
      list.add(
        new SeedNode(
          "user",
          null,
          "PAGE",
          "index",
          "UserIndex",
          "/user/index",
          null,
          "个人中心",
          "User Center",
          "user",
          false,
          null,
          false,
          true,
          0,
          "query,create,update,delete"
        )
      );

      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/system",
          "system",
          "LAYOUT",
          null,
          "系统设置",
          "System",
          "setting",
          false,
          null,
          false,
          true,
          3,
          null
        )
      );
      list.add(
        new SeedNode(
          "users",
          null,
          "PAGE",
          "user",
          "SystemUser",
          "/system/user/index",
          null,
          "用户管理",
          "Users",
          "user",
          false,
          null,
          false,
          true,
          0,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "ai",
          "SystemAi",
          "/system/ai/index",
          null,
          "AI设置",
          "AI Settings",
          "chat",
          false,
          null,
          false,
          true,
          6,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "users",
          null,
          "PAGE",
          "role",
          "SystemRole",
          "/system/role/index",
          null,
          "角色管理",
          "Roles",
          "usergroup",
          false,
          null,
          false,
          true,
          1,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "users",
          null,
          "PAGE",
          "org",
          "SystemOrg",
          "/system/org/index",
          null,
          "机构管理",
          "Organization",
          "tree-round-dot-vertical",
          false,
          null,
          false,
          true,
          2,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "menu",
          "SystemMenu",
          "/system/menu/index",
          null,
          "目录/页面管理",
          "Menu Manager",
          "tree-round-dot-vertical",
          false,
          null,
          false,
          true,
          0,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "personalize",
          "SystemPersonalize",
          "/system/personalize/index",
          null,
          "个性化设置",
          "Personalize",
          "setting-1",
          false,
          null,
          false,
          true,
          2,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "users",
          null,
          "PAGE",
          "log",
          "SystemLog",
          "/system/log/index",
          null,
          "操作日志",
          "Operation Logs",
          "file",
          false,
          null,
          false,
          true,
          2,
          "create,update,delete,query"
        )
      );

      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/user-settings",
          "users",
          "LAYOUT",
          null,
          "用户设置",
          null,
          "usergroup",
          false,
          null,
          false,
          true,
          4,
          null
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "storage",
          "SystemStorage",
          "/system/storage/index",
          null,
          "对象存储",
          "Object Storage",
          "cloud-upload",
          false,
          null,
          false,
          true,
          3,
          "update,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "verification",
          "SystemVerification",
          "/system/verification/index",
          null,
          "验证设置",
          "Verification",
          "check-circle",
          false,
          null,
          false,
          true,
          4,
          "update,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "modules",
          "SystemModule",
          "/system/modules/index",
          null,
          "模块管理",
          "Modules",
          "grid-view",
          false,
          null,
          false,
          true,
          6,
          "update,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "security",
          "SystemSecurity",
          "/system/security/index",
          null,
          "安全设置",
          "Security",
          "lock-on",
          false,
          null,
          false,
          true,
          5,
          "update,query"
        )
      );
      list.add(
        new SeedNode(
          "system",
          null,
          "PAGE",
          "sensitive",
          "SystemSensitive",
          "/system/sensitive/index",
          null,
          "敏感词拦截",
          "Sensitive Words",
          "filter",
          false,
          null,
          false,
          true,
          5,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/monitor",
          "SystemMonitor",
          "LAYOUT",
          null,
          "系统监控",
          "Monitor",
          "chart-bar",
          false,
          null,
          false,
          true,
          1,
          "query,create,update,delete"
        )
      );
      list.add(
        new SeedNode(
          "SystemMonitor",
          null,
          "PAGE",
          "online-user",
          "SystemMonitorOnlineUser",
          "/system/monitor/online-user/index",
          null,
          "在线用户",
          "Online User",
          "usergroup-add",
          false,
          null,
          false,
          true,
          0,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "SystemMonitor",
          null,
          "PAGE",
          "server",
          "SystemMonitorServer",
          "/system/monitor/server/index",
          null,
          "服务监控",
          "Server Monitor",
          "server",
          false,
          null,
          false,
          true,
          1,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "SystemMonitor",
          null,
          "PAGE",
          "redis",
          "SystemMonitorRedis",
          "/system/monitor/redis/index",
          null,
          "Redis监控",
          "Redis Monitor",
          "chart-3d",
          false,
          null,
          false,
          true,
          2,
          "query,create,update,delete"
        )
      );
      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/example",
          "example",
          "LAYOUT",
          "/example/goods",
          "示例页面",
          "Examples",
          "file",
          false,
          null,
          false,
          true,
          0,
          null
        )
      );
      list.add(
        new SeedNode(
          "example",
          null,
          "PAGE",
          "goods",
          "ExampleGoods",
          "/example/goods/index",
          null,
          "商品管理",
          "Goods",
          null,
          false,
          null,
          false,
          true,
          0,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "example",
          null,
          "PAGE",
          "order",
          "ExampleOrder",
          "/example/order/index",
          null,
          "订单管理",
          "Orders",
          null,
          false,
          null,
          false,
          true,
          1,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/announcement",
          "announcement",
          "LAYOUT",
          "/announcement/table",
          "公告管理",
          "Announcements",
          "notification",
          false,
          null,
          false,
          true,
          5,
          null
        )
      );
      list.add(
        new SeedNode(
          "announcement",
          null,
          "PAGE",
          "table",
          "AnnouncementTable",
          "/announcement/table/index",
          null,
          "公告列表",
          "Announcement Table",
          null,
          false,
          null,
          false,
          true,
          0,
          "create,update,delete,query"
        )
      );
      list.add(
        new SeedNode(
          "announcement",
          null,
          "PAGE",
          "cards",
          "AnnouncementCards",
          "/announcement/cards/index",
          null,
          "公告卡片",
          "Announcement Cards",
          null,
          false,
          null,
          false,
          true,
          1,
          "create,update,delete,query"
        )
      );



      list.add(
        new SeedNode(
          null,
          null,
          "DIR",
          "/console",
          "console",
          "LAYOUT",
          "/console/download",
          "文件下载",
          "Console",
          "download",
          false,
          null,
          false,
          true,
          6,
          null
        )
      );
      list.add(
        new SeedNode(
          "console",
          null,
          "PAGE",
          "download",
          "ConsoleDownload",
          "/console/download/index",
          null,
          "文件下载",
          "File Download",
          "download",
          false,
          null,
          false,
          true,
          0,
          "create,update,delete,query"
        )
      );
      return list;
    }
  }
}
