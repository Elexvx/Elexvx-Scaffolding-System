package elexvx.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FileUploadInitRequest {
  @NotBlank(message = "文件名称不能为空")
  private String fileName;

  @NotNull(message = "文件大小不能为空")
  @Min(value = 1, message = "文件大小必须大于 0")
  private Long fileSize;

  @Min(value = 1024, message = "分片大小不能小于 1KB")
  private Integer chunkSize;

  @NotBlank(message = "文件指纹不能为空")
  private String fingerprint;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public Integer getChunkSize() {
    return chunkSize;
  }

  public void setChunkSize(Integer chunkSize) {
    this.chunkSize = chunkSize;
  }

  public String getFingerprint() {
    return fingerprint;
  }

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }
}
