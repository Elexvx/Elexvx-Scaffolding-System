export interface RedisInfo {
  redis_version?: string;
  redis_mode?: string;
  tcp_port?: string;
  connected_clients?: string;
  uptime_in_days?: string;
  used_memory_human?: string;
  used_memory_peak_human?: string;
  mem_fragmentation_ratio?: string;
  keyspace_hits?: string;
  keyspace_misses?: string;
  instantaneous_ops_per_sec?: number;
  keyCount?: number;
  timestamp?: number;
  usedMemory?: number;
  hitCount?: number;
  missCount?: number;
  [key: string]: string | number | undefined;
}

export interface CommandStat { command: string; calls: string; usec: string; usecPerCall: string; }
export interface KeyspaceInfo { db: string; keys: string; expires: string; avgTtl: string; }
export interface MetricPoint { timestamp: number; memory: number; ops: number; }
export interface RedisHealthStatus { redisEnabled: boolean; redisAvailable: boolean; redisMessage?: string; }
export interface RedisMonitorResponse {
  info: RedisInfo;
  commandStats: CommandStat[];
  keyspace: KeyspaceInfo[];
  timestamp?: number;
  usedMemory?: number;
  totalCommandsProcessed?: number;
  instantaneousOpsPerSec?: number;
  connectedClients?: number;
  keyCount?: number;
  hitCount?: number;
  missCount?: number;
}
