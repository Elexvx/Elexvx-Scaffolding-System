package elexvx.admin.service;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class SmsCodeService {
  public SmsCodeService(VerificationCacheService verificationCacheService) {
    this.store = verificationCacheService.smsCodeCache();
  }

  private static final int CODE_EXPIRES_SECONDS = 300;
  private final Cache<String, String> store;
  private final Random random = new Random();

  public int getExpiresInSeconds() {
    return CODE_EXPIRES_SECONDS;
  }

  public String generateCode(String phone) {
    String code = String.format("%06d", random.nextInt(1000000));
    store.put(phone, code);
    return code;
  }

  public boolean verify(String phone, String code) {
    String cached = store.getIfPresent(phone);
    if (cached == null) return false;
    store.invalidate(phone);
    return cached.equals(code == null ? "" : code.trim());
  }

  public void invalidate(String phone) {
    if (phone == null || phone.isBlank()) return;
    store.invalidate(phone);
  }
}
