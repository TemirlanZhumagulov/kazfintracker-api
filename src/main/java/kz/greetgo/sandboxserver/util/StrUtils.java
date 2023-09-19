package kz.greetgo.sandboxserver.util;

public class StrUtils {

  public static boolean isNullOrBlank(String str) {
    return str == null || str.isBlank();
  }

  public static <T> T getEnvOrDefault(String env, T def) {
    var envValue = System.getenv(env);

    if (!StrUtils.isNullOrBlank(envValue)) {
      return (T) envValue;
    }

    return def;
  }

}
