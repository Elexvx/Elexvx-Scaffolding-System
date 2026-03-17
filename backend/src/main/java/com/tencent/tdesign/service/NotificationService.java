package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.NotificationUpsertRequest;
import com.tencent.tdesign.entity.Notification;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.mapper.NotificationMapper;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.socket.NettySocketService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.NotificationResponse;
import com.tencent.tdesign.vo.NotificationSummary;
import com.tencent.tdesign.vo.PageResult;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {
  private static final String DEFAULT_STATUS = "draft";
  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
  private final NotificationMapper mapper;
  private final MessageService messageService;
  private final OperationLogService operationLogService;
  private final Optional<NettySocketService> nettySocketService;
  private final AuthContext authContext;

  public NotificationService(
    NotificationMapper mapper,
    MessageService messageService,
    OperationLogService operationLogService,
    Optional<NettySocketService> nettySocketService,
    AuthContext authContext
  ) {
    this.mapper = mapper;
    this.messageService = messageService;
    this.operationLogService = operationLogService;
    this.nettySocketService = nettySocketService;
    this.authContext = authContext;
  }

  @Transactional
  public NotificationResponse create(NotificationUpsertRequest req) {
    PermissionUtil.check("system:NotificationTable:create");
    Notification n = new Notification();
    apply(n, req);
    n.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus() : DEFAULT_STATUS);
    n.setCreatedAt(LocalDateTime.now());
    n.setUpdatedAt(LocalDateTime.now());
    long userId = authContext.requireUserId();
    n.setCreatedById(userId);
    n.setCreatedByName(String.valueOf(userId));
    if ("published".equalsIgnoreCase(n.getStatus())) {
      n.setPublishAt(LocalDateTime.now());
    }
    Notification saved = saveNotification(n);
    if ("published".equalsIgnoreCase(saved.getStatus())) {
      broadcast(saved);
    }
    operationLogService.log("CREATE", "\u901a\u77e5\u7ba1\u7406", "\u521b\u5efa\u901a\u77e5: " + saved.getTitle());
    return NotificationResponse.from(saved);
  }

  @Transactional
  public NotificationResponse update(Long id, NotificationUpsertRequest req) {
    PermissionUtil.check("system:NotificationTable:update");
    Notification n = Optional.ofNullable(mapper.selectById(id))
      .orElseThrow(() -> badRequest("\u901a\u77e5\u4e0d\u5b58\u5728"));
    apply(n, req);
    if (StringUtils.hasText(req.getStatus())) {
      n.setStatus(req.getStatus());
    }
    if ("published".equalsIgnoreCase(n.getStatus()) && n.getPublishAt() == null) {
      n.setPublishAt(LocalDateTime.now());
    }
    n.setUpdatedAt(LocalDateTime.now());
    Notification saved = saveNotification(n);
    operationLogService.log("UPDATE", "\u901a\u77e5\u7ba1\u7406", "\u66f4\u65b0\u901a\u77e5: " + saved.getTitle());
    return NotificationResponse.from(saved);
  }

  @Transactional
  public NotificationResponse publish(Long id, boolean publish) {
    PermissionUtil.check("system:NotificationTable:update");
    Notification n = Optional.ofNullable(mapper.selectById(id))
      .orElseThrow(() -> badRequest("\u901a\u77e5\u4e0d\u5b58\u5728"));
    n.setStatus(publish ? "published" : "withdrawn");
    if (publish) {
      n.setPublishAt(LocalDateTime.now());
    }
    n.setUpdatedAt(LocalDateTime.now());
    Notification saved = saveNotification(n);
    if (publish) {
      broadcast(saved);
    }
    operationLogService.log(publish ? "PUBLISH" : "WITHDRAW", "\u901a\u77e5\u7ba1\u7406", "\u66f4\u6539\u901a\u77e5\u72b6\u6001: " + saved.getTitle());
    return NotificationResponse.from(saved);
  }

  @Transactional
  public boolean delete(Long id) {
    PermissionUtil.check("system:NotificationTable:delete");
    Notification n = Optional.ofNullable(mapper.selectById(id))
      .orElseThrow(() -> badRequest("\u901a\u77e5\u4e0d\u5b58\u5728"));
    mapper.deleteById(n.getId());
    operationLogService.log("DELETE", "\u901a\u77e5\u7ba1\u7406", "\u5220\u9664\u901a\u77e5: " + n.getTitle());
    return true;
  }

  @Transactional(readOnly = true)
  public NotificationResponse detail(Long id) {
    PermissionUtil.check("system:NotificationTable:query");
    return NotificationResponse.from(Optional.ofNullable(mapper.selectById(id))
      .orElseThrow(() -> badRequest("\u901a\u77e5\u4e0d\u5b58\u5728")));
  }

  @Transactional(readOnly = true)
  public PageResult<NotificationResponse> page(String keyword, String status, String priority, int page, int size) {
    PermissionUtil.check("system:NotificationTable:query");
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    List<String> statuses = StringUtils.hasText(status)
      ? Arrays.asList(status.split(","))
      : List.of("draft", "published", "withdrawn");
    String keywordLike = StringUtils.hasText(keyword)
      ? "%" + keyword.trim().toLowerCase() + "%"
      : null;
    String normalizedPriority = StringUtils.hasText(priority) ? priority.trim().toLowerCase() : null;
    List<Notification> rows = mapper.selectPage(statuses, keywordLike, normalizedPriority, offset, safeSize);
    long total = mapper.count(statuses, keywordLike, normalizedPriority);
    List<NotificationResponse> list = rows.stream().map(NotificationResponse::from).toList();
    return new PageResult<>(list, total);
  }

  @Transactional(readOnly = true)
  public List<NotificationSummary> latestPublished(int size) {
    PermissionUtil.check("system:NotificationTable:query");
    int limit = Math.max(size, 1);
    List<Notification> result = mapper.selectLatestPublished(limit);
    return result.stream()
      .sorted(Comparator.comparing(Notification::getPublishAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
      .map(NotificationSummary::from)
      .toList();
  }

  private void apply(Notification target, NotificationUpsertRequest req) {
    target.setTitle(req.getTitle().trim());
    target.setContent(req.getContent());
    target.setPriority(req.getPriority());
    target.setSummary(req.getSummary());
    target.setType(req.getType());
    target.setCoverUrl(req.getCoverUrl());
    target.setAttachmentUrl(req.getAttachmentUrl());
    target.setAttachmentName(req.getAttachmentName());
  }

  private Notification saveNotification(Notification notification) {
    if (notification.getId() == null) {
      mapper.insert(notification);
    } else {
      mapper.update(notification);
    }
    return notification;
  }

  private void broadcast(Notification n) {
    try {
      String content = (n.getSummary() != null && !n.getSummary().isBlank())
        ? n.getSummary()
        : stripHtml(n.getContent());
      String message = n.getTitle() + "\uff1a" + trimPreview(content, 120);
      messageService.broadcast(message, "notification", priorityToQuality(n.getPriority()));
      nettySocketService.ifPresent(service -> service.broadcastNotification(n, message));
    } catch (Exception e) {
      operationLogService.log("WARN", "\u901a\u77e5\u7ba1\u7406", "\u901a\u77e5\u63a8\u9001\u5931\u8d25: " + e.getMessage());
    }
  }

  private String priorityToQuality(String priority) {
    if (priority == null) return "middle";
    return switch (priority.toLowerCase()) {
      case "high" -> "high";
      case "low" -> "low";
      default -> "middle";
    };
  }

  private String stripHtml(String html) {
    if (html == null) return "";
    return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
  }

  private String trimPreview(String text, int max) {
    if (text == null) return "";
    String t = text.trim();
    return t.length() > max ? t.substring(0, max) + "..." : t;
  }
}
