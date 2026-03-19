package elexvx.admin.controller;

import elexvx.admin.service.AreaService;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.AreaNodeResponse;
import elexvx.admin.vo.AreaPathNode;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/area")
public class AreaController {
  private final AreaService areaService;

  public AreaController(AreaService areaService) {
    this.areaService = areaService;
  }

  @GetMapping("/children")
  public ApiResponse<List<AreaNodeResponse>> listChildren(@RequestParam(defaultValue = "0") Integer parentId) {
    return ApiResponse.success(areaService.listChildren(parentId));
  }

  @GetMapping("/path")
  public ApiResponse<List<AreaPathNode>> getPath(@RequestParam Integer areaId) {
    return ApiResponse.success(areaService.getPath(areaId));
  }

  @GetMapping("/resolve")
  public ApiResponse<List<AreaPathNode>> resolve(
    @RequestParam(required = false) String province,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String district
  ) {
    return ApiResponse.success(areaService.resolvePath(province, city, district));
  }
}
