package top.elexvx.admin.controller;

import top.elexvx.admin.annotation.RepeatSubmit;
import top.elexvx.admin.dto.AnnouncementUpsertRequest;
import top.elexvx.admin.service.AnnouncementService;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.AnnouncementResponse;
import top.elexvx.admin.vo.AnnouncementSummary;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.PageResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {
  private final AnnouncementService service;

  public AnnouncementController(AnnouncementService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<PageResult<AnnouncementResponse>> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String priority
  ) {
    return ApiResponse.success(service.page(keyword, status, priority, page, size));
  }

  @GetMapping("/latest")
  public ApiResponse<List<AnnouncementSummary>> latest(@RequestParam(defaultValue = "8") int size) {
    return ApiResponse.success(service.latestPublished(size));
  }

  @GetMapping("/{id}")
  public ApiResponse<AnnouncementResponse> detail(@PathVariable Long id) {
    return ApiResponse.success(service.detail(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<AnnouncementResponse> create(@RequestBody @Valid AnnouncementUpsertRequest req) {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(service.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<AnnouncementResponse> update(@PathVariable Long id, @RequestBody @Valid AnnouncementUpsertRequest req) {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(service.update(id, req));
  }

  @PostMapping("/{id}/publish")
  @RepeatSubmit
  public ApiResponse<AnnouncementResponse> publish(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean publish) {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(service.publish(id, publish));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(service.delete(id));
  }
}
