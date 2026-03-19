package top.elexvx.admin.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;

@Configuration
@ConditionalOnProperty(name = "elexvx.redis.enabled", havingValue = "true")
public class RedisConfig {

  private final RedisProperties redisProperties;

  public RedisConfig(RedisProperties redisProperties) {
    this.redisProperties = redisProperties;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    String host = Objects.requireNonNullElse(redisProperties.getHost(), "localhost");
    config.setHostName(Objects.requireNonNull(host));
    config.setPort(redisProperties.getPort());
    config.setDatabase(redisProperties.getDatabase());
    
    String password = Objects.requireNonNullElse(redisProperties.getPassword(), "");
    if (!password.isEmpty()) {
      config.setPassword(RedisPassword.of(password));
    }

    LettuceClientConfiguration clientConfiguration = buildClientConfiguration();
    return new LettuceConnectionFactory(config, clientConfiguration);
  }

  private LettuceClientConfiguration buildClientConfiguration() {
    int timeoutMs = redisProperties.getTimeout();
    if (timeoutMs <= 0) {
      return LettuceClientConfiguration.builder().build();
    }

    Duration timeout = Duration.ofMillis(timeoutMs);
    return LettuceClientConfiguration.builder()
        .commandTimeout(timeout)
        .clientOptions(ClientOptions.builder()
            .socketOptions(SocketOptions.builder().connectTimeout(timeout).build())
            .build())
        .build();
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new StringRedisSerializer());
    return template;
  }
}
