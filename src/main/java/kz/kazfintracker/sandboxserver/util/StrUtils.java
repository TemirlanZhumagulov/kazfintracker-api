package kz.kazfintracker.sandboxserver.util;

public class StrUtils {

  public static boolean isNullOrBlank(String str) {
    return str == null || str.isBlank();
  }

  public static String getEnvOrDefault(String env, String def) {
    var envValue = System.getenv(env);

    if (!StrUtils.isNullOrBlank(envValue)) {
      return envValue;
    }

    return def;
  }

}
