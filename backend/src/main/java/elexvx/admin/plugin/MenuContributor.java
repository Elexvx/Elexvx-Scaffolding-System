package elexvx.admin.plugin;

import java.util.List;
import java.util.Map;

public interface MenuContributor {
  List<Map<String, Object>> provideMenus();
}
