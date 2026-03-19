package top.elexvx.admin.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket 文本帧协议处理器。
 *
 * <p>协议约定：
 * <ul>
 *   <li>客户端发送 JSON 文本帧，反序列化为 {@link NettyPayload}。</li>
 *   <li>当前支持 {@code type=auth}（携带 token 鉴权）与 {@code type=ping}（心跳）。</li>
 *   <li>鉴权成功后会将 Channel 与 userId 绑定到 {@link NettyChannelRegistry}，用于后续消息路由。</li>
 * </ul>
 *
 * <p>安全注意：未鉴权连接只能进行必要的握手/心跳；业务消息需要在此处或上层进一步做权限校验。
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);
  private final ObjectMapper objectMapper;
  private final NettyChannelRegistry registry;
  private final NettyAuthService authService;

  public NettyServerHandler(ObjectMapper objectMapper, NettyChannelRegistry registry, NettyAuthService authService) {
    this.objectMapper = objectMapper;
    this.registry = registry;
    this.authService = authService;
  }

  @Override
  /**
   * 新连接建立时先注册 Channel（userId 为空表示未鉴权）。
   */
  public void channelActive(ChannelHandlerContext ctx) {
    registry.register(ctx.channel(), null);
  }

  @Override
  /**
   * 连接断开时清理注册信息，避免 Channel 泄漏。
   */
  public void channelInactive(ChannelHandlerContext ctx) {
    registry.unregister(ctx.channel());
  }

  @Override
  /**
   * 处理客户端上行消息。
   *
   * <p>仅处理合法 JSON 且包含 type 的消息；未知类型按 debug 记录。
   */
  protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    NettyPayload payload = objectMapper.readValue(msg.text(), NettyPayload.class);
    if (payload == null || payload.getType() == null) {
      return;
    }
    switch (payload.getType()) {
      case "auth" -> handleAuth(ctx.channel(), payload);
      case "ping" -> handlePing(ctx.channel());
      default -> log.debug("Unhandled netty payload type: {}", payload.getType());
    }
  }

  @Override
  /**
   * 空闲超时处理：读空闲则关闭连接。
   */
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof IdleStateEvent event) {
      if (event.state() == IdleState.READER_IDLE) {
        ctx.close();
      }
    }
  }

  @Override
  /**
   * 异常保护：记录异常并关闭连接，避免异常 Channel 持续占用资源。
   */
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.warn("Netty socket error: {}", cause.getMessage());
    ctx.close();
  }

  private void handleAuth(Channel channel, NettyPayload payload) {
    Optional<Long> userId = authService.authenticate(payload.getToken());
    if (userId.isPresent()) {
      registry.register(channel, userId.get());
      send(channel, "auth_ok", "authorized");
    } else {
      send(channel, "auth_fail", "unauthorized");
      channel.close();
    }
  }

  private void handlePing(Channel channel) {
    send(channel, "pong", null);
  }

  private void send(Channel channel, String type, String message) {
    NettyPayload payload = new NettyPayload();
    payload.setType(type);
    payload.setMessage(message);
    payload.setTimestamp(Instant.now().toEpochMilli());
    try {
      channel.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(payload)));
    } catch (Exception e) {
      log.debug("Failed to write netty payload: {}", e.getMessage());
    }
  }
}
