package elexvx.admin.entity;

import java.time.LocalDateTime;

public class AiProviderSetting {

  private Long id;

  private String name;

  private String vendor;

  private String baseUrl;

  private String endpointPath;

  private String model;

  private String apiKey;

  private String apiVersion;

  private Double temperature;

  private Integer maxTokens;

  private Boolean isDefaultProvider;

  private Boolean enabled;

  private String extraHeaders;

  private String remark;

  private String lastTestStatus;

  private String lastTestMessage;

  private LocalDateTime lastTestedAt;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getEndpointPath() {
    return endpointPath;
  }

  public void setEndpointPath(String endpointPath) {
    this.endpointPath = endpointPath;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public Boolean getIsDefaultProvider() {
    return isDefaultProvider;
  }

  public void setIsDefaultProvider(Boolean isDefaultProvider) {
    this.isDefaultProvider = isDefaultProvider;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getExtraHeaders() {
    return extraHeaders;
  }

  public void setExtraHeaders(String extraHeaders) {
    this.extraHeaders = extraHeaders;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getLastTestStatus() {
    return lastTestStatus;
  }

  public void setLastTestStatus(String lastTestStatus) {
    this.lastTestStatus = lastTestStatus;
  }

  public String getLastTestMessage() {
    return lastTestMessage;
  }

  public void setLastTestMessage(String lastTestMessage) {
    this.lastTestMessage = lastTestMessage;
  }

  public LocalDateTime getLastTestedAt() {
    return lastTestedAt;
  }

  public void setLastTestedAt(LocalDateTime lastTestedAt) {
    this.lastTestedAt = lastTestedAt;
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
