package top.elexvx.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityBaselineValidatorTest {
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withUserConfiguration(SecurityBaselineValidator.class)
    .withPropertyValues("spring.profiles.active=prod");

  @Test
  void shouldFailWhenProdCorsNotConfigured() {
    contextRunner.run(context -> assertThat(context).hasFailed());
  }
}
