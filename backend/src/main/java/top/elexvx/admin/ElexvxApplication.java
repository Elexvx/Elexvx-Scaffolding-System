package top.elexvx.admin;

import com.anji.captcha.config.AjCaptchaServiceAutoConfiguration;
import com.anji.captcha.properties.AjCaptchaProperties;
import top.elexvx.admin.config.properties.ElexvxCoreProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@Import(AjCaptchaServiceAutoConfiguration.class)
@EnableConfigurationProperties({AjCaptchaProperties.class, ElexvxCoreProperties.class})
@MapperScan("top.elexvx.admin.mapper")
public class ElexvxApplication {
  public static void main(String[] args) {
    SpringApplication.run(ElexvxApplication.class, args);
  }
}
