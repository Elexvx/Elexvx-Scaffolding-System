package elexvx.admin.vo;

import elexvx.admin.entity.FileResource;
import java.time.LocalDateTime;

public class FileResourceResponse {
  private Long id;
  private String content;
  private String fileName;
  private String suffix;
  private String fileUrl;
  private Long createdById;
  private String createdByName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static FileResourceResponse from(FileResource entity) {
    FileResourceResponse response = new FileResourceResponse();
    response.id = entity.getId();
    response.content = entity.getContent();
    response.fileName = entity.getFileName();
    response.suffix = entity.getSuffix();
    response.fileUrl = entity.getFileUrl();
    response.createdById = entity.getCreatedById();
    response.createdByName = entity.getCreatedByName();
    response.createdAt = entity.getCreatedAt();
    response.updatedAt = entity.getUpdatedAt();
    return response;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public void setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }

  public Long getCreatedById() {
    return createdById;
  }

  public void setCreatedById(Long createdById) {
    this.createdById = createdById;
  }

  public String getCreatedByName() {
    return createdByName;
  }

  public void setCreatedByName(String createdByName) {
    this.createdByName = createdByName;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
