package elexvx.admin.plugin;

public interface PluginBootstrapExtension {
  default void onStart(String pluginId) {}
  default void onStop(String pluginId) {}
}
