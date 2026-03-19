package elexvx.admin.controller;

import elexvx.admin.service.OperationLogService;
import elexvx.admin.security.AccessControlService;
import elexvx.admin.security.AuthContext;
import elexvx.admin.util.ExcelExportUtil;
import elexvx.admin.util.PermissionUtil;
import elexvx.admin.vo.ApiResponse;
import elexvx.admin.vo.OperationLogVO;
import elexvx.admin.vo.PageResult;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@RestController
@RequestMapping("/system/log")
public class OperationLogController {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Logger log = LoggerFactory.getLogger(OperationLogController.class);
  private final OperationLogService service;
  private final AuthContext authContext;
  private final AccessControlService accessControlService;

  public OperationLogController(
    OperationLogService service,
    AuthContext authContext,
    AccessControlService accessControlService
  ) {
    this.service = service;
    this.authContext = authContext;
    this.accessControlService = accessControlService;
  }

  @GetMapping("/page")
  public ApiResponse<PageResult<OperationLogVO>> page(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String start,
    @RequestParam(required = false) String end,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    PermissionUtil.check("system:SystemLog:query");
    LocalDate startDate = parseDate(start);
    LocalDate endDate = parseDate(end);
    Long userId = accessControlService.hasRole("admin") ? null : authContext.requireUserId();
    return ApiResponse.success(service.page(keyword, action, startDate, endDate, userId, page, size));
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> export(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String start,
    @RequestParam(required = false) String end
  ) {
    PermissionUtil.check("system:SystemLog:query");
    LocalDate startDate = parseDate(start);
    LocalDate endDate = parseDate(end);
    Long userId = accessControlService.hasRole("admin") ? null : authContext.requireUserId();
    List<OperationLogVO> list = service.listAll(keyword, action, startDate, endDate, userId);
    Workbook workbook = new XSSFWorkbook();
    try {
      Sheet sheet = workbook.createSheet("操作日志");
      var headerStyle = ExcelExportUtil.createHeaderStyle(workbook);
      var bodyStyle = ExcelExportUtil.createBodyStyle(workbook);
      String[] headers = new String[] { "序号", "操作类型", "模块", "详情", "账号", "IP", "设备信息", "时间" };
      ExcelExportUtil.writeHeaderRow(sheet, 0, headers, headerStyle);

      int rowIndex = 1;
      for (OperationLogVO row : list) {
        Row r = sheet.createRow(rowIndex++);
        r.createCell(0).setCellValue(rowIndex - 1);
        r.createCell(1).setCellValue(row.getAction() == null ? "" : row.getAction());
        r.createCell(2).setCellValue(row.getModule() == null ? "" : row.getModule());
        r.createCell(3).setCellValue(row.getDetail() == null ? "" : row.getDetail());
        r.createCell(4).setCellValue(row.getAccount() == null ? "" : row.getAccount());
        r.createCell(5).setCellValue(row.getIpAddress() == null ? "" : row.getIpAddress());
        r.createCell(6).setCellValue(row.getDeviceInfo() == null ? "" : row.getDeviceInfo());
        r.createCell(7).setCellValue(row.getCreatedAt() == null ? "" : String.valueOf(row.getCreatedAt()));
        ExcelExportUtil.applyRowCellStyle(r, headers.length, bodyStyle);
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
        int width = Math.min(Math.max(sheet.getColumnWidth(i) + 512, 12 * 256), 80 * 256);
        sheet.setColumnWidth(i, width);
      }

      String fileName = "operation-logs-" + LocalDate.now().format(DATE_FORMAT) + ".xlsx";
      return ExcelExportUtil.toXlsxResponse(workbook, fileName);
    } finally {
      try {
        workbook.close();
      } catch (Exception closeException) {
        log.debug("关闭操作日志导出工作簿失败", closeException);
      }
    }
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) return null;
    return LocalDate.parse(value, DATE_FORMAT);
  }

  
}
