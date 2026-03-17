package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.FileResourceRequest;
import com.tencent.tdesign.entity.FileResource;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import com.tencent.tdesign.mapper.FileResourceMapper;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.FileResourceResponse;
import com.tencent.tdesign.vo.PageResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileDownloadService {
  private static final String RESOURCE_NAME = "ConsoleDownload";
  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
  private final FileResourceMapper mapper;
  private final ObjectStorageService storageService;
  private final OperationLogService operationLogService;
  private final AuthContext authContext;

  public FileDownloadService(
    FileResourceMapper mapper,
    ObjectStorageService storageService,
    OperationLogService operationLogService,
    AuthContext authContext
  ) {
    this.mapper = mapper;
    this.storageService = storageService;
    this.operationLogService = operationLogService;
    this.authContext = authContext;
  }

  @Transactional(readOnly = true)
  public PageResult<FileResourceResponse> page(int page, int size) {
    PermissionUtil.check("system:" + RESOURCE_NAME + ":query");
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    List<FileResource> rows = mapper.selectPage(offset, safeSize);
    long total = mapper.count();
    List<FileResourceResponse> list = rows.stream().map(FileResourceResponse::from).toList();
    return new PageResult<>(list, total);
  }

  @Transactional
  public FileResourceResponse create(FileResourceRequest req) {
    PermissionUtil.check("system:" + RESOURCE_NAME + ":create");
    FileResource entity = new FileResource();
    LocalDateTime now = LocalDateTime.now();
    entity.setContent(normalize(req.getContent()));
    entity.setFileName(normalize(req.getFileName()));
    entity.setSuffix(normalizeSuffix(req.getSuffix()));
    entity.setFileUrl(normalize(req.getFileUrl()));
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    long userId = authContext.requireUserId();
    entity.setCreatedById(userId);
    entity.setCreatedByName(String.valueOf(userId));
    mapper.insert(entity);
    operationLogService.log("CREATE", "文件下载", "新增文件: " + entity.getContent());
    return FileResourceResponse.from(entity);
  }

  @Transactional
  public FileResourceResponse update(Long id, FileResourceRequest req) {
    PermissionUtil.check("system:" + RESOURCE_NAME + ":update");
    FileResource existing = Optional.ofNullable(mapper.selectById(id)).orElseThrow(() -> badRequest("记录不存在"));
    String fileUrl = normalize(req.getFileUrl());
    if (!Objects.equals(existing.getFileUrl(), fileUrl)) {
      storageService.deleteByUrl(existing.getFileUrl());
    }
    existing.setContent(normalize(req.getContent()));
    existing.setFileName(normalize(req.getFileName()));
    existing.setSuffix(normalizeSuffix(req.getSuffix()));
    existing.setFileUrl(fileUrl);
    existing.setUpdatedAt(LocalDateTime.now());
    mapper.update(existing);
    operationLogService.log("UPDATE", "文件下载", "更新文件: " + existing.getContent());
    return FileResourceResponse.from(existing);
  }

  @Transactional
  public boolean delete(Long id) {
    PermissionUtil.check("system:" + RESOURCE_NAME + ":delete");
    FileResource existing = Optional.ofNullable(mapper.selectById(id)).orElseThrow(() -> badRequest("记录不存在"));
    mapper.deleteById(id);
    storageService.deleteByUrl(existing.getFileUrl());
    operationLogService.log("DELETE", "文件下载", "删除文件: " + existing.getContent());
    return true;
  }

  private String normalize(String value) {
    return value == null ? null : value.trim();
  }

  private String normalizeSuffix(String value) {
    if (value == null) return null;
    String trimmed = value.trim().toLowerCase(Locale.ROOT);
    return trimmed.isEmpty() ? null : trimmed;
  }
}
