package elexvx.admin.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import elexvx.admin.entity.StorageSetting;
import jakarta.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ObjectStorageClientManager {
  private static final Logger log = LoggerFactory.getLogger(ObjectStorageClientManager.class);
  private final AtomicReference<AliyunHolder> aliyunRef = new AtomicReference<>();
  private final AtomicReference<TencentHolder> tencentRef = new AtomicReference<>();

  public OSS getAliyunClient(StorageSetting setting, String endpoint) {
    String fingerprint = fingerprint(setting, endpoint);
    AliyunHolder existing = aliyunRef.get();
    if (existing != null && Objects.equals(existing.fingerprint, fingerprint)) {
      return existing.client;
    }
    OSS created = new OSSClientBuilder().build(endpoint, setting.getAccessKey(), setting.getSecretKey());
    AliyunHolder updated = new AliyunHolder(fingerprint, created);
    AliyunHolder previous = aliyunRef.getAndSet(updated);
    if (previous != null) {
      shutdownAliyunClient(previous.client, "refresh");
    }
    return created;
  }

  public COSClient getTencentClient(StorageSetting setting) {
    String fingerprint = fingerprint(setting, setting.getRegion());
    TencentHolder existing = tencentRef.get();
    if (existing != null && Objects.equals(existing.fingerprint, fingerprint)) {
      return existing.client;
    }
    ClientConfig config = new ClientConfig(new Region(setting.getRegion()));
    config.setHttpProtocol(HttpProtocol.https);
    COSClient created = new COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(setting.getAccessKey(), setting.getSecretKey()), config);
    TencentHolder updated = new TencentHolder(fingerprint, created);
    TencentHolder previous = tencentRef.getAndSet(updated);
    if (previous != null) {
      shutdownTencentClient(previous.client, "refresh");
    }
    return created;
  }

  public void invalidateAll() {
    AliyunHolder aliyun = aliyunRef.getAndSet(null);
    if (aliyun != null) {
      shutdownAliyunClient(aliyun.client, "invalidate");
    }
    TencentHolder tencent = tencentRef.getAndSet(null);
    if (tencent != null) {
      shutdownTencentClient(tencent.client, "invalidate");
    }
  }

  @PreDestroy
  public void destroy() {
    invalidateAll();
  }

  private String fingerprint(StorageSetting setting, String ext) {
    return String.join("|",
      String.valueOf(setting.getProvider()),
      String.valueOf(setting.getAccessKey()),
      String.valueOf(setting.getSecretKey()),
      String.valueOf(setting.getBucket()),
      String.valueOf(ext));
  }

  private void shutdownAliyunClient(OSS client, String stage) {
    try {
      client.shutdown();
    } catch (Exception shutdownException) {
      log.warn("关闭阿里云 OSS 客户端失败，stage={}", stage, shutdownException);
    }
  }

  private void shutdownTencentClient(COSClient client, String stage) {
    try {
      client.shutdown();
    } catch (Exception shutdownException) {
      log.warn("关闭腾讯云 COS 客户端失败，stage={}", stage, shutdownException);
    }
  }

  private record AliyunHolder(String fingerprint, OSS client) {}
  private record TencentHolder(String fingerprint, COSClient client) {}
}
