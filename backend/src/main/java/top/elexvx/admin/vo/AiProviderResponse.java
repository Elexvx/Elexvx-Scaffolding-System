package top.elexvx.admin.vo;

import top.elexvx.admin.entity.AiProviderSetting;
import java.time.LocalDateTime;

public class AiProviderResponse {
  private Long id;
  private String name;
  private String vendor;
  private String baseUrl;
  private String endpointPath;
  private String model;
  private String apiVersion;
  private Double temperature;
  private Integer maxTokens;
  private Boolean isDefaultProvider;
  private Boolean enabled;
  private String remark;
  private boolean apiKeyConfigured;
  private String lastTestStatus;
  private String lastTestMessage;
  private LocalDateTime lastTestedAt;

  public static AiProviderResponse from(AiProviderSetting entity) {
    AiProviderResponse r = new AiProviderResponse();
    r.id = entity.getId();
    r.name = entity.getName();
    r.vendor = entity.getVendor();
    r.baseUrl = entity.getBaseUrl();
    r.endpointPath = entity.getEndpointPath();
    r.model = entity.getModel();
    r.apiVersion = entity.getApiVersion();
    r.temperature = entity.getTemperature();
    r.maxTokens = entity.getMaxTokens();
    r.isDefaultProvider = entity.getIsDefaultProvider();
    r.enabled = entity.getEnabled();
    r.remark = entity.getRemark();
    r.apiKeyConfigured = entity.getApiKey() != null && !entity.getApiKey().isBlank();
    r.lastTestStatus = entity.getLastTestStatus();
    r.lastTestMessage = entity.getLastTestMessage();
    r.lastTestedAt = entity.getLastTestedAt();
    return r;
  }

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

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public boolean isApiKeyConfigured() {
    return apiKeyConfigured;
  }

  public void setApiKeyConfigured(boolean apiKeyConfigured) {
    this.apiKeyConfigured = apiKeyConfigured;
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
}
