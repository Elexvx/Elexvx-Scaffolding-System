package top.elexvx.admin.vo;

import java.util.List;
import java.util.Map;

public class RedisInfoVO {
  private Map<String, String> info;
  private List<CommandStat> commandStats;
  private List<KeyspaceInfo> keyspace;
  private Long timestamp;
  private Long usedMemory;
  private Long totalCommandsProcessed;
  private Long instantaneousOpsPerSec;
  private Long connectedClients;
  private Long keyCount;
  private Long hitCount;
  private Long missCount;

  public static class CommandStat {
    private String command;
    private String calls;
    private String usec;
    private String usecPerCall;

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public String getCalls() { return calls; }
    public void setCalls(String calls) { this.calls = calls; }
    public String getUsec() { return usec; }
    public void setUsec(String usec) { this.usec = usec; }
    public String getUsecPerCall() { return usecPerCall; }
    public void setUsecPerCall(String usecPerCall) { this.usecPerCall = usecPerCall; }
  }

  public static class KeyspaceInfo {
    private String db;
    private String keys;
    private String expires;
    private String avgTtl;

    public String getDb() { return db; }
    public void setDb(String db) { this.db = db; }
    public String getKeys() { return keys; }
    public void setKeys(String keys) { this.keys = keys; }
    public String getExpires() { return expires; }
    public void setExpires(String expires) { this.expires = expires; }
    public String getAvgTtl() { return avgTtl; }
    public void setAvgTtl(String avgTtl) { this.avgTtl = avgTtl; }
  }

  public Map<String, String> getInfo() { return info; }
  public void setInfo(Map<String, String> info) { this.info = info; }
  public List<CommandStat> getCommandStats() { return commandStats; }
  public void setCommandStats(List<CommandStat> commandStats) { this.commandStats = commandStats; }
  public List<KeyspaceInfo> getKeyspace() { return keyspace; }
  public void setKeyspace(List<KeyspaceInfo> keyspace) { this.keyspace = keyspace; }
  public Long getTimestamp() { return timestamp; }
  public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
  public Long getUsedMemory() { return usedMemory; }
  public void setUsedMemory(Long usedMemory) { this.usedMemory = usedMemory; }
  public Long getTotalCommandsProcessed() { return totalCommandsProcessed; }
  public void setTotalCommandsProcessed(Long totalCommandsProcessed) { this.totalCommandsProcessed = totalCommandsProcessed; }
  public Long getInstantaneousOpsPerSec() { return instantaneousOpsPerSec; }
  public void setInstantaneousOpsPerSec(Long instantaneousOpsPerSec) { this.instantaneousOpsPerSec = instantaneousOpsPerSec; }
  public Long getConnectedClients() { return connectedClients; }
  public void setConnectedClients(Long connectedClients) { this.connectedClients = connectedClients; }
  public Long getKeyCount() { return keyCount; }
  public void setKeyCount(Long keyCount) { this.keyCount = keyCount; }
  public Long getHitCount() { return hitCount; }
  public void setHitCount(Long hitCount) { this.hitCount = hitCount; }
  public Long getMissCount() { return missCount; }
  public void setMissCount(Long missCount) { this.missCount = missCount; }
}
