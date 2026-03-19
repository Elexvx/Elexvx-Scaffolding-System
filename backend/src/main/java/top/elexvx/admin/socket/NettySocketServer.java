package top.elexvx.admin.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty WebSocket 服务器。
 *
 * <p>启用条件由 {@link NettyServerProperties#isEnabled()} 控制。当前实现使用 WebSocket（路径 {@code /ws}）
 * 传输 JSON 文本帧（见 {@link NettyPayload}），并交由 {@link NettyServerHandler} 处理协议与鉴权。
 *
 * <p>生命周期：由外部组件在应用启动/停止时调用 {@link #start()} / {@link #stop()}。
 */
public class NettySocketServer {
  private static final Logger log = LoggerFactory.getLogger(NettySocketServer.class);
  private final NettyServerProperties properties;
  private final NettyChannelRegistry registry;
  private final NettyAuthService authService;
  private final ObjectMapper objectMapper;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel serverChannel;

  public NettySocketServer(
    NettyServerProperties properties,
    NettyChannelRegistry registry,
    NettyAuthService authService,
    ObjectMapper objectMapper
  ) {
    this.properties = properties;
    this.registry = registry;
    this.authService = authService;
    this.objectMapper = objectMapper;
  }

  /**
   * 启动服务端并绑定端口。
   *
   * <p>注意：该方法会创建并持有 Netty 事件循环线程组；若重复调用需要确保先 {@link #stop()}。
   */
  public void start() {
    if (!properties.isEnabled()) {
      log.info("Netty socket server disabled.");
      return;
    }
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap
      .group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .childOption(ChannelOption.SO_KEEPALIVE, true)
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) {
          ch.pipeline()
            .addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(65536))
            .addLast(new IdleStateHandler(60, 0, 0))
            .addLast(new WebSocketServerProtocolHandler("/ws", null, true))
            .addLast(new NettyServerHandler(objectMapper, registry, authService));
        }
      });
    try {
      serverChannel = bootstrap.bind(properties.getPort()).sync().channel();
      log.info("Netty socket server started on port {}", properties.getPort());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Netty socket server start interrupted.");
    }
  }

  /**
   * 停止服务端并释放资源。
   *
   * <p>关闭 Channel 后再优雅停止事件循环，避免连接未释放导致的端口占用。
   */
  public void stop() {
    if (serverChannel != null) {
      serverChannel.close();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully(1, 3, TimeUnit.SECONDS);
    }
    if (workerGroup != null) {
      workerGroup.shutdownGracefully(1, 3, TimeUnit.SECONDS);
    }
    log.info("Netty socket server stopped.");
  }
}
