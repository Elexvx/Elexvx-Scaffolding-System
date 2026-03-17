package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.DictionaryCreateRequest;
import com.tencent.tdesign.dto.DictionaryItemCreateRequest;
import com.tencent.tdesign.dto.DictionaryItemUpdateRequest;
import com.tencent.tdesign.dto.DictionaryUpdateRequest;
import com.tencent.tdesign.entity.SysDict;
import com.tencent.tdesign.entity.SysDictItem;
import com.tencent.tdesign.service.DictionaryService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.DictionaryImportResult;
import com.tencent.tdesign.vo.PageResult;
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
    PermissionUtil.check("system:SystemDict:query");
    return dictionaryService.exportItems(dictId);
  }

  @GetMapping("/items/template")
  public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
    PermissionUtil.check("system:SystemDict:query");
    dictionaryService.downloadTemplateXlsx(response);
  }
}
