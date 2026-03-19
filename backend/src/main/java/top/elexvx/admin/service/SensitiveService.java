package top.elexvx.admin.service;

import top.elexvx.admin.dto.SensitivePageSettingRequest;
import top.elexvx.admin.dto.SensitiveSettingsRequest;
import top.elexvx.admin.entity.SensitivePageSetting;
import top.elexvx.admin.entity.SensitiveSetting;
import top.elexvx.admin.entity.SensitiveWord;
import top.elexvx.admin.exception.BusinessException;
import top.elexvx.admin.exception.ErrorCodes;
import top.elexvx.admin.mapper.SensitivePageSettingMapper;
import top.elexvx.admin.mapper.SensitiveSettingMapper;
import top.elexvx.admin.mapper.SensitiveWordMapper;
import top.elexvx.admin.util.ExcelExportUtil;
import top.elexvx.admin.util.PermissionUtil;
import top.elexvx.admin.vo.PageResult;
import top.elexvx.admin.vo.SensitiveImportResult;
import top.elexvx.admin.vo.SensitiveSettingsResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

@Service
public class SensitiveService {
  private static final Logger log = LoggerFactory.getLogger(SensitiveService.class);
  private static final int MAX_WORD_LENGTH = 200;
  private static final int BATCH_SIZE = 1000;
  private static final int MAX_ERROR_MESSAGES = 50;
  private static final Set<String> SKIP_FIELD_KEYS = Set.of(
    "password",
    "passwd",
    "pwd",
    "email",
    "mail",
    "phone",
    "mobile",
    "tel",
    "idcard",
    "idno",
    "idnumber",
    "identity"
  );

  private final SensitiveWordMapper wordMapper;
  private final SensitivePageSettingMapper pageMapper;
  private final SensitiveSettingMapper settingMapper;
  private final OperationLogService operationLogService;
  private final JdbcTemplate jdbc;

  public SensitiveService(
    SensitiveWordMapper wordMapper,
    SensitivePageSettingMapper pageMapper,
    SensitiveSettingMapper settingMapper,
    OperationLogService operationLogService,
    JdbcTemplate jdbc
  ) {
    this.wordMapper = wordMapper;
    this.pageMapper = pageMapper;
    this.settingMapper = settingMapper;
    this.operationLogService = operationLogService;
    this.jdbc = jdbc;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public List<SensitiveWord> listWords(String keyword) {
    PermissionUtil.check("system:SystemSensitive:query");
    List<SensitiveWord> list = wordMapper.selectAll();
    if (!StringUtils.hasText(keyword)) {
      list.sort(Comparator.comparing(SensitiveWord::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
      return list;
    }
    String lower = keyword.trim().toLowerCase(Locale.ROOT);
    return list.stream()
      .filter(w -> w.getWord() != null && w.getWord().toLowerCase(Locale.ROOT).contains(lower))
      .sorted(Comparator.comparing(SensitiveWord::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
      .toList();
  }

    public PageResult<SensitiveWord> pageWords(String keyword, int page, int size) {
    PermissionUtil.check("system:SystemSensitive:query");
    int safePage = Math.max(page, 0);
    int safeSize = size <= 0 ? 10 : Math.min(size, 200);
    int offset = safePage * safeSize;
    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
    List<SensitiveWord> rows = wordMapper.selectPage(kw, offset, safeSize);
    long total = wordMapper.countByKeyword(kw);
    return new PageResult<>(rows, total);
  }


  public SensitiveWord createWord(String word) {
    PermissionUtil.check("system:SystemSensitive:create");
    String normalized = normalizeWord(word);
    if (!StringUtils.hasText(normalized)) {
      throw badRequest("Sensitive word cannot be empty.");
    }
    if (normalized.length() > MAX_WORD_LENGTH) {
      throw badRequest("Sensitive word length must be <= " + MAX_WORD_LENGTH);
    }
    SensitiveWord exists = wordMapper.selectByWord(normalized);
    if (exists != null) {
      throw badRequest("Sensitive word already exists.");
    }
    SensitiveWord w = new SensitiveWord();
    w.setWord(normalized);
    w.setEnabled(true);
    w.setCreatedAt(LocalDateTime.now());
    w.setUpdatedAt(LocalDateTime.now());
    wordMapper.insert(w);
    SensitiveWord saved = w;
    operationLogService.log("CREATE", "Sensitive words", "Created sensitive word " + normalized);
    return saved;
  }

  public boolean deleteWord(long id) {
    PermissionUtil.check("system:SystemSensitive:delete");
    SensitiveWord w = wordMapper.selectById(id);
    if (w == null) throw badRequest("Sensitive word not found.");
    wordMapper.deleteById(w.getId());
    operationLogService.log("DELETE", "Sensitive words", "Deleted sensitive word " + w.getWord());
    return true;
  }

  public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
    PermissionUtil.check("system:SystemSensitive:query");
    Workbook workbook = new XSSFWorkbook();
    try {
      Sheet sheet = workbook.createSheet("敏感词模板");
      var headerStyle = ExcelExportUtil.createHeaderStyle(workbook);
      var bodyStyle = ExcelExportUtil.createBodyStyle(workbook);
      String[] headers = new String[] { "敏感词" };
      ExcelExportUtil.writeHeaderRow(sheet, 0, headers, headerStyle);

      Row row1 = sheet.createRow(1);
      row1.createCell(0).setCellValue("示例敏感词1");
      ExcelExportUtil.applyRowCellStyle(row1, headers.length, bodyStyle);
      Row row2 = sheet.createRow(2);
      row2.createCell(0).setCellValue("示例敏感词2");
      ExcelExportUtil.applyRowCellStyle(row2, headers.length, bodyStyle);

      sheet.autoSizeColumn(0);
      int width = Math.min(Math.max(sheet.getColumnWidth(0) + 512, 18 * 256), 60 * 256);
      sheet.setColumnWidth(0, width);

      ExcelExportUtil.writeXlsxToResponse(response, workbook, "sensitive_words_template.xlsx");
    } finally {
      try {
        workbook.close();
      } catch (Exception closeException) {
        log.debug("关闭敏感词模板工作簿失败", closeException);
      }
    }
  }

  public SensitiveImportResult importWords(MultipartFile file) {
    PermissionUtil.check("system:SystemSensitive:create");
    if (file == null || file.isEmpty()) {
      throw badRequest("上传文件为空");
    }

    SensitiveImportResult result = new SensitiveImportResult();
    ImportStats stats = new ImportStats(result);
    WordBatchWriter writer = new WordBatchWriter(jdbc, stats);

    String name = file.getOriginalFilename();
    String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
    try {
      if (lower.endsWith(".xlsx")) {
        readFromXlsxStreaming(file, (index, value) -> handleWordRow(index, value, stats, writer));
      } else if (lower.endsWith(".xls")) {
        readFromXls(file, (index, value) -> handleWordRow(index, value, stats, writer));
      } else if (lower.endsWith(".csv") || lower.endsWith(".txt")) {
        readFromCsv(file, (index, value) -> handleWordRow(index, value, stats, writer));
      } else {
        throw badRequest("仅支持.xlsx/.xls/.csv/.txt 文件");
      }
    } catch (Exception e) {
      throw badRequest("读取敏感词文件失败: " + e.getMessage());
    }

    writer.flush();
    stats.applyToResult();
    operationLogService.log("IMPORT", "敏感词设置", "导入敏感词 " + stats.imported + " 条");
    return result;
  }

  public SensitiveSettingsResponse getSettings() {
    PermissionUtil.check("system:SystemSensitive:query");
    SensitiveSetting setting = getOrCreateSetting();
    List<SensitivePageSetting> pages = pageMapper.selectAll().stream()
      .sorted(Comparator.comparing(SensitivePageSetting::getPageKey, Comparator.nullsLast(String::compareTo)))
      .toList();
    return new SensitiveSettingsResponse(setting.isEnabled(), pages);
  }

  public SensitiveSettingsResponse saveSettings(SensitiveSettingsRequest req) {
    PermissionUtil.check("system:SystemSensitive:update");
    SensitiveSetting setting = getOrCreateSetting();
    LocalDateTime now = LocalDateTime.now();
    if (req.getEnabled() != null) {
      setting.setEnabled(req.getEnabled());
      setting.setUpdatedAt(now);
      if (setting.getId() == null) settingMapper.insert(setting);
      else settingMapper.update(setting);
    }

    if (req.getPages() != null) {
      for (SensitivePageSettingRequest page : req.getPages()) {
        String pageKey = normalizePageKey(page.getPageKey());
        if (!StringUtils.hasText(pageKey)) {
          continue;
        }
        SensitivePageSetting target =
          pageMapper.selectByPageKey(pageKey);
        if (target == null) target = new SensitivePageSetting();
        if (target.getId() == null) {
          target.setPageKey(pageKey);
          target.setCreatedAt(now);
        }
        if (page.getPageName() != null) {
          target.setPageName(page.getPageName().trim());
        }
        if (page.getEnabled() != null) {
          target.setEnabled(page.getEnabled());
        }
        target.setUpdatedAt(now);
        if (target.getId() == null) pageMapper.insert(target);
        else pageMapper.update(target);
      }
    }

    operationLogService.log("UPDATE", "敏感词设置", "更新敏感词拦截配置");
    return getSettings();
  }

  public boolean isPageEnabled(String pageKey) {
    SensitiveSetting setting = getOrCreateSetting();
    boolean globalEnabled = setting.isEnabled();

    // If pageKey is missing (e.g. API call) or empty, fallback to global switch.
    if (!StringUtils.hasText(pageKey)) return globalEnabled;

    String normalized = normalizePageKey(pageKey);
    if (!StringUtils.hasText(normalized)) return globalEnabled;

    SensitivePageSetting matched = pageMapper.selectByPageKey(normalized);
    if (matched == null) {
      String alt = normalized.startsWith("/") ? normalized.substring(1) : "/" + normalized;
      matched = pageMapper.selectByPageKey(alt);
    }

    // If page setting exists, it overrides the global default.
    return matched != null ? matched.isEnabled() : globalEnabled;
  }

  public SensitiveHit findHit(Object payload) {
    if (payload == null) return null;
    List<String> words = wordMapper.selectAllEnabled().stream()
      .map(SensitiveWord::getWord)
      .filter(StringUtils::hasText)
      .map(w -> w.toLowerCase(Locale.ROOT))
      .toList();
    if (words.isEmpty()) return null;
    return findHit(payload, "", words);
  }

  private SensitiveHit findHit(Object payload, String path, List<String> words) {
    if (payload == null) return null;
    if (payload instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey());
        if (!StringUtils.hasText(key)) continue;
        if (shouldSkipField(key)) {
          continue;
        }
        String nextPath = path.isEmpty() ? key : path + "." + key;
        SensitiveHit hit = findHit(entry.getValue(), nextPath, words);
        if (hit != null) return hit;
      }
      return null;
    }
    if (payload instanceof List<?> list) {
      for (int i = 0; i < list.size(); i++) {
        String nextPath = path + "[" + i + "]";
        SensitiveHit hit = findHit(list.get(i), nextPath, words);
        if (hit != null) return hit;
      }
      return null;
    }
    if (payload instanceof String text) {
      String lower = text.toLowerCase(Locale.ROOT);
      for (String word : words) {
        if (lower.contains(word)) {
          return new SensitiveHit(path, word);
        }
      }
    }
    return null;
  }

  private boolean shouldSkipField(String key) {
    String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    if (!StringUtils.hasText(normalized)) return false;
    for (String skip : SKIP_FIELD_KEYS) {
      if (normalized.contains(skip)) return true;
    }
    return false;
  }

    private SensitiveSetting getOrCreateSetting() {
    SensitiveSetting setting = settingMapper.selectTop();
    if (setting == null) {
      setting = new SensitiveSetting();
      setting.setEnabled(false);
      setting.setUpdatedAt(LocalDateTime.now());
      settingMapper.insert(setting);
    }
    return setting;
  }


  private String normalizeWord(String word) {
    if (word == null) return "";
    return word.trim();
  }

  private String normalizePageKey(String pageKey) {
    if (pageKey == null) return "";
    String key = pageKey.trim();
    if (key.isEmpty()) return "";
    int queryIdx = key.indexOf('?');
    if (queryIdx >= 0) {
      key = key.substring(0, queryIdx);
    }
    int hashIdx = key.indexOf('#');
    if (hashIdx >= 0) {
      key = key.substring(0, hashIdx);
    }
    if (key.length() > 1 && key.endsWith("/")) {
      key = key.substring(0, key.length() - 1);
    }
    if (!key.startsWith("/") && key.contains("/")) {
      key = "/" + key;
    }
    return key.trim();
  }

  private void handleWordRow(int index, String value, ImportStats stats, WordBatchWriter writer) {
    String normalized = normalizeWord(value);
    if (!StringUtils.hasText(normalized)) {
      return;
    }
    if (index == 1) {
      String header = normalized.replace(" ", "");
      if ("Sensitiveword".equalsIgnoreCase(header) || "敏感词".equals(header)) {
        return;
      }
    }
    stats.total++;
    if (normalized.length() > MAX_WORD_LENGTH) {
      stats.failed++;
      stats.addError("第" + index + " 行敏感词过长");
      return;
    }
    writer.add(normalized);
  }

  private void readFromCsv(MultipartFile file, WordConsumer consumer) throws Exception {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      int index = 0;
      while ((line = reader.readLine()) != null) {
        index++;
        String value = line;
        if (line.contains(",")) {
          value = line.split(",", 2)[0];
        } else if (line.contains("\t")) {
          value = line.split("\t", 2)[0];
        }
        consumer.accept(index, value);
      }
    }
  }

  private void readFromXls(MultipartFile file, WordConsumer consumer) throws Exception {
    DataFormatter formatter = new DataFormatter();
    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        return;
      }
      for (Row row : sheet) {
        int index = row.getRowNum() + 1;
        Cell cell = row.getCell(0);
        String value = cell == null ? "" : formatter.formatCellValue(cell);
        consumer.accept(index, value);
      }
    }
  }

  private void readFromXlsxStreaming(MultipartFile file, WordConsumer consumer) throws Exception {
    try (InputStream in = file.getInputStream(); OPCPackage pkg = OPCPackage.open(in)) {
      XSSFReader reader = new XSSFReader(pkg);
      StylesTable styles = reader.getStylesTable();
      ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
      java.util.Iterator<InputStream> sheets = reader.getSheetsData();
      if (!sheets.hasNext()) {
        return;
      }
      try (InputStream sheet = sheets.next()) {
        DataFormatter formatter = new DataFormatter();
        XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, new XlsxHandler(consumer), formatter, false);
        parser.setContentHandler(handler);
        parser.parse(new InputSource(sheet));
      }
    }
  }

  private static class XlsxHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
    private final WordConsumer consumer;
    private int rowIndex = 0;
    private String firstCellValue = "";

    private XlsxHandler(WordConsumer consumer) {
      this.consumer = consumer;
    }

    @Override
    public void startRow(int rowNum) {
      rowIndex = rowNum + 1;
      firstCellValue = "";
    }

    @Override
    public void endRow(int rowNum) {
      consumer.accept(rowIndex, firstCellValue);
    }

    @Override
    public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
      if (cellReference == null) return;
      CellReference ref = new CellReference(cellReference);
      if (ref.getCol() == 0) {
        firstCellValue = formattedValue;
      }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {}
  }

  @FunctionalInterface
  private interface WordConsumer {
    void accept(int index, String value);
  }

  private static class ImportStats {
    private final SensitiveImportResult result;
    private int total;
    private int imported;
    private int skipped;
    private int failed;

    private ImportStats(SensitiveImportResult result) {
      this.result = result;
    }

    private void addError(String message) {
      if (message == null || message.isBlank()) return;
      if (result.getErrors().size() >= MAX_ERROR_MESSAGES) return;
      result.addError(message);
    }

    private void applyToResult() {
      result.setTotal(total);
      result.setImported(imported);
      result.setSkipped(skipped);
      result.setFailed(failed);
    }
  }

  private static class WordBatchWriter {
    private final JdbcTemplate jdbc;
    private final ImportStats stats;
    private final List<String> batch = new ArrayList<>();
    private final Set<String> batchDedup = new HashSet<>();

    private WordBatchWriter(JdbcTemplate jdbc, ImportStats stats) {
      this.jdbc = jdbc;
      this.stats = stats;
    }

    private void add(String word) {
      String lower = word.toLowerCase(Locale.ROOT);
      if (!batchDedup.add(lower)) {
        stats.skipped++;
        return;
      }
      batch.add(word);
      if (batch.size() >= BATCH_SIZE) {
        flush();
      }
    }

    private void flush() {
      if (batch.isEmpty()) return;
      LocalDateTime now = LocalDateTime.now();
      List<String> batchSnapshot = List.copyOf(batch);
      int[][] results = jdbc.batchUpdate(
        "INSERT IGNORE INTO sensitive_words (word, enabled, created_at, updated_at) VALUES (?, ?, ?, ?)",
        batchSnapshot,
        batchSnapshot.size(),
        (ps, word) -> {
          ps.setString(1, word);
          ps.setBoolean(2, true);
          ps.setObject(3, now);
          ps.setObject(4, now);
        }
      );
      for (int[] group : results) {
        for (int count : group) {
          if (count > 0) stats.imported++;
          else stats.skipped++;
        }
      }
      batch.clear();
      batchDedup.clear();
    }
  }

  public record SensitiveHit(String fieldPath, String word) {}

}
