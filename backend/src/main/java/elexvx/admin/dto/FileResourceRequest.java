package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class FileResourceRequest {
  @NotBlank(message = "内容不能为空")
  private String content;

  @NotBlank(message = "文件名称不能为空")
  private String fileName;

  @NotBlank(message = "文件后缀不能为空")
  private String suffix;

  @NotBlank(message = "文件地址不能为空")
  private String fileUrl;

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
}
