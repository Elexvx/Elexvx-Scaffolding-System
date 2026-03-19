package top.elexvx.admin.service;

import top.elexvx.admin.config.properties.ElexvxCoreProperties;
import top.elexvx.admin.vo.UpdateCheckResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class SystemUpdateService {
  private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern URL_PATTERN = Pattern.compile("\"downloadUrl\"\\s*:\\s*\"([^\"]+)\"");

  private final HttpClient httpClient;
  private final ElexvxCoreProperties properties;

  public SystemUpdateService(ElexvxCoreProperties properties) {
    this.properties = properties;
    this.httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .build();
  }

  public UpdateCheckResponse checkForUpdates() {
    String currentVersion = properties.getSystem().getVersion();
    String updateCheckUrl = properties.getSystem().getUpdateCheckUrl();
    UpdateCheckResponse response = new UpdateCheckResponse();
    response.setCurrentVersion(currentVersion);
    if (updateCheckUrl == null || updateCheckUrl.isBlank()) {
      response.setUpdateAvailable(false);
      response.setMessage("未配置更新检测地址");
      return response;
    }
    try {
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(updateCheckUrl))
        .timeout(Duration.ofSeconds(6))
        .GET()
        .build();
      HttpResponse<String> result = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (result.statusCode() >= 400) {
        response.setUpdateAvailable(false);
        response.setMessage("更新检测失败，返回状态码 " + result.statusCode());
        return response;
      }
      String body = result.body() == null ? "" : result.body();
      String latest = extract(body, VERSION_PATTERN);
      String downloadUrl = extract(body, URL_PATTERN);
      response.setLatestVersion(latest);
      response.setUpdateUrl(downloadUrl);
      response.setUpdateAvailable(isNewer(currentVersion, latest));
      if (!response.isUpdateAvailable()) {
        response.setMessage("当前已是最新版本");
      }
      return response;
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      response.setUpdateAvailable(false);
      response.setMessage("更新检测失败: " + e.getMessage());
      return response;
    }
  }

  private String extract(String body, Pattern pattern) {
    Matcher matcher = pattern.matcher(body);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  private boolean isNewer(String current, String latest) {
    if (latest == null || latest.isBlank()) {
      return false;
    }
    String[] currentParts = current.split("\\.");
    String[] latestParts = latest.split("\\.");
    int max = Math.max(currentParts.length, latestParts.length);
    for (int i = 0; i < max; i++) {
      int c = i < currentParts.length ? parseInt(currentParts[i]) : 0;
      int l = i < latestParts.length ? parseInt(latestParts[i]) : 0;
      if (l > c) return true;
      if (l < c) return false;
    }
    return false;
  }

  private int parseInt(String value) {
    try {
      return Integer.parseInt(value.replaceAll("[^0-9]", ""));
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
