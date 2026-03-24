package elexvx.admin.controller;

import elexvx.admin.annotation.RepeatSubmit;
import elexvx.admin.dto.DictionaryCreateRequest;
import elexvx.admin.dto.DictionaryItemCreateRequest;
import elexvx.admin.dto.DictionaryItemUpdateRequest;
import elexvx.admin.dto.DictionaryUpdateRequest;
import elexvx.admin.entity.SysDict;
import elexvx.admin.entity.SysDictItem;
import elexvx.admin.service.DictionaryService;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.DictionaryImportResult;
import elexvx.admin.vo.PageResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/system/dict")
public class SystemDictController {
  private final DictionaryService dictionaryService;

  public SystemDictController(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @GetMapping("/page")
  public ApiResponse<PageResult<SysDict>> page(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Integer status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    PermissionUtil.check("system:SystemDict:query");
    return ApiResponse.success(dictionaryService.page(keyword, status, page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<SysDict> get(@PathVariable long id) {
    PermissionUtil.check("system:SystemDict:query");
    return ApiResponse.success(dictionaryService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<SysDict> create(@RequestBody @Valid DictionaryCreateRequest req) {
    PermissionUtil.check("system:SystemDict:create");
    return ApiResponse.success(dictionaryService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<SysDict> update(@PathVariable long id, @RequestBody @Valid DictionaryUpdateRequest req) {
    PermissionUtil.check("system:SystemDict:update");
    return ApiResponse.success(dictionaryService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    PermissionUtil.check("system:SystemDict:delete");
    return ApiResponse.success(dictionaryService.delete(id));
  }

  @GetMapping("/{dictId}/items")
  public ApiResponse<PageResult<SysDictItem>> pageItems(
    @PathVariable long dictId,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Integer status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    PermissionUtil.check("system:SystemDict:query");
    return ApiResponse.success(dictionaryService.pageItems(dictId, keyword, status, page, size));
  }

  @GetMapping("/code/{code}/items")
  public ApiResponse<List<SysDictItem>> listByCode(@PathVariable String code) {
    return ApiResponse.success(dictionaryService.listItemsByCode(code));
  }

  @PostMapping("/{dictId}/items")
  @RepeatSubmit
  public ApiResponse<SysDictItem> createItem(@PathVariable long dictId, @RequestBody @Valid DictionaryItemCreateRequest req) {
    PermissionUtil.check("system:SystemDict:create");
    return ApiResponse.success(dictionaryService.createItem(dictId, req));
  }

  @PutMapping("/items/{id}")
  @RepeatSubmit
  public ApiResponse<SysDictItem> updateItem(@PathVariable long id, @RequestBody @Valid DictionaryItemUpdateRequest req) {
    PermissionUtil.check("system:SystemDict:update");
    return ApiResponse.success(dictionaryService.updateItem(id, req));
  }

  @DeleteMapping("/items/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> deleteItem(@PathVariable long id) {
    PermissionUtil.check("system:SystemDict:delete");
    return ApiResponse.success(dictionaryService.deleteItem(id));
  }

  @PostMapping("/{dictId}/items/import")
  @RepeatSubmit
  public ApiResponse<DictionaryImportResult> importItems(@PathVariable long dictId, @RequestParam("file") MultipartFile file) {
    PermissionUtil.check("system:SystemDict:update");
    return ApiResponse.success(dictionaryService.importItems(dictId, file));
  }

  @GetMapping("/{dictId}/items/export")
  public ResponseEntity<byte[]> exportItems(@PathVariable long dictId) {
    // 导出接口返回二进制文件，不能包装为 ApiResponse。
    PermissionUtil.check("system:SystemDict:query");
    return dictionaryService.exportItems(dictId);
  }

  @GetMapping("/items/template")
  public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
    // 模板下载直接写入响应流，不能包装为 ApiResponse。
    PermissionUtil.check("system:SystemDict:query");
    dictionaryService.downloadTemplateXlsx(response);
  }
}
