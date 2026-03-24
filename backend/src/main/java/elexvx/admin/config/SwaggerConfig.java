package elexvx.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tdesignOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("系统接口文档")
                        .description("企业级管理系统后端接口文档")
                        .version("v1.0.0")
                        .license(new Info().getLicense() != null ? new Info().getLicense() : new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
