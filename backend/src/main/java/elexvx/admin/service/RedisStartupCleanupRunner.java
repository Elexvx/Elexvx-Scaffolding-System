package elexvx.admin.service;

import elexvx.admin.config.RedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = {"elexvx.redis.enabled", "elexvx.redis.clear-on-startup"}, havingValue = "true")
public class RedisStartupCleanupRunner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(RedisStartupCleanupRunner.class);
  private static final String PROD_PROFILE = "prod";

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisProperties redisProperties;
  private final Environment environment;

  public RedisStartupCleanupRunner(
      RedisTemplate<String, Object> redisTemplate,
      RedisProperties redisProperties,
      Environment environment) {
    this.redisTemplate = redisTemplate;
    this.redisProperties = redisProperties;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (isProdProfileActive() && !redisProperties.isAllowClearOnStartupInProd()) {
      log.warn(
          "Redis startup cleanup skipped in production profile. database={}, set elexvx.redis.allow-clear-on-startup-in-prod=true to force cleanup.",
          redisProperties.getDatabase());
      return;
    }
    try {
      Long beforeSize = redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
      if (beforeSize == null) {
        log.warn("Redis startup cleanup skipped because dbSize check returned null. database={}", redisProperties.getDatabase());
        return;
      }
      redisTemplate.execute((RedisCallback<Void>) connection -> {
        connection.serverCommands().flushDb();
        return null;
      });
      Long afterSize = redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
      long after = afterSize == null ? -1L : afterSize;
      log.info(
          "Redis startup cleanup finished. database={}, keysBefore={}, keysAfter={}",
          redisProperties.getDatabase(),
          beforeSize,
          after);
    } catch (Exception ex) {
      log.error("Redis startup cleanup failed. database={}", redisProperties.getDatabase(), ex);
    }
  }

  private boolean isProdProfileActive() {
    for (String profile : environment.getActiveProfiles()) {
      if (PROD_PROFILE.equalsIgnoreCase(profile)) {
        return true;
      }
    }
    return false;
  }
}
