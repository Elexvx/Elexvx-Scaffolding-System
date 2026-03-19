package elexvx.admin.service;

import elexvx.admin.entity.VerificationSetting;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
public class EmailSenderService {
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public void sendLoginCode(VerificationSetting setting, String to, String code, int expiresInSeconds) {
    String subject = "登录验证码";
    String text = "你的验证码是: " + code + "\n有效期: " + expiresInSeconds + " 秒\n请勿泄露给他人。";
    sendText(setting, to, subject, text);
  }

  public void sendTest(VerificationSetting setting, String to) {
    String subject = "邮件发送测试";
    String text = "这是一封测试邮件。\n发送时间: " + fmt.format(LocalDateTime.now());
    sendText(setting, to, subject, text);
  }

  private void sendText(VerificationSetting setting, String to, String subject, String text) {
    JavaMailSenderImpl sender = buildSender(setting);

    String from = setting == null ? null : setting.getEmailFrom();
    if (from == null || from.isBlank()) {
      from = setting == null ? null : setting.getEmailUsername();
    }
    if (from == null || from.isBlank()) {
      throw badRequest("发件人不能为空");
    }

    try {
      MimeMessage message = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
      helper.setFrom(Objects.requireNonNull(from));
      helper.setTo(Objects.requireNonNull(to));
      helper.setSubject(Objects.requireNonNull(subject));
      helper.setText(Objects.requireNonNull(text), false);
      sender.send(message);
    } catch (Exception e) {
      throw badRequest("邮件发送失败: " + safeMessage(e));
    }
  }

  private JavaMailSenderImpl buildSender(VerificationSetting setting) {
    if (setting == null
      || setting.getEmailHost() == null || setting.getEmailHost().isBlank()
      || setting.getEmailPort() == null || setting.getEmailPort() <= 0
      || setting.getEmailUsername() == null || setting.getEmailUsername().isBlank()
      || setting.getEmailPassword() == null || setting.getEmailPassword().isBlank()) {
      throw badRequest("邮箱配置不完整");
    }

    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(setting.getEmailHost().trim());
    sender.setPort(setting.getEmailPort());
    sender.setUsername(setting.getEmailUsername().trim());
    sender.setPassword(setting.getEmailPassword());
    sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

    Properties props = sender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.timeout", "8000");
    props.put("mail.smtp.connectiontimeout", "8000");
    props.put("mail.smtp.writetimeout", "8000");
    props.put("mail.debug", "false");

    boolean ssl = Boolean.TRUE.equals(setting.getEmailSsl());
    if (ssl) {
      props.put("mail.smtp.ssl.enable", "true");
    } else {
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.starttls.required", "false");
    }

    return sender;
  }

  private String safeMessage(Exception e) {
    String msg = e.getMessage();
    if (msg == null) return e.getClass().getSimpleName();
    msg = msg.replace("\r", " ").replace("\n", " ").trim();
    if (msg.length() > 200) msg = msg.substring(0, 200);
    return msg;
  }
}
