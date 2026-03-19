package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class FileUploadCompleteRequest {
  @NotBlank(message = "上传 ID 不能为空")
  private String uploadId;

  private String page;
  private String folder;

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public String getFolder() {
    return folder;
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }
}
