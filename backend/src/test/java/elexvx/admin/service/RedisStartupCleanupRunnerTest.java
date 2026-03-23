package elexvx.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import elexvx.admin.config.RedisProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

@SuppressWarnings("unchecked")
class RedisStartupCleanupRunnerTest {

  @Test
  void runSkipsCleanupInProdByDefault() {
    RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setDatabase(0);
    redisProperties.setAllowClearOnStartupInProd(false);
    Environment environment = mock(Environment.class);
    when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});

    RedisStartupCleanupRunner runner = new RedisStartupCleanupRunner(redisTemplate, redisProperties, environment);

    runner.run(mock(ApplicationArguments.class));

    verifyNoInteractions(redisTemplate);
  }

  @Test
  void runDoesNotThrowWhenCleanupFails() {
    RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    RedisProperties redisProperties = new RedisProperties();
    Environment environment = mock(Environment.class);
    when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});
    when(redisTemplate.execute(any(RedisCallback.class))).thenThrow(new RuntimeException("flush failed"));

    RedisStartupCleanupRunner runner = new RedisStartupCleanupRunner(redisTemplate, redisProperties, environment);

    assertDoesNotThrow(() -> runner.run(mock(ApplicationArguments.class)));
    verify(redisTemplate, times(1)).execute(any(RedisCallback.class));
  }

  @Test
  void runFlushesRedisWhenCheckPasses() {
    RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setDatabase(0);
    redisProperties.setAllowClearOnStartupInProd(true);
    Environment environment = mock(Environment.class);
    when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
    when(redisTemplate.execute(any(RedisCallback.class)))
        .thenReturn(5L)
        .thenReturn(null)
        .thenReturn(0L);

    RedisStartupCleanupRunner runner = new RedisStartupCleanupRunner(redisTemplate, redisProperties, environment);

    assertDoesNotThrow(() -> runner.run(mock(ApplicationArguments.class)));
    verify(redisTemplate, times(3)).execute(any(RedisCallback.class));
  }
}
