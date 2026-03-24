package elexvx.admin.service;

import elexvx.admin.config.RedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = {"elexvx.redis.enabled", "elexvx.redis.clear-on-startup"}, havingValue = "true")
public class RedisStartupCleanupRunner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(RedisStartupCleanupRunner.class);

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisProperties redisProperties;

  public RedisStartupCleanupRunner(RedisTemplate<String, Object> redisTemplate, RedisProperties redisProperties) {
    this.redisTemplate = redisTemplate;
    this.redisProperties = redisProperties;
  }

  @Override
  public void run(ApplicationArguments args) {
    Long beforeSize = redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
    redisTemplate.execute((RedisCallback<Void>) connection -> {
      connection.serverCommands().flushDb();
      return null;
    });
    Long afterSize = redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
    long before = beforeSize == null ? -1L : beforeSize;
    long after = afterSize == null ? -1L : afterSize;
    log.info("Redis startup cleanup finished. database={}, keysBefore={}, keysAfter={}", redisProperties.getDatabase(), before, after);
  }
}
