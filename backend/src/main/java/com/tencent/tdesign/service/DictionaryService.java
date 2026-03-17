package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.DictionaryCreateRequest;
import com.tencent.tdesign.dto.DictionaryItemCreateRequest;
import com.tencent.tdesign.dto.DictionaryItemUpdateRequest;
import com.tencent.tdesign.dto.DictionaryUpdateRequest;
import com.tencent.tdesign.entity.SysDict;
import com.tencent.tdesign.entity.SysDictItem;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.mapper.SysDictMapper;
import com.tencent.tdesign.util.ExcelExportUtil;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.DictionaryImportResult;
import com.tencent.tdesign.vo.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DictionaryService {
  private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
  private static final int MAX_IMPORT_ERRORS = 50;
  private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z0-9_]+$");
  private static final String ADDRESS_DISTRICT_CODE = "address_district";
  private static final String OBSOLETE_TEAM_CODE = "team";
  private static final List<BuiltinDictSpec> BUILTIN_DICT_SPECS = List.of(
    new BuiltinDictSpec("gender", "\u6027\u522b", "\u6027\u522b\u9009\u9879", 1),
    new BuiltinDictSpec("id_document_type", "\u8bc1\u4ef6-\u7c7b\u578b", "\u8bc1\u4ef6\u7c7b\u578b", 2),
    new BuiltinDictSpec("address_district", "\u5730\u5740-\u533a", "\u5730\u5740\u533a\u53bf", 4),
    new BuiltinDictSpec("announcement_type", "\u516c\u544a-\u7c7b\u578b", "\u516c\u544a\u7c7b\u578b", 5),
    new BuiltinDictSpec("announcement_priority", "\u516c\u544a-\u4f18\u5148\u7ea7", "\u516c\u544a\u4f18\u5148\u7ea7", 6),
    new BuiltinDictSpec("announcement_status", "\u516c\u544a-\u72b6\u6001", "\u516c\u544a\u72b6\u6001", 7),
    new BuiltinDictSpec("notification_type", "\u901a\u77e5-\u7c7b\u578b", "\u901a\u77e5\u7c7b\u578b", 8),
    new BuiltinDictSpec("notification_priority", "\u901a\u77e5-\u4f18\u5148\u7ea7", "\u901a\u77e5\u4f18\u5148\u7ea7", 9),
    new BuiltinDictSpec("notification_status", "\u901a\u77e5-\u72b6\u6001", "\u901a\u77e5\u72b6\u6001", 10),
    new BuiltinDictSpec("message_quality", "\u6d88\u606f-\u4f18\u5148\u7ea7", "\u6d88\u606f\u4f18\u5148\u7ea7", 11),
    new BuiltinDictSpec("log_action", "\u65e5\u5fd7-\u64cd\u4f5c\u7c7b\u578b", "\u64cd\u4f5c\u65e5\u5fd7\u7c7b\u578b", 12),
    new BuiltinDictSpec("user_status", "\u7528\u6237-\u72b6\u6001", "\u7528\u6237\u72b6\u6001", 13),
    new BuiltinDictSpec("org_type", "\u673a\u6784-\u7c7b\u578b", "\u673a\u6784\u7c7b\u578b", 14),
    new BuiltinDictSpec("org_status", "\u673a\u6784-\u72b6\u6001", "\u673a\u6784\u72b6\u6001", 15),
    new BuiltinDictSpec("menu_node_type", "\u83dc\u5355-\u8282\u70b9\u7c7b\u578b", "\u83dc\u5355\u8282\u70b9\u7c7b\u578b", 16),
    new BuiltinDictSpec("menu_action", "\u83dc\u5355-\u6743\u9650\u52a8\u4f5c", "\u83dc\u5355\u6743\u9650\u52a8\u4f5c", 17),
    new BuiltinDictSpec("storage_provider", "\u5b58\u50a8-\u63d0\u4f9b\u5546", "\u5b58\u50a8\u63d0\u4f9b\u5546", 18),
    new BuiltinDictSpec("sms_provider", "\u77ed\u4fe1-\u901a\u9053", "\u77ed\u4fe1\u901a\u9053", 19),
    new BuiltinDictSpec("ai_vendor", "AI-\u5382\u5546", "AI\u5382\u5546", 20)
  );

  private final SysDictMapper dictMapper;
  private final DictionaryItemTableService itemTableService;
  private final OperationLogService operationLogService;

  public DictionaryService(
    SysDictMapper dictMapper,
    DictionaryItemTableService itemTableService,
    OperationLogService operationLogService
  ) {
    this.dictMapper = dictMapper;
    this.itemTableService = itemTableService;
    this.operationLogService = operationLogService;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public PageResult<SysDict> page(String keyword, Integer status, int page, int size) {
    PermissionUtil.check("system:SystemDict:query");
    ensureBuiltinDictionaryMetadata();
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
    List<SysDict> rows = dictMapper.selectPage(kw, status, offset, safeSize);
    long total = dictMapper.countByKeyword(kw, status);
    return new PageResult<>(rows, total);
  }

  public SysDict get(long id) {
    PermissionUtil.check("system:SystemDict:query");
    SysDict dict = dictMapper.selectById(id);
    if (dict == null) {
      throw badRequest("Dictionary not found.");
    }
    return dict;
  }

  @Transactional
  public SysDict create(DictionaryCreateRequest req) {
    PermissionUtil.check("system:SystemDict:create");
    String code = normalizeCode(req.getCode());
    if (dictMapper.selectByCode(code) != null) {
      throw badRequest("Dictionary code already exists.");
    }

    SysDict dict = new SysDict();
    dict.setName(req.getName().trim());
    dict.setCode(code);
    dict.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    dict.setSort(req.getSort() == null ? 0 : req.getSort());
    dict.setRemark(req.getRemark());
    dict.setCreatedAt(LocalDateTime.now());
    dict.setUpdatedAt(LocalDateTime.now());
    dictMapper.insert(dict);

    itemTableService.ensureDictTable(dict);
    operationLogService.log("CREATE", "Dictionary", "Create dictionary: " + dict.getName());
    return dict;
  }

  @Transactional
  public SysDict update(long id, DictionaryUpdateRequest req) {
    PermissionUtil.check("system:SystemDict:update");
    SysDict dict = dictMapper.selectById(id);
    if (dict == null) {
      throw badRequest("Dictionary not found.");
    }

    String previousCode = dict.getCode();
    if (req.getName() != null) {
      dict.setName(req.getName().trim());
    }

    if (req.getCode() != null) {
      String code = normalizeCode(req.getCode());
      SysDict exists = dictMapper.selectByCode(code);
      if (exists != null && !Objects.equals(exists.getId(), id)) {
        throw badRequest("Dictionary code already exists.");
      }
      if (!code.equals(previousCode)) {
        itemTableService.renameDictTable(dict, code);
      }
      dict.setCode(code);
    }

    if (req.getStatus() != null) {
      dict.setStatus(req.getStatus());
    }
    if (req.getSort() != null) {
      dict.setSort(req.getSort());
    }
    if (req.getRemark() != null) {
      dict.setRemark(req.getRemark());
    }

    dict.setUpdatedAt(LocalDateTime.now());
    dictMapper.update(dict);
    operationLogService.log("UPDATE", "Dictionary", "Update dictionary: " + dict.getName());
    return dict;
  }

  @Transactional
  public boolean delete(long id) {
    PermissionUtil.check("system:SystemDict:delete");
    SysDict dict = dictMapper.selectById(id);
    if (dict == null) {
      return true;
    }

    itemTableService.dropDictTable(dict.getCode());
    dictMapper.deleteById(id);
    operationLogService.log("DELETE", "Dictionary", "Delete dictionary: " + dict.getName());
    return true;
  }

  public PageResult<SysDictItem> pageItems(long dictId, String keyword, Integer status, int page, int size) {
    PermissionUtil.check("system:SystemDict:query");
    ensureBuiltinDictionaryMetadata();
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw badRequest("Dictionary not found.");
    }
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

    List<SysDictItem> rows = itemTableService.selectPage(dict, kw, status, offset, safeSize);
    long total = itemTableService.countByDict(dict, kw, status);
    return new PageResult<>(rows, total);
  }

  public List<SysDictItem> listItemsByCode(String code) {
    PermissionUtil.check("system:SystemDict:query");
    ensureBuiltinDictionaryMetadata();
    if (!StringUtils.hasText(code)) {
      return List.of();
    }

    SysDict dict = dictMapper.selectByCode(code.trim().toLowerCase(Locale.ROOT));
    if (dict == null || !Integer.valueOf(1).equals(dict.getStatus())) {
      return List.of();
    }
    return itemTableService.selectEnabledByDict(dict);
  }

  @Transactional
  public SysDictItem createItem(long dictId, DictionaryItemCreateRequest req) {
    PermissionUtil.check("system:SystemDict:create");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw badRequest("Dictionary not found.");
    }

    String value = req.getValue().trim();
    SysDictItem existing = itemTableService.selectByDictValue(dict, value);
    if (existing != null) {
      throw badRequest("Dictionary value already exists.");
    }

    SysDictItem item = new SysDictItem();
    item.setDictId(dictId);
    item.setLabel(req.getLabel().trim());
    item.setValue(value);
    item.setValueType(normalizeValueType(req.getValueType()));
    item.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    item.setSort(req.getSort() == null ? 0 : req.getSort());
    item.setTagColor(normalizeText(req.getTagColor()));
    item.setProvince(normalizeText(req.getProvince()));
    item.setCity(normalizeText(req.getCity()));
    item.setDistrict(normalizeText(req.getDistrict()));
    validateAreaItemFields(dict, item.getProvince(), item.getCity(), item.getDistrict());
    item.setCreatedAt(LocalDateTime.now());
    item.setUpdatedAt(LocalDateTime.now());
    itemTableService.insert(dict, item);

    operationLogService.log("CREATE", "Dictionary", "Create dictionary item: " + dict.getName() + " - " + item.getLabel());
    return item;
  }

  @Transactional
  public SysDictItem updateItem(long id, DictionaryItemUpdateRequest req) {
    PermissionUtil.check("system:SystemDict:update");
    DictItemLocation location = requireItemLocation(id);
    SysDict dict = location.dict;
    SysDictItem item = location.item;

    if (req.getValue() != null) {
      String value = req.getValue().trim();
      SysDictItem existing = itemTableService.selectByDictValue(dict, value);
      if (existing != null && !Objects.equals(existing.getId(), item.getId())) {
        throw badRequest("Dictionary value already exists.");
      }
      item.setValue(value);
    }
    if (req.getLabel() != null) {
      item.setLabel(req.getLabel().trim());
    }
    if (req.getValueType() != null) {
      item.setValueType(normalizeValueType(req.getValueType()));
    }
    if (req.getStatus() != null) {
      item.setStatus(req.getStatus());
    }
    if (req.getSort() != null) {
      item.setSort(req.getSort());
    }
    if (req.getTagColor() != null) {
      item.setTagColor(normalizeText(req.getTagColor()));
    }
    if (req.getProvince() != null) {
      item.setProvince(normalizeText(req.getProvince()));
    }
    if (req.getCity() != null) {
      item.setCity(normalizeText(req.getCity()));
    }
    if (req.getDistrict() != null) {
      item.setDistrict(normalizeText(req.getDistrict()));
    }
    validateAreaItemFields(dict, item.getProvince(), item.getCity(), item.getDistrict());
    item.setUpdatedAt(LocalDateTime.now());

    itemTableService.update(dict, item);
    operationLogService.log("UPDATE", "Dictionary", "Update dictionary item: " + item.getLabel());
    return item;
  }

  @Transactional
  public boolean deleteItem(long id) {
    PermissionUtil.check("system:SystemDict:delete");
    DictItemLocation location = findItemLocation(id);
    if (location == null) {
      return true;
    }

    itemTableService.deleteById(location.dict, id);
    operationLogService.log("DELETE", "Dictionary", "Delete dictionary item: " + location.item.getLabel());
    return true;
  }

  @Transactional
  public DictionaryImportResult importItems(long dictId, MultipartFile file) {
    PermissionUtil.check("system:SystemDict:create");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw badRequest("Dictionary not found.");
    }
    if (file == null || file.isEmpty()) {
      throw badRequest("Uploaded file is empty.");
    }

    DictionaryImportResult result = new DictionaryImportResult();
    String name = file.getOriginalFilename();
    String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
    List<DictItemRow> rows;
    try {
      if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
        rows = readFromExcel(file);
      } else if (lower.endsWith(".csv") || lower.endsWith(".txt")) {
        rows = readFromCsv(file);
      } else {
        throw badRequest("Only .xlsx/.xls/.csv/.txt files are supported.");
      }
    } catch (Exception e) {
      throw badRequest("Failed to read dictionary file: " + e.getMessage());
    }

    int total = 0;
    int imported = 0;
    int updated = 0;
    int skipped = 0;
    int failed = 0;

    for (DictItemRow row : rows) {
      total += 1;
      if (!StringUtils.hasText(row.label) || !StringUtils.hasText(row.value)) {
        failed += 1;
        if (result.getErrors().size() < MAX_IMPORT_ERRORS) {
          result.addError("Row " + total + " missing label or value.");
        }
        continue;
      }

      String value = row.value.trim();
      SysDictItem existing = itemTableService.selectByDictValue(dict, value);
      if (existing == null) {
        SysDictItem item = new SysDictItem();
        item.setDictId(dictId);
        item.setLabel(row.label.trim());
        item.setValue(value);
        item.setValueType(inferValueTypeFromValue(value));
        item.setStatus(row.status == null ? 1 : row.status);
        item.setSort(row.sort == null ? 0 : row.sort);
        item.setTagColor(normalizeText(row.tagColor));
        item.setProvince(normalizeText(row.province));
        item.setCity(normalizeText(row.city));
        item.setDistrict(normalizeText(row.district));
        try {
          validateAreaItemFields(dict, item.getProvince(), item.getCity(), item.getDistrict());
        } catch (BusinessException ex) {
          failed += 1;
          if (result.getErrors().size() < MAX_IMPORT_ERRORS) {
            result.addError("Row " + total + " " + ex.getMessage());
          }
          continue;
        }
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemTableService.insert(dict, item);
        imported += 1;
      } else {
        existing.setLabel(row.label.trim());
        existing.setValueType(inferValueTypeFromValue(value));
        existing.setStatus(row.status == null ? existing.getStatus() : row.status);
        existing.setSort(row.sort == null ? existing.getSort() : row.sort);
        if (row.tagColor != null) {
          existing.setTagColor(normalizeText(row.tagColor));
        }
        if (row.province != null) {
          existing.setProvince(normalizeText(row.province));
        }
        if (row.city != null) {
          existing.setCity(normalizeText(row.city));
        }
        if (row.district != null) {
          existing.setDistrict(normalizeText(row.district));
        }
        try {
          validateAreaItemFields(dict, existing.getProvince(), existing.getCity(), existing.getDistrict());
        } catch (BusinessException ex) {
          failed += 1;
          if (result.getErrors().size() < MAX_IMPORT_ERRORS) {
            result.addError("Row " + total + " " + ex.getMessage());
          }
          continue;
        }
        existing.setUpdatedAt(LocalDateTime.now());
        itemTableService.update(dict, existing);
        updated += 1;
      }
    }

    result.setTotal(total);
    result.setImported(imported);
    result.setUpdated(updated);
    result.setSkipped(skipped);
    result.setFailed(failed);
    operationLogService.log("IMPORT", "Dictionary", "Import dictionary items: " + dict.getName() + " imported=" + imported);
    return result;
  }

  public ResponseEntity<byte[]> exportItems(long dictId) {
    PermissionUtil.check("system:SystemDict:query");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw badRequest("Dictionary not found.");
    }

    List<SysDictItem> items = itemTableService.selectByDict(dict);
    Workbook workbook = new XSSFWorkbook();
    try {
      Sheet sheet = workbook.createSheet("字典项");
      var headerStyle = ExcelExportUtil.createHeaderStyle(workbook);
      var bodyStyle = ExcelExportUtil.createBodyStyle(workbook);
      String[] headers = new String[] { "序号", "名称", "数据值", "状态", "排序", "标签颜色", "省", "市", "区" };
      ExcelExportUtil.writeHeaderRow(sheet, 0, headers, headerStyle);

      int rowIndex = 1;
      for (SysDictItem item : items) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(rowIndex - 1);
        row.createCell(1).setCellValue(item.getLabel() == null ? "" : item.getLabel());
        row.createCell(2).setCellValue(item.getValue() == null ? "" : item.getValue());
        row.createCell(3).setCellValue(item.getStatus() == null ? "" : String.valueOf(item.getStatus()));
        row.createCell(4).setCellValue(item.getSort() == null ? "" : String.valueOf(item.getSort()));
        row.createCell(5).setCellValue(item.getTagColor() == null ? "" : item.getTagColor());
        row.createCell(6).setCellValue(item.getProvince() == null ? "" : item.getProvince());
        row.createCell(7).setCellValue(item.getCity() == null ? "" : item.getCity());
        row.createCell(8).setCellValue(item.getDistrict() == null ? "" : item.getDistrict());
        ExcelExportUtil.applyRowCellStyle(row, headers.length, bodyStyle);
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
        int width = Math.min(Math.max(sheet.getColumnWidth(i) + 512, 12 * 256), 60 * 256);
        sheet.setColumnWidth(i, width);
      }

      String filename = "dict_" + dict.getCode() + "_items.xlsx";
      return ExcelExportUtil.toXlsxResponse(workbook, filename);
    } finally {
      try {
        workbook.close();
      } catch (Exception closeException) {
        log.debug("关闭字典导出工作簿失败", closeException);
      }
    }
  }

  public void downloadTemplate(HttpServletResponse response) {
    PermissionUtil.check("system:SystemDict:query");
    try {
      String content =
        "index,label,value,status,sort,tag_color,province,city,district\n1,sample,example,1,1,success,广东省,深圳市,南山区\n";
      response.setContentType("text/csv; charset=UTF-8");
      response.setHeader("Content-Disposition", "attachment; filename=dict_items_template.csv");
      response.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate template: " + e.getMessage());
    }
  }

  public void downloadTemplateXlsx(HttpServletResponse response) {
    PermissionUtil.check("system:SystemDict:query");
    Workbook workbook = new XSSFWorkbook();
    try {
      Sheet sheet = workbook.createSheet("字典项模板");
      var headerStyle = ExcelExportUtil.createHeaderStyle(workbook);
      var bodyStyle = ExcelExportUtil.createBodyStyle(workbook);
      String[] headers = new String[] { "序号", "名称", "数据值", "状态", "排序", "标签颜色", "省", "市", "区" };
      ExcelExportUtil.writeHeaderRow(sheet, 0, headers, headerStyle);

      Row sample = sheet.createRow(1);
      sample.createCell(0).setCellValue(1);
      sample.createCell(1).setCellValue("示例");
      sample.createCell(2).setCellValue("example");
      sample.createCell(3).setCellValue("1");
      sample.createCell(4).setCellValue("1");
      sample.createCell(5).setCellValue("success");
      sample.createCell(6).setCellValue("广东省");
      sample.createCell(7).setCellValue("深圳市");
      sample.createCell(8).setCellValue("南山区");
      ExcelExportUtil.applyRowCellStyle(sample, headers.length, bodyStyle);

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
        int width = Math.min(Math.max(sheet.getColumnWidth(i) + 512, 12 * 256), 60 * 256);
        sheet.setColumnWidth(i, width);
      }

      ExcelExportUtil.writeXlsxToResponse(response, workbook, "dict_items_template.xlsx");
    } finally {
      try {
        workbook.close();
      } catch (Exception closeException) {
        log.debug("关闭字典模板工作簿失败", closeException);
      }
    }
  }

  private DictItemLocation requireItemLocation(long id) {
    DictItemLocation location = findItemLocation(id);
    if (location == null) {
      throw badRequest("Dictionary item not found.");
    }
    return location;
  }

  private DictItemLocation findItemLocation(long id) {
    List<SysDict> dicts = dictMapper.selectAll();
    if (dicts == null || dicts.isEmpty()) {
      return null;
    }

    for (SysDict dict : dicts) {
      if (dict == null || dict.getId() == null || !StringUtils.hasText(dict.getCode())) {
        continue;
      }
      try {
        SysDictItem item = itemTableService.selectById(dict, id);
        if (item != null) {
          return new DictItemLocation(dict, item);
        }
      } catch (Exception queryException) {
        log.warn("查询字典项位置失败，dictCode={}, itemId={}", dict.getCode(), id, queryException);
      }
    }
    return null;
  }

  private String normalizeCode(String code) {
    if (code == null) {
      throw badRequest("Dictionary code is required.");
    }
    String value = code.trim().toLowerCase(Locale.ROOT);
    if (value.isEmpty()) {
      throw badRequest("Dictionary code is required.");
    }
    if (!CODE_PATTERN.matcher(value).matches()) {
      throw badRequest("Dictionary code only allows letters, numbers, and underscore.");
    }
    return value;
  }

  private String normalizeValueType(String valueType) {
    if (!StringUtils.hasText(valueType)) {
      return "string";
    }
    String type = valueType.trim().toLowerCase(Locale.ROOT);
    return switch (type) {
      case "number", "int", "integer" -> "number";
      case "bool", "boolean" -> "boolean";
      default -> "string";
    };
  }

  private String inferValueTypeFromValue(String value) {
    if (!StringUtils.hasText(value)) {
      return "string";
    }
    String trimmed = value.trim();
    String lower = trimmed.toLowerCase(Locale.ROOT);
    if ("true".equals(lower) || "false".equals(lower)) {
      return "boolean";
    }
    if (trimmed.matches("[-+]?\\d+(\\.\\d+)?")) {
      return "number";
    }
    return "string";
  }

  private String normalizeText(String text) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    return text.trim();
  }

  private void ensureBuiltinDictionaryMetadata() {
    removeObsoleteDictionaryMetadata();
    LocalDateTime now = LocalDateTime.now();
    for (BuiltinDictSpec spec : BUILTIN_DICT_SPECS) {
      if (dictMapper.selectByCode(spec.code()) != null) {
        continue;
      }
      if (!itemTableService.dictTableExists(spec.code())) {
        continue;
      }
      SysDict dict = new SysDict();
      dict.setName(spec.name());
      dict.setCode(spec.code());
      dict.setStatus(1);
      dict.setSort(spec.sort());
      dict.setRemark(spec.remark());
      dict.setCreatedAt(now);
      dict.setUpdatedAt(now);
      dictMapper.insert(dict);
    }
  }

  private void removeObsoleteDictionaryMetadata() {
    SysDict obsolete = dictMapper.selectByCode(OBSOLETE_TEAM_CODE);
    if (obsolete == null || obsolete.getId() == null) {
      return;
    }
    try {
      itemTableService.dropDictTable(OBSOLETE_TEAM_CODE);
    } catch (Exception dropException) {
      log.warn("删除废弃字典表失败，dictCode={}", OBSOLETE_TEAM_CODE, dropException);
    }
    dictMapper.deleteById(obsolete.getId());
  }

  private void validateAreaItemFields(SysDict dict, String province, String city, String district) {
    if (dict == null || !StringUtils.hasText(dict.getCode())) {
      return;
    }
    if (!ADDRESS_DISTRICT_CODE.equalsIgnoreCase(dict.getCode())) {
      return;
    }
    if (!StringUtils.hasText(province) || !StringUtils.hasText(city) || !StringUtils.hasText(district)) {
      throw badRequest("address_district dictionary items require province/city/district.");
    }
  }

  private List<DictItemRow> readFromExcel(MultipartFile file) throws Exception {
    List<DictItemRow> rows = new ArrayList<>();
    DataFormatter formatter = new DataFormatter();
    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        return rows;
      }
      boolean headerSkipped = false;
      boolean hasSeqColumn = false;
      boolean hasValueTypeColumn = false;
      for (Row row : sheet) {
        String c0 = formatter.formatCellValue(row.getCell(0));
        String c1 = formatter.formatCellValue(row.getCell(1));
        String c2 = formatter.formatCellValue(row.getCell(2));
        String c3 = formatter.formatCellValue(row.getCell(3));
        if (!headerSkipped && isHeaderRow(c0, c1)) {
          headerSkipped = true;
          hasSeqColumn = containsSeqHeader(c0);
          String headerJoined = (c0 + "," + c1 + "," + c2 + "," + c3).toLowerCase(Locale.ROOT);
          hasValueTypeColumn = headerJoined.contains("value_type") || headerJoined.contains("数据值类型");
          continue;
        }
        int offset = hasSeqColumn ? 1 : 0;
        int valueTypeOffset = hasValueTypeColumn ? 1 : 0;
        String label = formatter.formatCellValue(row.getCell(offset));
        String value = formatter.formatCellValue(row.getCell(offset + 1));
        String status = formatter.formatCellValue(row.getCell(offset + 2 + valueTypeOffset));
        String sort = formatter.formatCellValue(row.getCell(offset + 3 + valueTypeOffset));
        String tagColor = formatter.formatCellValue(row.getCell(offset + 4 + valueTypeOffset));
        String province = formatter.formatCellValue(row.getCell(offset + 5 + valueTypeOffset));
        String city = formatter.formatCellValue(row.getCell(offset + 6 + valueTypeOffset));
        String district = formatter.formatCellValue(row.getCell(offset + 7 + valueTypeOffset));
        DictItemRow item = new DictItemRow();
        item.label = label;
        item.value = value;
        item.status = parseInteger(status);
        item.sort = parseInteger(sort);
        item.tagColor = StringUtils.hasText(tagColor) ? tagColor.trim() : null;
        item.province = normalizeText(province);
        item.city = normalizeText(city);
        item.district = normalizeText(district);
        if (isEmptyRow(item)) {
          continue;
        }
        rows.add(item);
      }
    }
    return rows;
  }

  private List<DictItemRow> readFromCsv(MultipartFile file) throws Exception {
    List<DictItemRow> rows = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      boolean headerSkipped = false;
      boolean hasSeqColumn = false;
      boolean hasValueTypeColumn = false;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] parts = line.split(",", -1);
        String label = parts.length > 0 ? parts[0].trim() : "";
        String value = parts.length > 1 ? parts[1].trim() : "";
        if (!headerSkipped && isHeaderRow(label, value)) {
          headerSkipped = true;
          hasSeqColumn = containsSeqHeader(label);
          String headerJoined = String.join(",", parts).toLowerCase(Locale.ROOT);
          hasValueTypeColumn = headerJoined.contains("value_type") || headerJoined.contains("数据值类型");
          continue;
        }
        int offset = hasSeqColumn ? 1 : 0;
        int valueTypeOffset = hasValueTypeColumn ? 1 : 0;
        DictItemRow item = new DictItemRow();
        item.label = parts.length > offset ? parts[offset].trim() : "";
        item.value = parts.length > offset + 1 ? parts[offset + 1].trim() : "";
        item.status = parts.length > offset + 2 + valueTypeOffset ? parseInteger(parts[offset + 2 + valueTypeOffset]) : null;
        item.sort = parts.length > offset + 3 + valueTypeOffset ? parseInteger(parts[offset + 3 + valueTypeOffset]) : null;
        item.tagColor = parts.length > offset + 4 + valueTypeOffset ? normalizeText(parts[offset + 4 + valueTypeOffset]) : null;
        item.province = parts.length > offset + 5 + valueTypeOffset ? normalizeText(parts[offset + 5 + valueTypeOffset]) : null;
        item.city = parts.length > offset + 6 + valueTypeOffset ? normalizeText(parts[offset + 6 + valueTypeOffset]) : null;
        item.district = parts.length > offset + 7 + valueTypeOffset ? normalizeText(parts[offset + 7 + valueTypeOffset]) : null;
        if (isEmptyRow(item)) {
          continue;
        }
        rows.add(item);
      }
    }
    return rows;
  }

  private boolean isHeaderRow(String label, String value) {
    String lower = (label + value).toLowerCase(Locale.ROOT);
    if (lower.contains("label") || lower.contains("name") || lower.contains("value")) return true;
    String normalized = (label + value).replace(" ", "");
    return normalized.contains("序号") || normalized.contains("名称") || normalized.contains("数据值") || normalized.contains("数据值类型");
  }

  private boolean containsSeqHeader(String cell) {
    if (!StringUtils.hasText(cell)) {
      return false;
    }
    String normalized = cell.replace(" ", "");
    String lower = normalized.toLowerCase(Locale.ROOT);
    return normalized.contains("序号") || lower.contains("index") || lower.contains("seq");
  }

  private Integer parseInteger(String input) {
    if (!StringUtils.hasText(input)) {
      return null;
    }
    try {
      return Integer.parseInt(input.trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private boolean isEmptyRow(DictItemRow row) {
    return !StringUtils.hasText(row.label) && !StringUtils.hasText(row.value);
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }

  private static class DictItemRow {
    private String label;
    private String value;
    private String valueType;
    private Integer status;
    private Integer sort;
    private String tagColor;
    private String province;
    private String city;
    private String district;
  }

  private static class DictItemLocation {
    private final SysDict dict;
    private final SysDictItem item;

    private DictItemLocation(SysDict dict, SysDictItem item) {
      this.dict = dict;
      this.item = item;
    }
  }

  private record BuiltinDictSpec(String code, String name, String remark, Integer sort) {}
}
