package elexvx.admin.service;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeService {
  private static final int CODE_EXPIRES_SECONDS = 300;

  private final Cache<String, String> store;
  private final Random random = new Random();

  public EmailCodeService(VerificationCacheService verificationCacheService) {
    this.store = verificationCacheService.emailCodeCache();
  }

  public int getExpiresInSeconds() {
    return CODE_EXPIRES_SECONDS;
  }

  public String generateCode(String email) {
    String code = String.format("%06d", random.nextInt(1000000));
    store.put(email, code);
    return code;
  }

  public boolean verify(String email, String code) {
    String cached = store.getIfPresent(email);
    if (cached == null) return false;
    boolean isValid = cached.equals(code == null ? "" : code.trim());
    if (isValid) {
      store.invalidate(email);
    }
    return isValid;
  }

  public void invalidate(String email) {
    if (email == null || email.isBlank()) return;
    store.invalidate(email);
  }
}
