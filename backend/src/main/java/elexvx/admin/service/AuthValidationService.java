package elexvx.admin.service;

import elexvx.admin.entity.UserEntity;
import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.vo.UserProfileResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthValidationService {
  private static final String DOC_TYPE_RESIDENT_ID_CARD = "resident_id_card";
  private static final String DOC_TYPE_PASSPORT = "passport";
  private static final Set<String> DOC_TYPE_RESIDENT_ID_CARD_ALIASES =
    Set.of("resident_id_card", "id_card", "identity_card", "china_id_card", "居民身份证");
  private static final Set<String> DOC_TYPE_PASSPORT_ALIASES = Set.of("passport", "护照");
  private static final int[] RESIDENT_ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
  private static final char[] RESIDENT_ID_CARD_CHECKSUM_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

  private final Optional<SmsSenderService> smsSenderService;
  private final Optional<EmailSenderService> emailSenderService;

  public AuthValidationService(Optional<SmsSenderService> smsSenderService, Optional<EmailSenderService> emailSenderService) {
    this.smsSenderService = smsSenderService;
    this.emailSenderService = emailSenderService;
  }

  public void ensureSmsEnabled(VerificationSetting setting) {
    if (smsSenderService.isEmpty()) {
      throw badRequest("短信模块未启用");
    }
    if (setting == null || !Boolean.TRUE.equals(setting.getSmsEnabled())) {
      throw badRequest("短信验证已关闭");
    }
  }

  public void ensureEmailEnabled(VerificationSetting setting) {
    if (emailSenderService.isEmpty()) {
      throw badRequest("邮箱模块未启用");
    }
    if (setting == null || !Boolean.TRUE.equals(setting.getEmailEnabled())) {
      throw badRequest("邮箱验证已关闭");
    }
  }

  public void ensureEmailConfig(VerificationSetting setting) {
    if (
      setting == null
        || isBlank(setting.getEmailHost())
        || setting.getEmailPort() == null
        || setting.getEmailPort() <= 0
        || isBlank(setting.getEmailUsername())
        || isBlank(setting.getEmailPassword())
    ) {
      throw badRequest("邮箱配置不完整");
    }
  }

  public void ensureSmsConfig(VerificationSetting setting) {
    if (setting == null) {
      throw badRequest("短信配置缺失");
    }
    SmsSenderService sender = requireSmsSender();
    boolean aliyunEnabled = sender.isAliyunEnabled(setting);
    boolean tencentEnabled = sender.isTencentEnabled(setting);
    if (!aliyunEnabled && !tencentEnabled) {
      throw badRequest("短信配置不完整");
    }
    if (aliyunEnabled) {
      boolean accessKeyMissing = isBlank(setting.getSmsAliyunAccessKeyId());
      boolean secretMissing = isBlank(setting.getSmsAliyunAccessKeySecret());
      boolean signMissing = isBlank(setting.getSmsAliyunSignName());
      boolean templateMissing = isBlank(setting.getSmsAliyunTemplateCode());
      boolean regionMissing = isBlank(setting.getSmsAliyunRegionId());
      if (accessKeyMissing || secretMissing || signMissing || templateMissing || regionMissing) {
        throw badRequest("短信配置不完整");
      }
    }
    if (tencentEnabled) {
      if (
        isBlank(setting.getSmsTencentSecretId())
          || isBlank(setting.getSmsTencentSecretKey())
          || isBlank(setting.getSmsTencentSignName())
          || isBlank(setting.getSmsTencentTemplateId())
          || isBlank(setting.getSmsTencentRegion())
          || isBlank(setting.getSmsSdkAppId())
      ) {
        throw badRequest("短信配置不完整");
      }
    }
  }

  public String normalizePhone(String phone) {
    if (phone == null) {
      return "";
    }
    String cleaned = phone.trim().replaceAll("[\\s-]", "");
    if (cleaned.startsWith("+86")) {
      cleaned = cleaned.substring(3);
    }
    if (cleaned.startsWith("86") && cleaned.length() > 11) {
      cleaned = cleaned.substring(2);
    }
    return cleaned;
  }

  public String normalizeEmail(String email) {
    if (email == null) {
      return "";
    }
    return email.trim().replaceAll("\\s+", "");
  }

  public String normalizeAccount(String account) {
    if (account == null) {
      return "";
    }
    return account.trim();
  }

  public String normalizeCode(String code) {
    if (code == null) {
      return "";
    }
    return code.trim().replaceAll("\\s+", "");
  }

  public String normalizeDocumentType(String idType) {
    if (idType == null) {
      return null;
    }
    String normalized = idType.trim();
    if (normalized.isEmpty()) {
      return null;
    }
    String lower = normalized.toLowerCase(Locale.ROOT);
    if (DOC_TYPE_RESIDENT_ID_CARD_ALIASES.contains(lower) || DOC_TYPE_RESIDENT_ID_CARD_ALIASES.contains(normalized)) {
      return DOC_TYPE_RESIDENT_ID_CARD;
    }
    if (DOC_TYPE_PASSPORT_ALIASES.contains(lower) || DOC_TYPE_PASSPORT_ALIASES.contains(normalized)) {
      return DOC_TYPE_PASSPORT;
    }
    return lower;
  }

  public void validateDocumentInfo(UserEntity user) {
    String idType = normalizeDocumentType(user.getIdType());
    String idCard = normalizeIdCard(user.getIdCard());
    user.setIdType(idType);
    user.setIdCard(idCard.isBlank() ? null : idCard);
    if (idType == null && !idCard.isBlank()) {
      throw badRequest("证件号码已填写，请先选择证件类型");
    }
    if (!idCard.isBlank()) {
      switch (idType) {
        case DOC_TYPE_RESIDENT_ID_CARD -> validateResidentIdCard(idCard);
        case DOC_TYPE_PASSPORT -> validatePassport(idCard);
        default -> throw badRequest("不支持的证件类型: " + idType);
      }
    }
    LocalDate validFrom = user.getIdValidFrom();
    LocalDate validTo = user.getIdValidTo();
    if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
      throw badRequest("证件有效期止不能早于证件有效期起");
    }
  }

  public void fillProfileCompleteness(UserProfileResponse profile) {
    int basicDone = 0;
    int documentDone = 0;
    List<String> incompleteItems = new ArrayList<>();
    if (hasText(profile.getName())) {
      basicDone += 1;
    } else {
      incompleteItems.add("name");
    }
    if (hasText(profile.getGender())) {
      basicDone += 1;
    } else {
      incompleteItems.add("gender");
    }
    if (hasText(profile.getMobile())) {
      basicDone += 1;
    } else {
      incompleteItems.add("mobile");
    }
    if (hasText(profile.getEmail())) {
      basicDone += 1;
    } else {
      incompleteItems.add("email");
    }
    boolean hasAddress = hasText(profile.getAddress())
      || hasText(profile.getProvince())
      || hasText(profile.getCity())
      || hasText(profile.getDistrict());
    if (hasAddress) {
      basicDone += 1;
    } else {
      incompleteItems.add("address");
    }
    if (hasText(profile.getIdType())) {
      documentDone += 1;
    } else {
      incompleteItems.add("idType");
    }
    if (hasText(profile.getIdCard())) {
      documentDone += 1;
    } else {
      incompleteItems.add("idCard");
    }
    if (profile.getIdValidFrom() != null) {
      documentDone += 1;
    } else {
      incompleteItems.add("idValidFrom");
    }
    if (profile.getIdValidTo() != null) {
      documentDone += 1;
    } else {
      incompleteItems.add("idValidTo");
    }
    profile.setBasicInfoScore(percent(basicDone, 5));
    profile.setDocumentInfoScore(percent(documentDone, 4));
    profile.setCompletenessScore(percent(basicDone + documentDone, 9));
    profile.setIncompleteItems(incompleteItems);
  }

  private SmsSenderService requireSmsSender() {
    return smsSenderService.orElseThrow(() -> badRequest("短信模块未启用"));
  }

  private String normalizeIdCard(String idCard) {
    if (idCard == null) {
      return "";
    }
    return idCard.trim().toUpperCase();
  }

  private void validateResidentIdCard(String idCard) {
    if (!idCard.matches("^[1-9]\\d{16}[0-9X]$")) {
      throw badRequest("居民身份证号码格式不正确");
    }
    String birth = idCard.substring(6, 14);
    try {
      LocalDate.parse(birth, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
    } catch (Exception ex) {
      throw badRequest("居民身份证号码中的出生日期不合法");
    }
    if (!isValidResidentIdCardChecksum(idCard)) {
      throw badRequest("居民身份证号码校验位不正确");
    }
  }

  private boolean isValidResidentIdCardChecksum(String idCard) {
    int sum = 0;
    for (int i = 0; i < 17; i++) {
      char ch = idCard.charAt(i);
      if (ch < '0' || ch > '9') {
        return false;
      }
      sum += (ch - '0') * RESIDENT_ID_CARD_WEIGHTS[i];
    }
    char expected = RESIDENT_ID_CARD_CHECKSUM_CODES[sum % 11];
    return expected == idCard.charAt(17);
  }

  private void validatePassport(String passportNo) {
    if (!passportNo.matches("^[A-Z0-9]{5,17}$")) {
      throw badRequest("护照号码格式不正确");
    }
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private int percent(int complete, int total) {
    if (total <= 0) {
      return 0;
    }
    return Math.round((complete * 100f) / total);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
}
