package kz.kazfintracker.sandboxserver.migration.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RND {
  public static final String ENG_STR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static final String eng_str = "abcdefghijklmnopqrstuvwxyz";
  public static final String DEG_STR = "0123456789";
  public static final char[] ALL_ENG = (ENG_STR + eng_str + DEG_STR).toCharArray();

  public static final Random RAND = new Random();

  public static String str(int len) {
    char ret[] = new char[len];
    for (int i = 0; i < len; i++) {
      ret[i] = ALL_ENG[_int(ALL_ENG.length)];
    }
    return new String(ret);
  }

  public static int _int(int boundValue) {
    int sign = 1;
    if (boundValue < 0) {
      sign = -1;
      boundValue = -boundValue;
    }

    return sign * RAND.nextInt(boundValue);
  }

  public static Date date(int fromDays, int toDays) {
    int days = fromDays + _int(toDays - fromDays);
    Calendar cal = new GregorianCalendar();
    cal.add(Calendar.DAY_OF_YEAR, days);
    return cal.getTime();
  }

  public static boolean bool(float truePercent) {
    return _int(100_000) < truePercent * 1000f;
  }

  public static Date dateYears(int maxAge, int minAge) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    int minBirthYear = currentYear + maxAge;
    if(minBirthYear <= 0) minBirthYear = 1;
    int maxBirthYear = currentYear + minAge;

    int birthYear = ThreadLocalRandom.current().nextInt(minBirthYear, maxBirthYear + 1);

    int birthMonth = ThreadLocalRandom.current().nextInt(0, 12);
    int birthDay = ThreadLocalRandom.current().nextInt(1, 29);  // Assume 28 days in February for simplicity

    Calendar calendar = Calendar.getInstance();
    calendar.set(birthYear, birthMonth, birthDay);

    return calendar.getTime();
  }
}
