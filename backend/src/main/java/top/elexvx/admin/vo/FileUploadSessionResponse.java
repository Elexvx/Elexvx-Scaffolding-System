package top.elexvx.admin.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUploadSessionResponse {
  private String uploadId;
  private String fileName;
  private Long fileSize;
  private Integer chunkSize;
  private Integer totalChunks;
  private List<Integer> uploadedChunks = Collections.emptyList();

  public static FileUploadSessionResponse from(
    String uploadId,
    String fileName,
    long fileSize,
    int chunkSize,
    int totalChunks,
    List<Integer> uploadedChunks
  ) {
    FileUploadSessionResponse response = new FileUploadSessionResponse();
    response.uploadId = uploadId;
    response.fileName = fileName;
    response.fileSize = fileSize;
    response.chunkSize = chunkSize;
    response.totalChunks = totalChunks;
    response.uploadedChunks = uploadedChunks != null ? new ArrayList<>(uploadedChunks) : Collections.emptyList();
    return response;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

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

  public Integer getTotalChunks() {
    return totalChunks;
  }

  public void setTotalChunks(Integer totalChunks) {
    this.totalChunks = totalChunks;
  }

  public List<Integer> getUploadedChunks() {
    return uploadedChunks;
  }

  public void setUploadedChunks(List<Integer> uploadedChunks) {
    this.uploadedChunks = uploadedChunks;
  }
}
