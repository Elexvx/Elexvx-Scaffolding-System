package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class AiProviderRequest {
  private Long id;

  @NotBlank(message = "名称不能为空")
  private String name;

  @NotBlank(message = "厂商类型不能为空")
  private String vendor;

  @NotBlank(message = "BaseUrl 不能为空")
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
  private Boolean reuseApiKey;

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

  public Boolean getReuseApiKey() {
    return reuseApiKey;
  }

  public void setReuseApiKey(Boolean reuseApiKey) {
    this.reuseApiKey = reuseApiKey;
  }
}
