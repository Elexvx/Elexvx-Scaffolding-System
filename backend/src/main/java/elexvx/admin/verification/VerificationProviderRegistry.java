package elexvx.admin.verification;

import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
/**
 * 验证码 Provider 注册表。
 *
 * <p>将 Spring 容器中发现的 {@link VerificationProvider} 按 {@code type} 归一化（trim + lower-case）后注册，
 * 供认证流程按类型选择具体实现（例如 sms / email）。
 */
public class VerificationProviderRegistry {
  private final Map<String, VerificationProvider> providers;

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public VerificationProviderRegistry(List<VerificationProvider> providers) {
    Map<String, VerificationProvider> map = new LinkedHashMap<>();
    if (providers != null) {
      for (VerificationProvider provider : providers) {
        if (provider == null || provider.getType() == null) continue;
        map.put(provider.getType().trim().toLowerCase(), provider);
      }
    }
    this.providers = Collections.unmodifiableMap(map);
  }

  public VerificationProvider require(String type) {
    if (type == null || type.isBlank()) {
      throw badRequest("验证码类型不能为空");
    }
    VerificationProvider provider = providers.get(type.trim().toLowerCase());
    if (provider == null) {
      throw badRequest("验证码模块未配置: " + type);
    }
    return provider;
  }

  public Map<String, VerificationProvider> getProviders() {
    return providers;
  }
}
