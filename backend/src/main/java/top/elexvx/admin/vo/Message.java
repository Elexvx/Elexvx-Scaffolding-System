package top.elexvx.admin.vo;

public class Message {
  private String id;
  private String content;
  private String type;
  private boolean status;
  private boolean collected;
  private String date;
  private String quality;

  public Message() {}

  public Message(String id, String content, String type, boolean status, boolean collected, String date, String quality) {
    this.id = id;
    this.content = content;
    this.type = type;
    this.status = status;
    this.collected = collected;
    this.date = date;
    this.quality = quality;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public boolean isStatus() { return status; }
  public void setStatus(boolean status) { this.status = status; }
  public boolean isCollected() { return collected; }
  public void setCollected(boolean collected) { this.collected = collected; }
  public String getDate() { return date; }
  public void setDate(String date) { this.date = date; }
  public String getQuality() { return quality; }
  public void setQuality(String quality) { this.quality = quality; }
}
