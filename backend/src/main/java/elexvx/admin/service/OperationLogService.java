package elexvx.admin.service;

import elexvx.admin.entity.OperationLogEntity;
import elexvx.admin.entity.UserEntity;
import elexvx.admin.mapper.OperationLogMapper;
import elexvx.admin.security.AuthContext;
import elexvx.admin.security.AuthSession;
import elexvx.admin.vo.OperationLogVO;
import elexvx.admin.vo.PageResult;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final long CLEANUP_INTERVAL_SECONDS = 60 * 60L;
  private static final long CLEANUP_INITIAL_DELAY_SECONDS = 300L;

  private final OperationLogMapper mapper;
  private final UiSettingService uiSettingService;
  private final HttpServletRequest request;
  private final AuthContext authContext;
  private final AuthTokenService authTokenService;
  private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor(r -> {
    Thread thread = new Thread(r, "operation-log-writer");
    thread.setDaemon(true);
    return thread;
  });
  private final java.util.concurrent.ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "operation-log-cleaner");
    thread.setDaemon(true);
    return thread;
  });

  public OperationLogService(
    OperationLogMapper mapper,
    UiSettingService uiSettingService,
    HttpServletRequest request,
    AuthContext authContext,
    AuthTokenService authTokenService
  ) {
    this.mapper = mapper;
    this.uiSettingService = uiSettingService;
    this.request = request;
    this.authContext = authContext;
    this.authTokenService = authTokenService;
    cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredLogs, CLEANUP_INITIAL_DELAY_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
  }

  public void logLogin(UserEntity user, String deviceModel, String os, String browser, String ipAddress) {
    OperationLogEntity e = new OperationLogEntity();
    e.setAction("LOGIN");
    e.setModule("登录");
    e.setDetail("用户登录");
    e.setUserId(user.getId());
    e.setUserGuid(user.getGuid());
    e.setAccount(user.getAccount());
    e.setIpAddress(ipAddress);
    e.setDeviceModel(deviceModel);
    e.setOs(os);
    e.setBrowser(browser);
    e.setCreatedAt(LocalDateTime.now());
    asyncInsert(e);
  }

  public void log(String action, String module, String detail) {
    OperationLogEntity e = new OperationLogEntity();
    e.setAction(action);
    e.setModule(module);
    e.setDetail(detail);

    if (authContext.isAuthenticated()) {
      Long userId = authContext.requireUserId();
      e.setUserId(userId);
      String token = authContext.getToken();
      AuthSession session = token == null ? null : authTokenService.getSession(token);
      if (session != null) {
        Object account = session.getAttributes().get("account");
        Object userGuid = session.getAttributes().get("userGuid");
        if (account != null) {
          e.setAccount(String.valueOf(account));
        }
        if (userGuid != null) {
          e.setUserGuid(String.valueOf(userGuid));
        }
        e.setDeviceModel(session.getDeviceModel());
        e.setOs(session.getOs());
        e.setBrowser(session.getBrowser());
      }
    }

    e.setIpAddress(resolveClientIp());
    e.setCreatedAt(LocalDateTime.now());
    asyncInsert(e);
  }

  public PageResult<OperationLogVO> page(
    String keyword,
    String action,
    LocalDate start,
    LocalDate end,
    Long userId,
    int page,
    int size
  ) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String safeKeyword = normalizeKeyword(keyword);
    String safeAction = normalizeAction(action);
    LocalDateTime startTime = start == null ? null : start.atStartOfDay();
    LocalDateTime endTime = end == null ? null : end.atTime(LocalTime.MAX);

    List<OperationLogEntity> rows = mapper.selectPage(safeKeyword, safeAction, startTime, endTime, userId, offset, safeSize);
    long total = mapper.count(safeKeyword, safeAction, startTime, endTime, userId);
    List<OperationLogVO> list = new ArrayList<>();
    for (OperationLogEntity e : rows) {
      list.add(toVo(e));
    }
    return new PageResult<>(list, total);
  }

  public List<OperationLogVO> listAll(String keyword, String action, LocalDate start, LocalDate end, Long userId) {
    String safeKeyword = normalizeKeyword(keyword);
    String safeAction = normalizeAction(action);
    LocalDateTime startTime = start == null ? null : start.atStartOfDay();
    LocalDateTime endTime = end == null ? null : end.atTime(LocalTime.MAX);
    List<OperationLogEntity> list = mapper.selectAll(safeKeyword, safeAction, startTime, endTime, userId);
    List<OperationLogVO> out = new ArrayList<>();
    for (OperationLogEntity e : list) {
      out.add(toVo(e));
    }
    return out;
  }

  private String normalizeKeyword(String keyword) {
    if (keyword == null) return null;
    String trimmed = keyword.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String normalizeAction(String action) {
    if (action == null) return null;
    String trimmed = action.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private OperationLogVO toVo(OperationLogEntity e) {
    OperationLogVO vo = new OperationLogVO();
    vo.setId(e.getId());
    vo.setAction(e.getAction());
    vo.setModule(e.getModule());
    vo.setDetail(e.getDetail());
    vo.setAccount(e.getAccount());
    vo.setUserGuid(e.getUserGuid());
    vo.setIpAddress(e.getIpAddress());
    vo.setDeviceInfo(buildDeviceInfo(e.getDeviceModel(), e.getOs(), e.getBrowser()));
    vo.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt().format(TIME_FORMAT) : "");
    return vo;
  }

  private String buildDeviceInfo(String deviceModel, String os, String browser) {
    String safeDevice = (deviceModel == null || deviceModel.isBlank()) ? "未知设备" : deviceModel;
    String safeOs = (os == null || os.isBlank()) ? "未知系统" : os;
    String safeBrowser = (browser == null || browser.isBlank()) ? "未知浏览器" : browser;
    return safeDevice + " / " + safeOs + " / " + safeBrowser;
  }

  private String resolveClientIp() {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }
    return ip;
  }

  private void cleanupExpiredLogs() {
    int retentionDays = getRetentionDays();
    if (retentionDays <= 0) return;
    LocalDateTime threshold = LocalDate.now().minusDays(retentionDays).atStartOfDay();
    try {
      mapper.deleteBefore(threshold);
    } catch (Exception ignored) {
    }
  }

  private int getRetentionDays() {
    Integer value = uiSettingService.getLogRetentionDays();
    return value == null || value <= 0 ? 90 : value;
  }

  private void asyncInsert(OperationLogEntity logEntity) {
    writeExecutor.execute(() -> {
      try {
        mapper.insert(logEntity);
      } catch (Exception ignored) {
      }
    });
  }

  @PreDestroy
  public void shutdownExecutors() {
    writeExecutor.shutdown();
    cleanupExecutor.shutdownNow();
  }
}
