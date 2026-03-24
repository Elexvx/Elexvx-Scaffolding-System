package elexvx.admin.service;

import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.vo.ConcurrentLoginDecisionEvent;
import elexvx.admin.vo.ConcurrentLoginEvent;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
/**
 * 并发登录确认协调器（基于 SSE）。
 *
 * <p>用于在“禁止多端登录”场景下，将新登录请求推送给在线设备，由在线设备批准/拒绝后再继续登录流程。
 *
 * <p>实现要点：
 * <ul>
 *   <li>loginId -> emitters：同一账号可存在多个在线监听端（多浏览器/多标签页）。</li>
 *   <li>pendingLogins：保存等待确认的登录请求，带 TTL；超时自动视为失效。</li>
 *   <li>心跳：定期发送 ping，尽快清理断开的 SSE 连接，避免内存泄漏。</li>
 * </ul>
 */
public class ConcurrentLoginService {
  private static final Logger log = LoggerFactory.getLogger(ConcurrentLoginService.class);
  public static final long PENDING_TTL_MS = 2 * 60 * 1000L;
  private static final long LOGIN_HEARTBEAT_INTERVAL_SECONDS = 15L;
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final Map<Long, CopyOnWriteArrayList<SseEmitter>> loginEmitters = new ConcurrentHashMap<>();
  private final Map<SseEmitter, ScheduledFuture<?>> loginHeartbeatTasks = new ConcurrentHashMap<>();
  private final Cache<String, PendingLogin> pendingLogins;
  private final Cache<String, String> concurrentStreamTickets;
  private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "concurrent-login-sse-heartbeat");
    thread.setDaemon(true);
    return thread;
  });


  public ConcurrentLoginService(VerificationCacheService verificationCacheService) {
    this.pendingLogins = verificationCacheService.pendingLoginCache();
    this.concurrentStreamTickets = verificationCacheService.concurrentStreamTicketCache();
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  @PreDestroy
  /**
   * 应用关闭时停止心跳线程，避免容器关闭阶段出现非预期阻塞。
   */
  public void shutdownHeartbeatExecutor() {
    heartbeatExecutor.shutdownNow();
  }

  /**
   * 订阅“账号发起并发登录”通知流。
   *
   * <p>该订阅通常由“已登录设备”建立，用于接收其他设备的登录确认请求（event: concurrent-login）。
   */
  public SseEmitter subscribeLoginNotice(long loginId) {
    SseEmitter emitter = new SseEmitter(0L);
    loginEmitters.computeIfAbsent(loginId, key -> new CopyOnWriteArrayList<>()).add(emitter);
    emitter.onCompletion(() -> removeLoginEmitter(loginId, emitter));
    emitter.onTimeout(() -> removeLoginEmitter(loginId, emitter));
    emitter.onError((ex) -> removeLoginEmitter(loginId, emitter));
    startLoginHeartbeat(loginId, emitter);
    sendConnectedEvent(loginId, emitter);
    return emitter;
  }

  public boolean hasActiveSubscriber(long loginId) {
    List<SseEmitter> emitters = loginEmitters.get(loginId);
    return emitters != null && !emitters.isEmpty();
  }

  /**
   * 主动通知在线端强制下线。
   *
   * <p>用于旧会话被撤销、Token 过期等需要前端立即感知的场景。
   */
  public void publishForceLogout(long loginId, String message) {
    List<SseEmitter> emitters = loginEmitters.get(loginId);
    if (emitters == null || emitters.isEmpty()) return;
    String text = (message == null || message.isBlank()) ? "当前登录状态失效，请重新登录" : message;
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("force-logout").data(Map.of("message", text)));
        removeLoginEmitter(loginId, emitter);
        emitter.complete();
      } catch (Exception ex) {
        removeLoginEmitter(loginId, emitter);
      }
    }
  }

  /**
   * 创建等待确认的登录请求并推送给在线设备。
   *
   * <p>若推送失败（例如 SSE 断开），不会阻塞登录流程；由上层决定是否降级为直接撤销旧会话。
   */
  public PendingLogin createPending(
    long loginId,
    String deviceModel,
    String os,
    String browser,
    String deviceInfo,
    String ipAddress,
    String loginLocation
  ) {
    String requestId = UUID.randomUUID().toString();
    String requestKey = UUID.randomUUID().toString().replace("-", "");
    PendingLogin pending = new PendingLogin(
      requestId,
      requestKey,
      loginId,
      deviceModel,
      os,
      browser,
      deviceInfo,
      ipAddress,
      loginLocation,
      System.currentTimeMillis()
    );
    pendingLogins.put(requestId, pending);
    try {
      publishLoginNotice(loginId, pending);
    } catch (Exception ex) {
      log.debug("推送并发登录通知失败，保持登录流程继续，loginId={}", loginId, ex);
    }
    return pending;
  }

  /**
   * 订阅单次“登录确认决策”结果流。
   *
   * <p>该订阅通常由“发起登录的设备”建立，等待在线设备给出 decision（approved/rejected）。
   */
  public SseEmitter subscribeDecision(String requestId, String requestKey) {
    PendingLogin pending = getPending(requestId);
    if (pending == null || !pending.requestKey.equals(requestKey)) {
      throw badRequest("等待确认已失效，请重新登录");
    }
    if (pending.isExpired()) {
      pendingLogins.invalidate(requestId);
      throw badRequest("等待确认已过期，请重新登录");
    }

    SseEmitter emitter = new SseEmitter(PENDING_TTL_MS);
    pending.decisionEmitters.add(emitter);
    emitter.onCompletion(() -> pending.decisionEmitters.remove(emitter));
    emitter.onTimeout(() -> pending.decisionEmitters.remove(emitter));
    emitter.onError((ex) -> pending.decisionEmitters.remove(emitter));

    if (pending.decision != Decision.PENDING) {
      sendDecisionEvent(emitter, pending.decision);
    }
    return emitter;
  }

  /**
   * 在线设备对登录请求作出批准/拒绝决策。
   */
  public PendingLogin decide(long loginId, String requestId, boolean approve) {
    PendingLogin pending = getPending(requestId);
    if (pending == null) {
      throw badRequest("等待确认已失效，请重新登录");
    }
    if (pending.loginId != loginId) {
      throw badRequest("无权处理该登录请求");
    }
    if (pending.isExpired()) {
      pendingLogins.invalidate(requestId);
      throw badRequest("等待确认已过期，请重新登录");
    }
    if (pending.decision != Decision.PENDING) {
      return pending;
    }
    pending.decision = approve ? Decision.APPROVED : Decision.REJECTED;
    notifyDecision(pending);
    return pending;
  }

  /**
   * 消费已批准的登录请求（一次性）。
   *
   * <p>调用成功后会移除 pending 记录，避免重复使用同一个 requestId 继续登录。
   */
  public PendingLogin consumeApproved(String requestId, String requestKey) {
    PendingLogin pending = getPending(requestId);
    if (pending == null || !pending.requestKey.equals(requestKey)) {
      throw badRequest("等待确认已失效，请重新登录");
    }
    if (pending.isExpired()) {
      pendingLogins.invalidate(requestId);
      throw badRequest("等待确认已过期，请重新登录");
    }
    if (pending.decision == Decision.REJECTED) {
      throw badRequest("该次登录已被拒绝");
    }
    if (pending.decision != Decision.APPROVED) {
      throw badRequest("等待对方确认中");
    }
    pendingLogins.invalidate(requestId);
    return pending;
  }

  public String issueConcurrentStreamTicket(String token) {
    if (token == null || token.isBlank()) {
      throw badRequest("登录态已失效，请重新登录");
    }
    String ticket = UUID.randomUUID().toString().replace("-", "");
    concurrentStreamTickets.put(ticket, token);
    return ticket;
  }

  public String consumeConcurrentStreamToken(String ticket) {
    if (ticket == null || ticket.isBlank()) {
      return null;
    }
    String token = concurrentStreamTickets.getIfPresent(ticket);
    if (token == null) {
      return null;
    }
    concurrentStreamTickets.invalidate(ticket);
    return token;
  }

  private void publishLoginNotice(long loginId, PendingLogin pending) {
    List<SseEmitter> emitters = loginEmitters.get(loginId);
    if (emitters == null || emitters.isEmpty()) return;
    ConcurrentLoginEvent event = new ConcurrentLoginEvent(
      pending.requestId,
      pending.deviceInfo,
      pending.ipAddress,
      pending.loginLocation,
      LocalDateTime.now().format(TIME_FORMAT)
    );
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("concurrent-login").data(event));
      } catch (Exception ex) {
        removeLoginEmitter(loginId, emitter);
      }
    }
  }

  private void notifyDecision(PendingLogin pending) {
    if (pending.decisionEmitters.isEmpty()) return;
    for (SseEmitter emitter : pending.decisionEmitters) {
      sendDecisionEvent(emitter, pending.decision);
    }
    pending.decisionEmitters.clear();
  }

  private void sendDecisionEvent(SseEmitter emitter, Decision decision) {
    try {
      String status = decision == Decision.APPROVED ? "approved" : "rejected";
      emitter.send(SseEmitter.event().name("decision").data(new ConcurrentLoginDecisionEvent(status)));
      emitter.complete();
    } catch (Exception ex) {
      log.debug("发送并发登录决策事件失败，decision={}", decision, ex);
      emitter.completeWithError(ex);
    }
  }

  private void removeLoginEmitter(long loginId, SseEmitter emitter) {
    cancelLoginHeartbeat(emitter);
    CopyOnWriteArrayList<SseEmitter> list = loginEmitters.get(loginId);
    if (list == null) return;
    list.remove(emitter);
    if (list.isEmpty()) {
      loginEmitters.remove(loginId);
    }
  }

  private void sendConnectedEvent(long loginId, SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().name("connected").data(Map.of("ts", System.currentTimeMillis())));
    } catch (Exception ex) {
      removeLoginEmitter(loginId, emitter);
    }
  }

  private void startLoginHeartbeat(long loginId, SseEmitter emitter) {
    cancelLoginHeartbeat(emitter);
    ScheduledFuture<?> task = heartbeatExecutor.scheduleAtFixedRate(
      () -> {
        try {
          emitter.send(SseEmitter.event().name("ping").data(Map.of("ts", System.currentTimeMillis())));
        } catch (Exception ex) {
          removeLoginEmitter(loginId, emitter);
        }
      },
      LOGIN_HEARTBEAT_INTERVAL_SECONDS,
      LOGIN_HEARTBEAT_INTERVAL_SECONDS,
      TimeUnit.SECONDS
    );
    loginHeartbeatTasks.put(emitter, task);
  }

  private void cancelLoginHeartbeat(SseEmitter emitter) {
    ScheduledFuture<?> task = loginHeartbeatTasks.remove(emitter);
    if (task != null) {
      task.cancel(true);
    }
  }

  private PendingLogin getPending(String requestId) {
    return pendingLogins.getIfPresent(requestId);
  }

  public static class PendingLogin {
    private final String requestId;
    private final String requestKey;
    private final long loginId;
    private final String deviceModel;
    private final String os;
    private final String browser;
    private final String deviceInfo;
    private final String ipAddress;
    private final String loginLocation;
    private final long createdAt;
    private volatile Decision decision = Decision.PENDING;
    private final CopyOnWriteArrayList<SseEmitter> decisionEmitters = new CopyOnWriteArrayList<>();

    public PendingLogin(
      String requestId,
      String requestKey,
      long loginId,
      String deviceModel,
      String os,
      String browser,
      String deviceInfo,
      String ipAddress,
      String loginLocation,
      long createdAt
    ) {
      this.requestId = requestId;
      this.requestKey = requestKey;
      this.loginId = loginId;
      this.deviceModel = deviceModel;
      this.os = os;
      this.browser = browser;
      this.deviceInfo = deviceInfo;
      this.ipAddress = ipAddress;
      this.loginLocation = loginLocation;
      this.createdAt = createdAt;
    }

    public String getRequestId() {
      return requestId;
    }

    public String getRequestKey() {
      return requestKey;
    }

    public long getLoginId() {
      return loginId;
    }

    public String getDeviceModel() {
      return deviceModel;
    }

    public String getOs() {
      return os;
    }

    public String getBrowser() {
      return browser;
    }

    public String getDeviceInfo() {
      return deviceInfo;
    }

    public String getIpAddress() {
      return ipAddress;
    }

    public String getLoginLocation() {
      return loginLocation;
    }

    public boolean isExpired() {
      return System.currentTimeMillis() - createdAt > PENDING_TTL_MS;
    }
  }

  private enum Decision {
    PENDING,
    APPROVED,
    REJECTED
  }
}
