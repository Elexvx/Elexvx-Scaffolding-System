package com.tencent.tdesign.service;

import com.tencent.tdesign.dao.AuthQueryDao;
import com.tencent.tdesign.dto.RoleUpsertRequest;
import com.tencent.tdesign.entity.RoleEntity;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.mapper.MenuItemMapper;
import com.tencent.tdesign.mapper.RoleMapper;
import com.tencent.tdesign.vo.RoleResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleAdminService {
  private final RoleMapper roleMapper;
  private final AuthQueryDao authDao;
  private final MenuItemMapper menuItemMapper;
  private final PermissionCodeService permissionCodeService;
  private final OperationLogService operationLogService;

  public RoleAdminService(
    RoleMapper roleMapper,
    AuthQueryDao authDao,
    MenuItemMapper menuItemMapper,
    PermissionCodeService permissionCodeService,
    OperationLogService operationLogService
  ) {
    this.roleMapper = roleMapper;
    this.authDao = authDao;
    this.menuItemMapper = menuItemMapper;
    this.permissionCodeService = permissionCodeService;
    this.operationLogService = operationLogService;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public List<RoleResponse> list() {
    List<RoleEntity> roles = roleMapper.selectAll();
    List<RoleResponse> out = new ArrayList<>();
    for (RoleEntity r : roles) {
      RoleResponse rr = new RoleResponse();
      rr.setId(r.getId());
      rr.setName(r.getName());
      rr.setDescription(r.getDescription());
      rr.setPermissions(authDao.findPermissionsByRoleId(r.getId()));
      rr.setMenuIds(authDao.findMenuIdsByRoleId(r.getId()));
      out.add(rr);
    }
    return out;
  }

  public RoleResponse get(long id) {
    RoleEntity r = Optional.ofNullable(roleMapper.selectById(id)).orElseThrow(() -> badRequest("角色不存在"));
    RoleResponse rr = new RoleResponse();
    rr.setId(r.getId());
    rr.setName(r.getName());
    rr.setDescription(r.getDescription());
    rr.setPermissions(authDao.findPermissionsByRoleId(r.getId()));
    rr.setMenuIds(authDao.findMenuIdsByRoleId(r.getId()));
    return rr;
  }

  @Transactional
  public RoleResponse create(RoleUpsertRequest req) {
    if (roleMapper.countByName(req.getName()) > 0) throw badRequest("角色已存在");
    RoleEntity r = new RoleEntity();
    r.setName(req.getName());
    r.setDescription(req.getDescription());
    r = saveRole(r);
    authDao.replaceRoleMenus(r.getId(), req.getMenuIds());
    authDao.replaceRolePermissions(r.getId(), resolvePermissionCodes(req));
    operationLogService.log("CREATE", "角色管理", "创建角色: " + r.getName());
    return get(r.getId());
  }

  @Transactional
  public RoleResponse update(long id, RoleUpsertRequest req) {
    RoleEntity r = Optional.ofNullable(roleMapper.selectById(id)).orElseThrow(() -> badRequest("角色不存在"));
    if ("admin".equals(r.getName()) && req.getName() != null && !"admin".equals(req.getName())) {
      throw badRequest("不允许修改 admin 角色名");
    }
    if (req.getName() != null && !req.getName().equals(r.getName()) && roleMapper.countByName(req.getName()) > 0) {
      throw badRequest("角色名已存在");
    }
    if (req.getName() != null) r.setName(req.getName());
    if (req.getDescription() != null) r.setDescription(req.getDescription());
    saveRole(r);
    if (req.getMenuIds() != null) {
      authDao.replaceRoleMenus(r.getId(), req.getMenuIds());
    }
    List<String> permissionCodes = resolvePermissionCodesForUpdate(req);
    if (permissionCodes != null) {
      authDao.replaceRolePermissions(r.getId(), permissionCodes);
    }
    operationLogService.log("UPDATE", "角色管理", "更新角色: " + r.getName());
    return get(id);
  }

  @Transactional
  public boolean delete(long id) {
    RoleEntity r = Optional.ofNullable(roleMapper.selectById(id)).orElseThrow(() -> badRequest("角色不存在"));
    if ("admin".equals(r.getName())) throw badRequest("不允许删除 admin 角色");
    roleMapper.deleteById(id);
    operationLogService.log("DELETE", "角色管理", "删除角色: " + r.getName());
    return true;
  }


  private RoleEntity saveRole(RoleEntity role) {
    if (role.getId() == null) {
      roleMapper.insert(role);
    } else {
      roleMapper.update(role);
    }
    return role;
  }

  @Transactional
  public void ensureAdminHasAllMenus() {
    authDao.ensureAdminHasAllMenus();
  }

  private List<String> resolvePermissionCodesForUpdate(RoleUpsertRequest req) {
    if (req.getMenuActionMap() != null) {
      return toPermissionCodes(req.getMenuActionMap());
    }
    if (req.getPermissions() != null) {
      return req.getPermissions();
    }
    return null;
  }

  private List<String> resolvePermissionCodes(RoleUpsertRequest req) {
    if (req.getMenuActionMap() != null) {
      return toPermissionCodes(req.getMenuActionMap());
    }
    return req.getPermissions();
  }

  private List<String> toPermissionCodes(Map<Long, List<String>> menuActionMap) {
    if (menuActionMap == null || menuActionMap.isEmpty()) return List.of();

    Set<Long> menuIds = new HashSet<>();
    menuActionMap.forEach((menuId, actions) -> {
      if (menuId != null && actions != null && !actions.isEmpty()) {
        menuIds.add(menuId);
      }
    });
    if (menuIds.isEmpty()) return List.of();

    Map<Long, String> routeByMenuId = new HashMap<>();
    menuItemMapper.selectByIds(menuIds).forEach(menu -> {
      if (menu == null || menu.getId() == null) return;
      String routeName = normalize(menu.getRouteName());
      if (routeName != null) {
        routeByMenuId.put(menu.getId(), routeName);
      }
    });

    Set<String> permissionCodes = new HashSet<>();
    menuActionMap.forEach((menuId, actions) -> {
      if (menuId == null || actions == null || actions.isEmpty()) return;
      String routeName = routeByMenuId.get(menuId);
      if (routeName == null) return;
      List<String> normalizedActions = permissionCodeService.normalizeActions(actions);
      for (String action : normalizedActions) {
        String code = permissionCodeService.buildPermissionCode(routeName, action);
        if (code != null) {
          permissionCodes.add(code);
        }
      }
    });
    return List.copyOf(permissionCodes);
  }

  private String normalize(String text) {
    if (text == null) return null;
    String value = text.trim();
    return value.isEmpty() ? null : value;
  }
}
