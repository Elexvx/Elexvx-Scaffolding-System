package elexvx.admin.socket;

/**
 * WebSocket 消息载荷（JSON）。
 *
 * <p>常用字段约定：
 * <ul>
 *   <li>{@code type}：消息类型（例如 auth / ping / pong / auth_ok / auth_fail）。</li>
 *   <li>{@code token}：鉴权令牌（仅在 auth 请求中使用）。</li>
 *   <li>{@code clientId}：客户端自定义标识（可选，用于多端区分）。</li>
 *   <li>{@code userId}：服务端识别的用户 ID（通常由服务端下行填充）。</li>
 *   <li>{@code timestamp}：毫秒时间戳，用于调试与时序校验。</li>
 * </ul>
 */
public class NettyPayload {
  private String type;
  private String token;
  private String clientId;
  private Long userId;
  private String message;
  private String category;
  private String priority;
  private long timestamp;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
