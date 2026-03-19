package top.elexvx.admin.socket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
/**
 * Socket Channel 注册表。
 *
 * <p>用于维护当前在线连接集合，以及 Channel -> userId 的绑定关系。该类被 Netty I/O 线程并发访问，
 * 因此采用线程安全的数据结构。
 */
public class NettyChannelRegistry {
  private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  private final Map<Channel, Long> channelUsers = new ConcurrentHashMap<>();

  public void register(Channel channel, Long userId) {
    channels.add(channel);
    if (userId != null) {
      channelUsers.put(channel, userId);
    }
  }

  /**
   * 注销并清理 Channel 的用户绑定。
   */
  public void unregister(Channel channel) {
    channels.remove(channel);
    channelUsers.remove(channel);
  }

  public ChannelGroup channels() {
    return channels;
  }

  public int size() {
    return channels.size();
  }
}
