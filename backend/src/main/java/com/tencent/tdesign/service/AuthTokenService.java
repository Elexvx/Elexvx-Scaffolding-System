package com.tencent.tdesign.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.security.AuthSession;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthTokenService {
  private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);
  private static final String TOKEN_KEY_PREFIX = "auth:token:";
  private static final String ALL_TOKENS_KEY = "auth:tokens";
  private static final String ALL_TOKENS_ZSET_KEY = "auth:tokens:z";
  private static final String USER_TOKEN_PREFIX = "auth:user:";
  private static final String USER_TOKEN_SUFFIX = ":tokens";

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public AuthTokenService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public String createToken(long userId, AuthSession session, long expiresInSeconds) {
    String token = UUID.randomUUID().toString().replace("-", "");
    session.setToken(token);
    session.setUserId(userId);
    session.setIssuedAt(System.currentTimeMillis());
    session.setExpiresAt(System.currentTimeMillis() + expiresInSeconds * 1000L);
    storeSession(token, session, expiresInSeconds);
    addUserToken(userId, token, expiresInSeconds, session.getExpiresAt());
    return token;
  }

  public AuthSession getSession(String token) {
    if (token == null || token.isBlank()) return null;
    String payload = getValue(tokenKey(token));
    if (payload == null || payload.isBlank()) return null;
    try {
      return objectMapper.readValue(payload, AuthSession.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  public void updateSession(String token, AuthSession session) {
    if (token == null || token.isBlank() || session == null) return;
    Long ttl = redisTemplate.getExpire(tokenKey(token));
    long expiresIn = ttl != null && ttl > 0 ? ttl : 0L;
    if (expiresIn <= 0) return;
    session.setExpiresAt(System.currentTimeMillis() + expiresIn * 1000L);
    storeSession(token, session, expiresIn);
    indexToken(token, session.getExpiresAt());
  }

  public void removeToken(String token) {
    AuthSession session = getSession(token);
    if (session != null) {
      removeUserToken(session.getUserId(), token);
      return;
    }
    redisTemplate.delete(tokenKey(token));
    redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, token);
    redisTemplate.opsForZSet().remove(ALL_TOKENS_ZSET_KEY, token);
  }

  public void removeUserToken(long userId, String token) {
    if (token == null || token.isBlank()) return;
    redisTemplate.delete(tokenKey(token));
    redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, token);
    redisTemplate.opsForZSet().remove(ALL_TOKENS_ZSET_KEY, token);
    redisTemplate.opsForSet().remove(userKey(userId), token);
  }

  public void removeUserTokens(long userId) {
    Set<Object> tokens = redisTemplate.opsForSet().members(userKey(userId));
    if (tokens != null) {
      for (Object tokenObj : tokens) {
        if (tokenObj == null) continue;
        String token = String.valueOf(tokenObj);
        removeUserToken(userId, token);
      }
    }
    redisTemplate.delete(userKey(userId));
  }

  public List<String> listAllTokens() {
    long now = System.currentTimeMillis();
    lazyMigrateLegacyTokens(now);
    pruneExpired(now);
    Set<Object> tokens = redisTemplate.opsForZSet().rangeByScore(ALL_TOKENS_ZSET_KEY, now, Double.POSITIVE_INFINITY);
    List<String> list = new ArrayList<>();
    if (tokens != null) {
      for (Object tokenObj : tokens) {
        if (tokenObj == null) continue;
        list.add(String.valueOf(tokenObj));
      }
    }
    return list;
  }

  public long countActiveTokens() {
    long now = System.currentTimeMillis();
    lazyMigrateLegacyTokens(now);
    pruneExpired(now);
    Long total = redisTemplate.opsForZSet().zCard(ALL_TOKENS_ZSET_KEY);
    return total == null ? 0L : total;
  }

  public List<String> listActiveTokensByPage(int offset, int limit) {
    int safeOffset = Math.max(offset, 0);
    int safeLimit = Math.max(limit, 1);
    long now = System.currentTimeMillis();
    lazyMigrateLegacyTokens(now);
    pruneExpired(now);
    Set<Object> tokens = redisTemplate.opsForZSet().reverseRange(ALL_TOKENS_ZSET_KEY, safeOffset, safeOffset + safeLimit - 1L);
    List<String> list = new ArrayList<>();
    if (tokens == null) {
      return list;
    }
    for (Object tokenObj : tokens) {
      if (tokenObj == null) continue;
      list.add(String.valueOf(tokenObj));
    }
    return list;
  }

  public Map<String, AuthSession> getSessions(Collection<String> tokens) {
    Map<String, AuthSession> sessions = new LinkedHashMap<>();
    if (tokens == null || tokens.isEmpty()) {
      return sessions;
    }
    List<String> keys = new ArrayList<>();
    List<String> tokenList = new ArrayList<>();
    for (String token : tokens) {
      if (!StringUtils.hasText(token)) continue;
      tokenList.add(token);
      keys.add(tokenKey(token));
    }
    if (keys.isEmpty()) {
      return sessions;
    }
    List<Object> payloads = redisTemplate.opsForValue().multiGet(keys);
    for (int i = 0; i < tokenList.size(); i++) {
      String token = tokenList.get(i);
      Object payload = payloads == null || i >= payloads.size() ? null : payloads.get(i);
      if (payload == null) {
        sessions.put(token, null);
        continue;
      }
      try {
        sessions.put(token, objectMapper.readValue(String.valueOf(payload), AuthSession.class));
      } catch (JsonProcessingException e) {
        sessions.put(token, null);
      }
    }
    return sessions;
  }

  public List<String> listUserTokens(long userId) {
    Set<Object> tokens = redisTemplate.opsForSet().members(userKey(userId));
    List<String> list = new ArrayList<>();
    if (tokens != null) {
      for (Object tokenObj : tokens) {
        if (tokenObj == null) continue;
        list.add(String.valueOf(tokenObj));
      }
    }
    return list;
  }

  private void storeSession(String token, AuthSession session, long expiresInSeconds) {
    try {
      String payload = objectMapper.writeValueAsString(session);
      redisTemplate.opsForValue().set(tokenKey(token), payload, Duration.ofSeconds(expiresInSeconds));
    } catch (JsonProcessingException serializationException) {
      log.warn("序列化登录会话失败，token={}", token, serializationException);
    }
  }

  private void addUserToken(long userId, String token, long expiresInSeconds, long expiresAtMs) {
    redisTemplate.opsForSet().add(userKey(userId), token);
    redisTemplate.opsForSet().add(ALL_TOKENS_KEY, token);
    indexToken(token, expiresAtMs);
    Long currentTtl = redisTemplate.getExpire(userKey(userId));
    if (currentTtl == null || currentTtl < expiresInSeconds) {
      redisTemplate.expire(userKey(userId), Duration.ofSeconds(expiresInSeconds));
    }
  }

  private void indexToken(String token, long expiresAtMs) {
    if (!StringUtils.hasText(token)) {
      return;
    }
    long normalizedExpiresAt = expiresAtMs > 0 ? expiresAtMs : System.currentTimeMillis();
    redisTemplate.opsForZSet().add(ALL_TOKENS_ZSET_KEY, token, normalizedExpiresAt);
  }

  private void pruneExpired(long now) {
    redisTemplate.opsForZSet().removeRangeByScore(ALL_TOKENS_ZSET_KEY, Double.NEGATIVE_INFINITY, now);
  }

  private void lazyMigrateLegacyTokens(long now) {
    Set<Object> legacyTokens = redisTemplate.opsForSet().members(ALL_TOKENS_KEY);
    if (legacyTokens == null || legacyTokens.isEmpty()) {
      return;
    }
    for (Object tokenObj : legacyTokens) {
      if (tokenObj == null) {
        continue;
      }
      String token = String.valueOf(tokenObj);
      AuthSession session = getSession(token);
      if (session == null || session.getExpiresAt() <= now) {
        redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, token);
        redisTemplate.opsForZSet().remove(ALL_TOKENS_ZSET_KEY, token);
        continue;
      }
      indexToken(token, session.getExpiresAt());
    }
  }

  private String getValue(String key) {
    Object value = redisTemplate.opsForValue().get(key);
    return value == null ? null : String.valueOf(value);
  }

  private String tokenKey(String token) {
    return TOKEN_KEY_PREFIX + token;
  }

  private String userKey(long userId) {
    return USER_TOKEN_PREFIX + userId + USER_TOKEN_SUFFIX;
  }
}
