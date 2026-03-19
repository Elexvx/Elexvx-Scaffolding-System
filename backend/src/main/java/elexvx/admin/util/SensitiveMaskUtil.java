package elexvx.admin.util;

public final class SensitiveMaskUtil {
  private SensitiveMaskUtil() {}

  public static boolean isMasked(String value) {
    return value != null && value.indexOf('*') >= 0;
  }
}
