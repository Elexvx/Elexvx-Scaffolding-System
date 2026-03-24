package elexvx.admin.service;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import elexvx.admin.entity.SecuritySetting;
import elexvx.admin.vo.CaptchaResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("tdesignCaptchaService")
public class CaptchaService {
  private static final String CAPTCHA_TYPE_IMAGE = "image";
  private static final String CAPTCHA_TYPE_DRAG = "drag";

  public static class CaptchaEntry {
    private final String code;
    private final long expiresAt;

    public CaptchaEntry(String code, long expiresAt) {
      this.code = code;
      this.expiresAt = expiresAt;
    }

    public String getCode() {
      return code;
    }

    public long getExpiresAt() {
      return expiresAt;
    }
  }

  private static final int CAPTCHA_EXPIRES_SECONDS = 120;
  private final Cache<String, CaptchaEntry> store;
  private final Random random = new Random();
  private final SecuritySettingService securitySettingService;
  private final com.anji.captcha.service.CaptchaService ajCaptchaService;
  private final boolean captchaFeatureEnabled;

  public CaptchaService(
      SecuritySettingService securitySettingService,
      com.anji.captcha.service.CaptchaService ajCaptchaService,
      VerificationCacheService verificationCacheService,
      @Value("${elexvx.security.captcha.enabled:true}") boolean captchaFeatureEnabled) {
    this.securitySettingService = securitySettingService;
    this.ajCaptchaService = ajCaptchaService;
    this.store = verificationCacheService.captchaCache();
    this.captchaFeatureEnabled = captchaFeatureEnabled;
  }

  public CaptchaResult generate() {
    SecuritySetting setting = securitySettingService.getOrCreate();
    if (!captchaFeatureEnabled || Boolean.FALSE.equals(setting.getCaptchaEnabled())) {
      CaptchaResult result = new CaptchaResult();
      result.setEnabled(false);
      return result;
    }

    String type = normalizeCaptchaType(setting.getCaptchaType());
    int imageLength = setting.getImageCaptchaLength() != null ? setting.getImageCaptchaLength() : 5;
    int noiseLines = setting.getImageCaptchaNoiseLines() != null ? setting.getImageCaptchaNoiseLines() : 8;
    int dragWidth = setting.getDragCaptchaWidth() != null ? setting.getDragCaptchaWidth() : 310;
    int dragHeight = setting.getDragCaptchaHeight() != null ? setting.getDragCaptchaHeight() : 155;
    int dragThreshold = setting.getDragCaptchaThreshold() != null ? setting.getDragCaptchaThreshold() : 98;

    if (CAPTCHA_TYPE_DRAG.equals(type)) {
      CaptchaResult result = new CaptchaResult("", "", CAPTCHA_EXPIRES_SECONDS);
      result.setType(type);
      result.setWidth(dragWidth);
      result.setHeight(dragHeight);
      result.setThreshold(dragThreshold);
      result.setEnabled(true);
      return result;
    }

    String code = CAPTCHA_TYPE_DRAG.equals(type) ? UUID.randomUUID().toString() : randomCode(imageLength);
    String id = UUID.randomUUID().toString();
    String image = CAPTCHA_TYPE_DRAG.equals(type)
        ? renderDragBackground(dragWidth, dragHeight)
        : renderImageCaptcha(code, 120, 40, noiseLines);
    CaptchaEntry entry = new CaptchaEntry(
      code.toLowerCase(),
      Instant.now().plusSeconds(CAPTCHA_EXPIRES_SECONDS).toEpochMilli()
    );
    store.put(id, entry);
    CaptchaResult result = new CaptchaResult(id, image, CAPTCHA_EXPIRES_SECONDS);
    result.setType(type);
    result.setWidth(CAPTCHA_TYPE_DRAG.equals(type) ? dragWidth : 120);
    result.setHeight(CAPTCHA_TYPE_DRAG.equals(type) ? dragHeight : 40);
    if (CAPTCHA_TYPE_DRAG.equals(type)) {
      // 拖动验证码在前端完成滑动后直接回传 token 作为校验码。
      result.setDragToken(code);
      result.setThreshold(dragThreshold);
    }
    result.setEnabled(true);
    return result;
  }

  public boolean verify(String id, String input) {
    SecuritySetting setting = securitySettingService.getOrCreate();
    String type = normalizeCaptchaType(setting.getCaptchaType());
    if (CAPTCHA_TYPE_DRAG.equals(type)) {
      if (input == null || input.isBlank())
        return false;
      CaptchaVO captchaVO = new CaptchaVO();
      captchaVO.setCaptchaVerification(input.trim());
      ResponseModel response = ajCaptchaService.verification(captchaVO);
      return response != null && response.isSuccess();
    }
    CaptchaEntry entry = store.getIfPresent(id);
    if (entry == null) return false;
    store.invalidate(id);
    if (Instant.now().toEpochMilli() > entry.getExpiresAt()) return false;
    return entry.getCode().equalsIgnoreCase(input == null ? "" : input.trim().toLowerCase());
  }

  private String randomCode(int len) {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }

  private String renderImageCaptcha(String code, int w, int h, int noiseLines) {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, w, h);
    for (int i = 0; i < noiseLines; i++) {
      g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
      int x1 = random.nextInt(w), y1 = random.nextInt(h);
      int x2 = random.nextInt(w), y2 = random.nextInt(h);
      g.drawLine(x1, y1, x2, y2);
    }
    g.setFont(new Font("Arial", Font.BOLD, Math.max(18, h - 14)));
    for (int i = 0; i < code.length(); i++) {
      g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
      double rot = (random.nextDouble() - 0.5) * 0.6;
      int x = 20 + i * (w - 40) / Math.max(code.length(), 1);
      int y = (int) (h * 0.7);
      g.rotate(rot, x, y);
      g.drawString(String.valueOf(code.charAt(i)), x, y);
      g.rotate(-rot, x, y);
    }
    g.dispose();
    return toBase64(img);
  }

  private String renderDragBackground(int w, int h) {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setColor(new Color(240, 243, 248));
    g.fillRect(0, 0, w, h);
    for (int i = 0; i < 6; i++) {
      g.setColor(new Color(180 + random.nextInt(60), 180 + random.nextInt(60), 180 + random.nextInt(60)));
      int radius = 10 + random.nextInt(30);
      int x = random.nextInt(Math.max(w - radius, 1));
      int y = random.nextInt(Math.max(h - radius, 1));
      g.fillOval(x, y, radius, radius);
    }
    g.setColor(new Color(80, 90, 110));
    g.setFont(new Font("Arial", Font.PLAIN, Math.max(14, h / 6)));
    g.drawString("拖动滑块完成验证", Math.max(12, w / 10), Math.max(20, h / 2));
    g.dispose();
    return toBase64(img);
  }

  private String toBase64(BufferedImage img) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(img, "png", baos);
      String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
      return "data:image/png;base64," + b64;
    } catch (Exception e) {
      return "";
    }
  }

  private String normalizeCaptchaType(String value) {
    if (CAPTCHA_TYPE_DRAG.equalsIgnoreCase(value))
      return CAPTCHA_TYPE_DRAG;
    return CAPTCHA_TYPE_IMAGE;
  }
}
