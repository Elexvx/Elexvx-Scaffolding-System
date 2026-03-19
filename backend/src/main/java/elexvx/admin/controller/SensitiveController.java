package elexvx.admin.controller;

import elexvx.admin.dto.SensitiveSettingsRequest;
import elexvx.admin.dto.SensitiveWordCreateRequest;
import elexvx.admin.entity.SensitiveWord;
import elexvx.admin.service.SensitiveService;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.PageResult;
import elexvx.admin.vo.SensitiveImportResult;
import elexvx.admin.vo.SensitiveSettingsResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/system/sensitive")
public class SensitiveController {
  private final SensitiveService sensitiveService;

  public SensitiveController(SensitiveService sensitiveService) {
    this.sensitiveService = sensitiveService;
  }

  @GetMapping("/words")
  public ApiResponse<List<SensitiveWord>> listWords(@RequestParam(required = false) String keyword) {
    PermissionUtil.check("system:SystemSensitive:query");
    return ApiResponse.success(sensitiveService.listWords(keyword));
  }

  @GetMapping("/words/page")
  public ApiResponse<PageResult<SensitiveWord>> pageWords(
    @RequestParam(required = false) String keyword,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    PermissionUtil.check("system:SystemSensitive:query");
    return ApiResponse.success(sensitiveService.pageWords(keyword, page, size));
  }

  @PostMapping("/words")
  public ApiResponse<SensitiveWord> createWord(@RequestBody @Valid SensitiveWordCreateRequest req) {
    PermissionUtil.check("system:SystemSensitive:create");
    return ApiResponse.success(sensitiveService.createWord(req.getWord()));
  }

  @DeleteMapping("/words/{id}")
  public ApiResponse<Boolean> deleteWord(@PathVariable long id) {
    PermissionUtil.check("system:SystemSensitive:delete");
    return ApiResponse.success(sensitiveService.deleteWord(id));
  }

  @GetMapping("/words/template")
  public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
    PermissionUtil.check("system:SystemSensitive:query");
    sensitiveService.downloadTemplate(response);
  }

  @PostMapping("/words/import")
  public ApiResponse<SensitiveImportResult> importWords(@RequestParam("file") MultipartFile file) {
    PermissionUtil.check("system:SystemSensitive:create");
    return ApiResponse.success(sensitiveService.importWords(file));
  }

  @GetMapping("/settings")
  public ApiResponse<SensitiveSettingsResponse> getSettings() {
    PermissionUtil.check("system:SystemSensitive:query");
    return ApiResponse.success(sensitiveService.getSettings());
  }

  @PostMapping("/settings")
  public ApiResponse<SensitiveSettingsResponse> saveSettings(@RequestBody SensitiveSettingsRequest req) {
    PermissionUtil.check("system:SystemSensitive:update");
    return ApiResponse.success(sensitiveService.saveSettings(req));
  }
}
