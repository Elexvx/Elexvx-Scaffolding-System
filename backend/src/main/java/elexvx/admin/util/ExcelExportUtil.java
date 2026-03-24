package elexvx.admin.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ExcelExportUtil {
  private ExcelExportUtil() {}

  public static CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);

    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
  }

  public static CellStyle createBodyStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setWrapText(true);
    return style;
  }

  public static Row writeHeaderRow(Sheet sheet, int rowIndex, String[] headers, CellStyle headerStyle) {
    Row row = sheet.createRow(rowIndex);
    row.setHeightInPoints(22f);
    for (int i = 0; i < headers.length; i++) {
      Cell cell = row.createCell(i);
      cell.setCellValue(headers[i]);
      if (headerStyle != null) {
        cell.setCellStyle(headerStyle);
      }
    }
    return row;
  }

  public static void applyRowCellStyle(Row row, int cellCount, CellStyle style) {
    if (row == null || style == null) return;
    for (int i = 0; i < cellCount; i++) {
      Cell cell = row.getCell(i);
      if (cell == null) {
        cell = row.createCell(i);
      }
      cell.setCellStyle(style);
    }
  }

  public static ResponseEntity<byte[]> toXlsxResponse(Workbook workbook, String fileName) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      workbook.write(out);
      byte[] body = out.toByteArray();
      String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.add(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded
      );
      return ResponseEntity.ok().headers(headers).body(body);
    } catch (Exception e) {
      throw new RuntimeException("生成Excel失败: " + e.getMessage());
    }
  }

  public static void writeXlsxToResponse(HttpServletResponse response, Workbook workbook, String fileName) {
    try {
      String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader(
        "Content-Disposition",
        "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded
      );
      workbook.write(response.getOutputStream());
    } catch (Exception e) {
      throw new RuntimeException("生成Excel失败: " + e.getMessage());
    }
  }
}

