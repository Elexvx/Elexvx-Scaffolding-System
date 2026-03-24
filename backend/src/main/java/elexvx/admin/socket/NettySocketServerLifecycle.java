package elexvx.admin.socket;

import org.springframework.context.SmartLifecycle;

public class NettySocketServerLifecycle implements SmartLifecycle {
  private final NettySocketServer server;
  private boolean running = false;

  public NettySocketServerLifecycle(NettySocketServer server) {
    this.server = server;
  }

  @Override
  public void start() {
    server.start();
    running = true;
  }

  @Override
  public void stop() {
    server.stop();
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
