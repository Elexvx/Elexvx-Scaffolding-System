package elexvx.admin.config;

import elexvx.admin.service.EmailSenderService;
import elexvx.admin.service.SmsSenderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerificationModuleConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "elexvx.modules.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
  @ConditionalOnClass(name = {
    "com.aliyuncs.DefaultAcsClient",
    "com.tencentcloudapi.sms.v20210111.SmsClient"
  })
  public SmsSenderService smsSenderService() {
    return new SmsSenderService();
  }

  @Bean
  @ConditionalOnProperty(prefix = "elexvx.modules.email", name = "enabled", havingValue = "true", matchIfMissing = true)
  @ConditionalOnClass(name = {
    "org.springframework.mail.javamail.JavaMailSenderImpl",
    "jakarta.mail.internet.MimeMessage"
  })
  public EmailSenderService emailSenderService() {
    return new EmailSenderService();
  }
}
