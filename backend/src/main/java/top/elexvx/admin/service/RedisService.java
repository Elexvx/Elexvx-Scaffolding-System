package top.elexvx.admin.service;

import top.elexvx.admin.vo.RedisInfoVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "elexvx.redis.enabled", havingValue = "true")
public class RedisService {
  private static final Logger log = LoggerFactory.getLogger(RedisService.class);

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public RedisInfoVO getRedisInfo() {
    RedisInfoVO redisInfoVO = new RedisInfoVO();
    redisInfoVO.setTimestamp(System.currentTimeMillis());
    try {
      // 获取Redis信息
      Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> 
          connection.serverCommands().info()
      );
      
      // 转换为Map
      Map<String, String> infoMap = new HashMap<>();
      if (info != null) {
        for (Object key : info.keySet()) {
          infoMap.put(key.toString(), info.get(key).toString());
        }
      }
      
      redisInfoVO.setInfo(infoMap);
      redisInfoVO.setUsedMemory(parseLong(infoMap.get("used_memory")));
      redisInfoVO.setTotalCommandsProcessed(parseLong(infoMap.get("total_commands_processed")));
      redisInfoVO.setInstantaneousOpsPerSec(parseLong(infoMap.get("instantaneous_ops_per_sec")));
      redisInfoVO.setConnectedClients(parseLong(infoMap.get("connected_clients")));
      redisInfoVO.setHitCount(parseLong(infoMap.get("keyspace_hits")));
      redisInfoVO.setMissCount(parseLong(infoMap.get("keyspace_misses")));

      // 获取命令统计信息
      List<RedisInfoVO.CommandStat> commandStats = getCommandStats();
      redisInfoVO.setCommandStats(commandStats);
      
      // 获取keyspace信息
      List<RedisInfoVO.KeyspaceInfo> keyspaceInfos = getKeyspaceInfo();
      redisInfoVO.setKeyspace(keyspaceInfos);
      long totalKeys = 0;
      for (RedisInfoVO.KeyspaceInfo ks : keyspaceInfos) {
        Long keyCount = parseLong(ks.getKeys());
        if (keyCount != null) {
          totalKeys += keyCount;
        }
      }
      redisInfoVO.setKeyCount(totalKeys);
    } catch (RedisConnectionFailureException e) {
      throw new RuntimeException("无法连接到 Redis 服务，请检查 Redis 是否已启动且配置正确。", e);
    } catch (Exception e) {
      throw new RuntimeException("获取 Redis 信息时发生未知错误: " + e.getMessage(), e);
    }

    return redisInfoVO;
  }

  private Long parseLong(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private List<RedisInfoVO.CommandStat> getCommandStats() {
    List<RedisInfoVO.CommandStat> commandStats = new ArrayList<>();
    
    try {
      Properties commandStatsInfo = redisTemplate.execute((RedisCallback<Properties>) connection -> 
          connection.serverCommands().info("commandstats")
      );
      
      if (commandStatsInfo != null) {
        for (Object key : commandStatsInfo.keySet()) {
          String keyStr = key.toString();
          if (keyStr.startsWith("cmdstat_")) {
            String valueStr = commandStatsInfo.get(key).toString();
            
            RedisInfoVO.CommandStat stat = new RedisInfoVO.CommandStat();
            stat.setCommand(keyStr.substring(8)); // 移除 "cmdstat_" 前缀
            
            // 解析值，格式为: calls=xxx,usec=xxx,usec_per_call=xxx
            String[] parts = valueStr.split(",");
            for (String part : parts) {
              String[] keyValue = part.split("=");
              if (keyValue.length == 2) {
                switch (keyValue[0]) {
                  case "calls":
                    stat.setCalls(keyValue[1]);
                    break;
                  case "usec":
                    stat.setUsec(keyValue[1]);
                    break;
                  case "usec_per_call":
                    stat.setUsecPerCall(keyValue[1]);
                    break;
                }
              }
            }
            
            commandStats.add(stat);
          }
        }
      }
    } catch (Exception e) {
      log.warn("读取 Redis commandstats 失败，返回空统计", e);
    }
    
    return commandStats;
  }

  private List<RedisInfoVO.KeyspaceInfo> getKeyspaceInfo() {
    List<RedisInfoVO.KeyspaceInfo> keyspaceInfos = new ArrayList<>();
    
    try {
      Properties keyspaceInfo = redisTemplate.execute((RedisCallback<Properties>) connection -> 
          connection.serverCommands().info("keyspace")
      );
      
      if (keyspaceInfo != null) {
        for (Object key : keyspaceInfo.keySet()) {
          String keyStr = key.toString();
          if (keyStr.startsWith("db")) {
            String valueStr = keyspaceInfo.get(key).toString();
            
            RedisInfoVO.KeyspaceInfo info = new RedisInfoVO.KeyspaceInfo();
            info.setDb(keyStr);
            
            // 解析值，格式为: keys=xxx,expires=xxx,avg_ttl=xxx
            String[] parts = valueStr.split(",");
            for (String part : parts) {
              String[] keyValue = part.split("=");
              if (keyValue.length == 2) {
                switch (keyValue[0]) {
                  case "keys":
                    info.setKeys(keyValue[1]);
                    break;
                  case "expires":
                    info.setExpires(keyValue[1]);
                    break;
                  case "avg_ttl":
                    info.setAvgTtl(keyValue[1]);
                    break;
                }
              }
            }
            
            keyspaceInfos.add(info);
          }
        }
      }
    } catch (Exception e) {
      log.warn("读取 Redis keyspace 失败，返回空统计", e);
    }
    
    return keyspaceInfos;
  }
}
