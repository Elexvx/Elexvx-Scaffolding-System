package top.elexvx.admin.service;

import top.elexvx.admin.exception.BusinessException;
import top.elexvx.admin.exception.ErrorCodes;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class UploadFileValidationService {
  private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp");
  private static final Set<String> OFFICE_OLE_EXTENSIONS = Set.of("doc", "xls", "ppt");
  private static final Set<String> OFFICE_ZIP_EXTENSIONS = Set.of("docx", "xlsx", "pptx");
  private static final Set<String> MIME_PDF = Set.of("application/pdf");
  private static final Set<String> MIME_CSV = Set.of("text/csv", "text/plain", "application/vnd.ms-excel");
  private static final Set<String> MIME_IMAGE = Set.of(
    "image/png",
    "image/jpeg",
    "image/jpg",
    "image/gif",
    "image/webp",
    "image/bmp"
  );
  private static final Set<String> MIME_OFFICE_OLE = Set.of(
    "application/msword",
    "application/vnd.ms-excel",
    "application/vnd.ms-powerpoint",
    "application/octet-stream"
  );
  private static final Set<String> MIME_OFFICE_ZIP = Set.of(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/zip",
    "application/octet-stream"
  );

  public void validate(String fileName, String contentType, InputStream stream) {
    String ext = extension(fileName);
    if (ext == null) {
      throw badRequest("文件扩展名不能为空");
    }
    byte[] head = readHead(stream);
    String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    if ("pdf".equals(ext)) {
      validateMime(normalizedType, MIME_PDF, "PDF MIME 不匹配");
      validateMagic(matches(head, 0x25, 0x50, 0x44, 0x46), "PDF 文件头不匹配");
      return;
    }
    if ("csv".equals(ext)) {
      validateMime(normalizedType, MIME_CSV, "CSV MIME 不匹配");
      return;
    }
    if (IMAGE_EXTENSIONS.contains(ext)) {
      validateMime(normalizedType, MIME_IMAGE, "图片 MIME 不匹配");
      validateMagic(matchImage(ext, head), "图片文件头不匹配");
      return;
    }
    if (OFFICE_OLE_EXTENSIONS.contains(ext)) {
      validateMime(normalizedType, MIME_OFFICE_OLE, "Office 文档 MIME 不匹配");
      validateMagic(matches(head, 0xD0, 0xCF, 0x11, 0xE0), "Office 文档文件头不匹配");
      return;
    }
    if (OFFICE_ZIP_EXTENSIONS.contains(ext)) {
      validateMime(normalizedType, MIME_OFFICE_ZIP, "Office 文档 MIME 不匹配");
      validateMagic(matches(head, 0x50, 0x4B, 0x03, 0x04), "Office 文档文件头不匹配");
    }
  }

  private void validateMime(String contentType, Set<String> whitelist, String message) {
    if (contentType.isEmpty()) {
      throw badRequest(message);
    }
    if (!whitelist.contains(contentType)) {
      throw badRequest(message);
    }
  }

  private void validateMagic(boolean matched, String message) {
    if (!matched) {
      throw badRequest(message);
    }
  }

  private boolean matchImage(String ext, byte[] head) {
    return switch (ext) {
      case "png" -> matches(head, 0x89, 0x50, 0x4E, 0x47);
      case "jpg", "jpeg" -> matches(head, 0xFF, 0xD8, 0xFF);
      case "gif" -> matches(head, 0x47, 0x49, 0x46, 0x38);
      case "webp" -> matches(head, 0x52, 0x49, 0x46, 0x46) && contains(head, "WEBP".getBytes());
      case "bmp" -> matches(head, 0x42, 0x4D);
      default -> false;
    };
  }

  private boolean contains(byte[] source, byte[] target) {
    if (source.length < target.length) return false;
    outer:
    for (int i = 0; i <= source.length - target.length; i++) {
      for (int j = 0; j < target.length; j++) {
        if (source[i + j] != target[j]) {
          continue outer;
        }
      }
      return true;
    }
    return false;
  }

  private boolean matches(byte[] head, int... signature) {
    if (head.length < signature.length) return false;
    for (int i = 0; i < signature.length; i++) {
      if ((head[i] & 0xFF) != signature[i]) {
        return false;
      }
    }
    return true;
  }

  private byte[] readHead(InputStream stream) {
    byte[] head = new byte[16];
    int total = 0;
    try {
      while (total < head.length) {
        int read = stream.read(head, total, head.length - total);
        if (read <= 0) {
          break;
        }
        total += read;
      }
    } catch (IOException e) {
      throw badRequest("读取文件头失败");
    }
    if (total == head.length) {
      return head;
    }
    byte[] resized = new byte[total];
    System.arraycopy(head, 0, resized, 0, total);
    return resized;
  }

  private String extension(String fileName) {
    if (fileName == null) return null;
    String name = fileName.trim();
    int dot = name.lastIndexOf('.');
    if (dot < 0 || dot == name.length() - 1) return null;
    return name.substring(dot + 1).toLowerCase(Locale.ROOT);
  }

  private BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
}
