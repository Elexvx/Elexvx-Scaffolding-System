package top.elexvx.admin.controller;

import top.elexvx.admin.security.AuthContext;
import top.elexvx.admin.service.FileTokenService;
import top.elexvx.admin.service.ObjectStorageService;
import top.elexvx.admin.service.OperationLogService;
import top.elexvx.admin.service.PermissionFacade;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class FileAccessController {
  private final FileTokenService tokenService;
  private final ObjectStorageService storageService;
  private final AuthContext authContext;
  private final PermissionFacade permissionFacade;
  private final OperationLogService operationLogService;

  public FileAccessController(
    FileTokenService tokenService,
    ObjectStorageService storageService,
    AuthContext authContext,
    PermissionFacade permissionFacade,
    OperationLogService operationLogService
  ) {
    this.tokenService = tokenService;
    this.storageService = storageService;
    this.authContext = authContext;
    this.permissionFacade = permissionFacade;
    this.operationLogService = operationLogService;
  }

  @RequestMapping(value = "/{token}", method = RequestMethod.HEAD)
  public ResponseEntity<Void> head(@PathVariable("token") String token) {
    try {
      FileTokenService.TokenPayload payload = tokenService.decrypt(token);
      authorize(payload);
      ObjectStorageService.FileMeta meta = storageService.stat(payload);
      HttpHeaders headers = new HttpHeaders();
      if (meta.getContentType() != null && !meta.getContentType().isBlank()) {
        headers.setContentType(MediaType.parseMediaType(meta.getContentType()));
      }
      if (meta.getContentLength() >= 0) {
        headers.setContentLength(meta.getContentLength());
      }
      headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
      headers.setCacheControl(CacheControl.noStore());
      return new ResponseEntity<>(headers, HttpStatus.OK);
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/{token}")
  public ResponseEntity<InputStreamResource> preview(
    @PathVariable("token") String token,
    @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
  ) {
    try {
      FileTokenService.TokenPayload payload = tokenService.decrypt(token);
      authorize(payload);

      if (rangeHeader != null && !rangeHeader.isBlank() && rangeHeader.startsWith("bytes=")) {
        ObjectStorageService.FileMeta meta = storageService.stat(payload);
        long total = meta.getContentLength();
        if (total > 0) {
          ByteRange range = parseRange(rangeHeader, total);
          if (range == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + total);
            return new ResponseEntity<>(null, headers, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
          }
          ObjectStorageService.FileStream stream = storageService.openStream(payload, range.start(), range.end());
          HttpHeaders headers = new HttpHeaders();
          writeDownloadHeaders(headers, stream.getContentType(), stream.getContentLength(), true);
          headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + range.start() + "-" + range.end() + "/" + total);
          InputStream inputStream = stream.getInputStream();
          return new ResponseEntity<>(
            new InputStreamResource(Objects.requireNonNull(inputStream)),
            headers,
            HttpStatus.PARTIAL_CONTENT
          );
        }
      }

      ObjectStorageService.FileStream stream = storageService.openStream(payload);
      HttpHeaders headers = new HttpHeaders();
      writeDownloadHeaders(headers, stream.getContentType(), stream.getContentLength(), false);
      InputStream inputStream = stream.getInputStream();
      return new ResponseEntity<>(
        new InputStreamResource(Objects.requireNonNull(inputStream)),
        headers,
        HttpStatus.OK
      );
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  private void authorize(FileTokenService.TokenPayload payload) {
    long userId = authContext.requireUserId();
    boolean hasDownloadPermission = permissionFacade.getEffectivePermissions(userId).contains("system:ConsoleDownload:query");
    boolean isAdmin = permissionFacade.isAdminAccount(userId);
    if (!isAdmin && !hasDownloadPermission) {
      operationLogService.log("FILE_ACCESS_DENY", "文件访问", "拒绝访问对象: " + payload.getObjectKey());
      throw new AccessDeniedException("权限不足");
    }
    operationLogService.log("FILE_ACCESS", "文件访问", "访问对象: " + payload.getObjectKey());
  }

  private void writeDownloadHeaders(HttpHeaders headers, String contentType, long contentLength, boolean partial) {
    if (contentType != null && !contentType.isBlank()) {
      headers.setContentType(MediaType.parseMediaType(contentType));
    }
    if (contentLength >= 0) {
      headers.setContentLength(contentLength);
    }
    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
    headers.setContentDisposition(ContentDisposition.attachment().build());
    headers.setCacheControl(CacheControl.noStore());
  }

  private record ByteRange(long start, long end) {
  }

  private ByteRange parseRange(String header, long total) {
    String raw = header.trim();
    if (!raw.startsWith("bytes=")) return null;
    String spec = raw.substring("bytes=".length());
    int comma = spec.indexOf(',');
    if (comma >= 0) spec = spec.substring(0, comma);
    spec = spec.trim();
    int dash = spec.indexOf('-');
    if (dash < 0) return null;
    String left = spec.substring(0, dash).trim();
    String right = spec.substring(dash + 1).trim();

    try {
      if (left.isEmpty()) {
        if (right.isEmpty()) return null;
        long suffix = Long.parseLong(right);
        if (suffix <= 0) return null;
        if (suffix >= total) return new ByteRange(0, total - 1);
        return new ByteRange(total - suffix, total - 1);
      }

      long start = Long.parseLong(left);
      long end = right.isEmpty() ? (total - 1) : Long.parseLong(right);
      if (start < 0 || end < start || start >= total) return null;
      if (end >= total) end = total - 1;
      return new ByteRange(start, end);
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
