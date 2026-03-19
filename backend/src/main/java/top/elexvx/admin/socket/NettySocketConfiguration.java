package top.elexvx.admin.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NettyServerProperties.class)
public class NettySocketConfiguration {
  @Bean
  @ConditionalOnProperty(prefix = "elexvx.netty", name = "enabled", havingValue = "true")
  public NettySocketServer nettySocketServer(
    NettyServerProperties properties,
    NettyChannelRegistry registry,
    NettyAuthService authService,
    ObjectMapper objectMapper
  ) {
    return new NettySocketServer(properties, registry, authService, objectMapper);
  }

  @Bean
  @ConditionalOnProperty(prefix = "elexvx.netty", name = "enabled", havingValue = "true")
  public NettySocketServerLifecycle nettySocketServerLifecycle(NettySocketServer server) {
    return new NettySocketServerLifecycle(server);
  }
}
