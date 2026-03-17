package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.AnnouncementUpsertRequest;
import com.tencent.tdesign.entity.Announcement;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.mapper.AnnouncementMapper;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.AnnouncementResponse;
import com.tencent.tdesign.vo.AnnouncementSummary;
import com.tencent.tdesign.vo.PageResult;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AnnouncementService {
  private static final String DEFAULT_STATUS = "draft";
  private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = Set.of(
    "pdf",
    "doc",
    "docx",
    "xls",
    "xlsx",
    "csv",
    "ppt",
    "pptx",
    "png",
    "jpg",
    "jpeg",
    "gif",
    "webp"
  );
  private final AnnouncementMapper mapper;
  private final OperationLogService operationLogService;
  private final AuthContext authContext;

  public AnnouncementService(
    AnnouncementMapper mapper,
    OperationLogService operationLogService,
    AuthContext authContext
  ) {
    this.mapper = mapper;
    this.operationLogService = operationLogService;
    this.authContext = authContext;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  @Transactional
  public AnnouncementResponse create(AnnouncementUpsertRequest req) {
    PermissionUtil.check("system:AnnouncementTable:create");
    Announcement a = new Announcement();
    apply(a, req);
    a.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus() : DEFAULT_STATUS);
    a.setCreatedAt(LocalDateTime.now());
    a.setUpdatedAt(LocalDateTime.now());
    long userId = authContext.requireUserId();
    a.setCreatedById(userId);
    a.setCreatedByName(String.valueOf(userId));
    if ("published".equalsIgnoreCase(a.getStatus())) {
      a.setPublishAt(LocalDateTime.now());
    }
    Announcement saved = saveAnnouncement(a);
    operationLogService.log("CREATE", "公告管理", "创建公告: " + saved.getTitle());
    return AnnouncementResponse.from(saved);
  }

  @Transactional
  public AnnouncementResponse update(Long id, AnnouncementUpsertRequest req) {
    PermissionUtil.check("system:AnnouncementTable:update");
    Announcement a = Optional.ofNullable(mapper.selectById(id)).orElseThrow(() -> badRequest("公告不存在"));
    apply(a, req);
    if (StringUtils.hasText(req.getStatus())) {
      a.setStatus(req.getStatus());
    }
    a.setUpdatedAt(LocalDateTime.now());
    Announcement saved = saveAnnouncement(a);
    operationLogService.log("UPDATE", "公告管理", "更新公告: " + saved.getTitle());
    return AnnouncementResponse.from(saved);
  }

  @Transactional
  public AnnouncementResponse publish(Long id, boolean publish) {
    PermissionUtil.check("system:AnnouncementTable:update");
    Announcement a = Optional.ofNullable(mapper.selectById(id)).orElseThrow(() -> badRequest("公告不存在"));
    a.setStatus(publish ? "published" : "withdrawn");
    if (publish) {
      a.setPublishAt(LocalDateTime.now());
    }
    a.setUpdatedAt(LocalDateTime.now());
    Announcement saved = saveAnnouncement(a);
    operationLogService.log(publish ? "PUBLISH" : "WITHDRAW", "公告管理", "更改公告状态: " + saved.getTitle());
    return AnnouncementResponse.from(saved);
  }

  @Transactional
  public boolean delete(Long id) {
    PermissionUtil.check("system:AnnouncementTable:delete");
    Announcement a = Optional.ofNullable(mapper.selectById(id)).orElseThrow(() -> badRequest("公告不存在"));
    mapper.deleteById(a.getId());
    operationLogService.log("DELETE", "公告管理", "删除公告: " + a.getTitle());
    return true;
  }

  @Transactional
  public AnnouncementResponse detail(Long id) {
    PermissionUtil.checkAny("system:AnnouncementCards:query", "system:AnnouncementTable:query");
    return AnnouncementResponse.from(Optional.ofNullable(mapper.selectById(id)).orElseThrow(() -> badRequest("公告不存在")));
  }

    @Transactional
  public PageResult<AnnouncementResponse> page(String keyword, String status, String priority, int page, int size) {
    PermissionUtil.checkAny("system:AnnouncementCards:query", "system:AnnouncementTable:query");
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    List<String> statuses = StringUtils.hasText(status)
      ? java.util.Arrays.asList(status.split(","))
      : List.of("draft", "published", "withdrawn");
    List<Announcement> rows = mapper.selectPageByStatus(statuses, offset, safeSize);
    long total = mapper.countByStatus(statuses);
    List<AnnouncementResponse> list = rows.stream()
      .filter(a -> matches(a, keyword, priority))
      .map(AnnouncementResponse::from)
      .toList();
    return new PageResult<>(list, total);
  }


    @Transactional
  public List<AnnouncementSummary> latestPublished(int size) {
    PermissionUtil.checkAny("system:AnnouncementCards:query", "system:AnnouncementTable:query");
    int limit = Math.max(size, 1);
    List<Announcement> result = mapper.selectLatestPublished(limit);
    return result.stream()
      .sorted(Comparator.comparing(Announcement::getPublishAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
      .map(AnnouncementSummary::from)
      .toList();
  }


  private boolean matches(Announcement a, String keyword, String priority) {
    boolean matchKeyword = true;
    if (StringUtils.hasText(keyword)) {
      String lower = keyword.toLowerCase();
      matchKeyword = a.getTitle().toLowerCase().contains(lower) || (a.getSummary() != null && a.getSummary().toLowerCase().contains(lower));
    }
    boolean matchPriority = !StringUtils.hasText(priority) || priority.equalsIgnoreCase(a.getPriority());
    return matchKeyword && matchPriority;
  }

  private void apply(Announcement target, AnnouncementUpsertRequest req) {
    target.setTitle(req.getTitle().trim());
    target.setContent(req.getContent());
    target.setType(req.getType());
    target.setPriority(req.getPriority());
    target.setSummary(req.getSummary());
    target.setCoverUrl(req.getCoverUrl());
    String attachmentUrl = normalizeBlank(req.getAttachmentUrl());
    String attachmentName = normalizeBlank(req.getAttachmentName());
    if (attachmentUrl == null && attachmentName != null) throw badRequest("附件地址不能为空");
    if (attachmentUrl != null) {
      if (attachmentName == null) throw badRequest("附件名称不能为空");
      validateAttachmentName(attachmentName);
    }
    target.setAttachmentUrl(attachmentUrl);
    target.setAttachmentName(attachmentName);
  }

  private void validateAttachmentName(String name) {
    String ext = fileExtension(name);
    if (ext == null || !ALLOWED_ATTACHMENT_EXTENSIONS.contains(ext)) {
      throw badRequest("附件格式不支持");
    }
  }

  private String normalizeBlank(String v) {
    if (v == null) return null;
    String t = v.trim();
    return t.isEmpty() ? null : t;
  }

  private String fileExtension(String name) {
    if (name == null) return null;
    String n = name.trim();
    int dot = n.lastIndexOf('.');
    if (dot < 0 || dot == n.length() - 1) return null;
    String ext = n.substring(dot + 1).toLowerCase();
    return ext.isEmpty() ? null : ext;
  }

  private Announcement saveAnnouncement(Announcement announcement) {
    if (announcement.getId() == null) {
      mapper.insert(announcement);
    } else {
      mapper.update(announcement);
    }
    return announcement;
  }

}
