package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.SecuritySetting;
import com.tencent.tdesign.exception.BusinessException;
import com.tencent.tdesign.exception.ErrorCodes;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
  private static final int DEFAULT_MIN_LENGTH = 6;

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  private final SecuritySettingService securitySettingService;

  public PasswordPolicyService(SecuritySettingService securitySettingService) {
    this.securitySettingService = securitySettingService;
  }

  /**
   * 校验密码是否符合系统设置的规范。
   * <p>由注册/修改/重置等入口统一调用，避免各处规则不一致。</p>
   */
  public void validate(String password) {
    SecuritySetting setting = securitySettingService.getOrCreate();
    int minLength = setting.getPasswordMinLength() != null ? setting.getPasswordMinLength() : DEFAULT_MIN_LENGTH;
    boolean requireUpper = Boolean.TRUE.equals(setting.getPasswordRequireUppercase());
    boolean requireLower = Boolean.TRUE.equals(setting.getPasswordRequireLowercase());
    boolean requireSpecial = Boolean.TRUE.equals(setting.getPasswordRequireSpecial());
    boolean allowSequential = setting.getPasswordAllowSequential() == null || Boolean.TRUE.equals(setting.getPasswordAllowSequential());

    List<String> violations = new ArrayList<>();
    String value = password == null ? "" : password;

    if (value.length() < minLength) {
      violations.add("至少 " + minLength + " 位");
    }
    if (requireUpper && value.chars().noneMatch(Character::isUpperCase)) {
      violations.add("包含大写字母");
    }
    if (requireLower && value.chars().noneMatch(Character::isLowerCase)) {
      violations.add("包含小写字母");
    }
    if (requireSpecial && value.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
      violations.add("包含特殊字符");
    }
    if (!allowSequential && hasSequentialChars(value)) {
      violations.add("禁止连续字符");
    }

    if (!violations.isEmpty()) {
      throw badRequest("密码需满足：" + String.join("、", violations));
    }
  }

  private boolean hasSequentialChars(String value) {
    if (value == null || value.length() < 3) return false;
    int streak = 1;
    int prev = -1;
    for (char raw : value.toCharArray()) {
      if (!Character.isLetterOrDigit(raw)) {
        streak = 1;
        prev = -1;
        continue;
      }
      int current = Character.toLowerCase(raw);
      if (prev != -1 && Math.abs(current - prev) == 1) {
        streak += 1;
        if (streak >= 3) return true;
      } else {
        streak = 1;
      }
      prev = current;
    }
    return false;
  }
}
