package elexvx.admin.service.menu.support;

import java.util.ArrayList;
import java.util.List;

public final class MenuSeedCatalog {
  private MenuSeedCatalog() {}

  public static List<MenuSeedNode> defaults() {
    List<MenuSeedNode> list = new ArrayList<>();
    list.add(new MenuSeedNode(null, null, "DIR", "/user", "user", "LAYOUT", "/user/index", "个人中心", "User Center", "user-safety-filled", false, null, false, true, 2, "query,create,update,delete"));
    list.add(new MenuSeedNode("user", null, "PAGE", "index", "UserIndex", "/user/index", null, "个人中心", "User Center", "user", false, null, false, true, 0, "query,create,update,delete"));
    list.add(new MenuSeedNode(null, null, "DIR", "/system", "system", "LAYOUT", null, "系统设置", "System", "setting", false, null, false, true, 3, null));
    list.add(new MenuSeedNode("users", null, "PAGE", "user", "SystemUser", "/system/user/index", null, "用户管理", "Users", "user", false, null, false, true, 0, "create,update,delete,query"));
    list.add(new MenuSeedNode("users", null, "PAGE", "role", "SystemRole", "/system/role/index", null, "角色管理", "Roles", "usergroup", false, null, false, true, 1, "create,update,delete,query"));
    list.add(new MenuSeedNode("users", null, "PAGE", "org", "SystemOrg", "/system/org/index", null, "机构管理", "Organization", "tree-round-dot-vertical", false, null, false, true, 2, "create,update,delete,query"));
    list.add(new MenuSeedNode("system", null, "PAGE", "menu", "SystemMenu", "/system/menu/index", null, "目录/页面管理", "Menu Manager", "tree-round-dot-vertical", false, null, false, true, 0, "create,update,delete,query"));
    list.add(new MenuSeedNode("system", null, "PAGE", "personalize", "SystemPersonalize", "/system/personalize/index", null, "个性化设置", "Personalize", "setting-1", false, null, false, true, 2, "create,update,delete,query"));
    list.add(new MenuSeedNode("users", null, "PAGE", "log", "SystemLog", "/system/log/index", null, "操作日志", "Operation Logs", "file", false, null, false, true, 2, "create,update,delete,query"));
    list.add(new MenuSeedNode(null, null, "DIR", "/user-settings", "users", "LAYOUT", null, "用户设置", null, "usergroup", false, null, false, true, 4, null));
    list.add(new MenuSeedNode("system", null, "PAGE", "storage", "SystemStorage", "/system/storage/index", null, "对象存储", "Object Storage", "cloud-upload", false, null, false, true, 3, "update,query"));
    list.add(new MenuSeedNode("system", null, "PAGE", "verification", "SystemVerification", "/system/verification/index", null, "验证设置", "Verification", "check-circle", false, null, false, true, 4, "update,query"));
    list.add(new MenuSeedNode("system", null, "PAGE", "security", "SystemSecurity", "/system/security/index", null, "安全设置", "Security", "lock-on", false, null, false, true, 5, "update,query"));
    list.add(new MenuSeedNode("system", null, "PAGE", "sensitive", "SystemSensitive", "/system/sensitive/index", null, "敏感词拦截", "Sensitive Words", "filter", false, null, false, true, 5, "create,update,delete,query"));
    list.add(new MenuSeedNode(null, null, "DIR", "/monitor", "SystemMonitor", "LAYOUT", null, "系统监控", "Monitor", "chart-bar", false, null, false, true, 1, "query,create,update,delete"));
    list.add(new MenuSeedNode("SystemMonitor", null, "PAGE", "online-user", "SystemMonitorOnlineUser", "/system/monitor/online-user/index", null, "在线用户", "Online User", "usergroup-add", false, null, false, true, 0, "create,update,delete,query"));
    list.add(new MenuSeedNode("SystemMonitor", null, "PAGE", "server", "SystemMonitorServer", "/system/monitor/server/index", null, "服务监控", "Server Monitor", "server", false, null, false, true, 1, "create,update,delete,query"));
    list.add(new MenuSeedNode("SystemMonitor", null, "PAGE", "redis", "SystemMonitorRedis", "/system/monitor/redis/index", null, "Redis监控", "Redis Monitor", "chart-3d", false, null, false, true, 2, "query,create,update,delete"));
    list.add(new MenuSeedNode(null, null, "DIR", "/example", "example", "LAYOUT", "/example/goods", "示例页面", "Examples", "file", false, null, false, true, 0, null));
    list.add(new MenuSeedNode("example", null, "PAGE", "goods", "ExampleGoods", "/example/goods/index", null, "商品管理", "Goods", null, false, null, false, true, 0, "create,update,delete,query"));
    list.add(new MenuSeedNode("example", null, "PAGE", "order", "ExampleOrder", "/example/order/index", null, "订单管理", "Orders", null, false, null, false, true, 1, "create,update,delete,query"));
    list.add(new MenuSeedNode(null, null, "DIR", "/announcement", "announcement", "LAYOUT", "/announcement/table", "公告管理", "Announcements", "notification", false, null, false, true, 5, null));
    list.add(new MenuSeedNode("announcement", null, "PAGE", "table", "AnnouncementTable", "/announcement/table/index", null, "公告列表", "Announcement Table", null, false, null, false, true, 0, "create,update,delete,query"));
    list.add(new MenuSeedNode("announcement", null, "PAGE", "cards", "AnnouncementCards", "/announcement/cards/index", null, "公告卡片", "Announcement Cards", null, false, null, false, true, 1, "create,update,delete,query"));
    list.add(new MenuSeedNode(null, null, "DIR", "/console", "console", "LAYOUT", "/console/download", "文件下载", "Console", "download", false, null, false, true, 6, null));
    list.add(new MenuSeedNode("console", null, "PAGE", "download", "ConsoleDownload", "/console/download/index", null, "文件下载", "File Download", "download", false, null, false, true, 0, "create,update,delete,query"));
    return list;
  }
}
