package top.elexvx.admin.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.elexvx.admin.entity.Notification;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NettySocketService {
  private static final Logger log = LoggerFactory.getLogger(NettySocketService.class);
  private final NettyChannelRegistry registry;
  private final ObjectMapper objectMapper;

  public NettySocketService(NettyChannelRegistry registry, ObjectMapper objectMapper) {
    this.registry = registry;
    this.objectMapper = objectMapper;
  }

  public void broadcastNotification(Notification notification, String message) {
    NettyPayload payload = new NettyPayload();
    payload.setType("notification");
    payload.setMessage(message);
    payload.setCategory(notification.getType());
    payload.setPriority(notification.getPriority());
    payload.setTimestamp(Instant.now().toEpochMilli());
    payload.setClientId(String.valueOf(notification.getId()));
    try {
      String json = objectMapper.writeValueAsString(payload);
      registry.channels().writeAndFlush(new TextWebSocketFrame(json));
    } catch (Exception e) {
      log.debug("Netty broadcast failed: {}", e.getMessage());
    }
  }
}
