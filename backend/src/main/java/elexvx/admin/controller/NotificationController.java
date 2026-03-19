package elexvx.admin.controller;

import elexvx.admin.annotation.RepeatSubmit;
import elexvx.admin.dto.NotificationBroadcastRequest;
import elexvx.admin.dto.NotificationUpsertRequest;
import elexvx.admin.service.NotificationService;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.NotificationResponse;
import elexvx.admin.vo.NotificationSummary;
import elexvx.admin.vo.PageResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationController {
  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<PageResult<NotificationResponse>> list(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String priority,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    return ApiResponse.success(service.page(keyword, status, priority, page, size));
  }

  @GetMapping("/latest")
  public ApiResponse<List<NotificationSummary>> latest(@RequestParam(defaultValue = "8") int size) {
    return ApiResponse.success(service.latestPublished(size));
  }

  @GetMapping("/{id}")
  public ApiResponse<NotificationResponse> detail(@PathVariable Long id) {
    return ApiResponse.success(service.detail(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<NotificationResponse> create(@RequestBody @Valid NotificationUpsertRequest req) {
    return ApiResponse.success(service.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<NotificationResponse> update(@PathVariable Long id, @RequestBody @Valid NotificationUpsertRequest req) {
    return ApiResponse.success(service.update(id, req));
  }

  @PostMapping("/{id}/publish")
  @RepeatSubmit
  public ApiResponse<NotificationResponse> publish(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean publish) {
    return ApiResponse.success(service.publish(id, publish));
  }

  @PostMapping("/broadcast")
  @RepeatSubmit
  public ApiResponse<NotificationResponse> broadcast(@RequestBody @Valid NotificationBroadcastRequest req) {
    NotificationUpsertRequest upsert = new NotificationUpsertRequest();
    upsert.setTitle(req.getTitle());
    upsert.setSummary(req.getSummary());
    upsert.setContent(req.getContent());
    upsert.setPriority(req.getPriority());
    upsert.setStatus("published");
    return ApiResponse.success(service.create(upsert));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    return ApiResponse.success(service.delete(id));
  }
}
