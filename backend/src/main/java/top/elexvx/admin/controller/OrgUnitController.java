package top.elexvx.admin.controller;

import top.elexvx.admin.annotation.RepeatSubmit;
import top.elexvx.admin.dto.OrgUnitAddUsersRequest;
import top.elexvx.admin.dto.OrgUnitReorderRequest;
import top.elexvx.admin.dto.OrgUnitUpsertRequest;
import top.elexvx.admin.service.OrgUnitService;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.OrgUnitNode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/org")
public class OrgUnitController {
  private final OrgUnitService orgUnitService;

  public OrgUnitController(OrgUnitService orgUnitService) {
    this.orgUnitService = orgUnitService;
  }

  @GetMapping("/tree")
  public ApiResponse<List<OrgUnitNode>> tree() {
    PermissionUtil.check("system:SystemOrg:query");
    return ApiResponse.success(orgUnitService.tree());
  }

  @GetMapping("/{id}")
  public ApiResponse<OrgUnitNode> get(@PathVariable long id) {
    PermissionUtil.check("system:SystemOrg:query");
    return ApiResponse.success(orgUnitService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<OrgUnitNode> create(@RequestBody @Valid OrgUnitUpsertRequest req) {
    PermissionUtil.check("system:SystemOrg:create");
    return ApiResponse.success(orgUnitService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<OrgUnitNode> update(@PathVariable long id, @RequestBody @Valid OrgUnitUpsertRequest req) {
    PermissionUtil.check("system:SystemOrg:update");
    return ApiResponse.success(orgUnitService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    PermissionUtil.check("system:SystemOrg:delete");
    return ApiResponse.success(orgUnitService.delete(id));
  }

  @PutMapping("/reorder")
  @RepeatSubmit
  public ApiResponse<Boolean> reorder(@RequestBody @Valid OrgUnitReorderRequest req) {
    PermissionUtil.check("system:SystemOrg:update");
    return ApiResponse.success(orgUnitService.reorder(req));
  }

  @PutMapping("/{id}/users")
  @RepeatSubmit
  public ApiResponse<Boolean> addUsers(@PathVariable long id, @RequestBody @Valid OrgUnitAddUsersRequest req) {
    PermissionUtil.check("system:SystemOrg:update");
    return ApiResponse.success(orgUnitService.addUsers(id, req.getUserIds()));
  }
}
