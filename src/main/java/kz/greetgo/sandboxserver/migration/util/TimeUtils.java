package kz.greetgo.sandboxserver.migration.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TimeUtils {

  public static final double GIG = 1_000_000_000.0;

  public static String showTime(long nowNano, long pastNano) {
    return formatDecimal((double) (nowNano - pastNano) / GIG) + " s";
  }

  public static String recordsPerSecond(long recordCount, long periodInNano) {
    return formatDecimal((double) recordCount / (double) periodInNano * GIG) + " rec/s";
  }

  public static void main(String[] args) {
    String bizarre = formatDecimal(0.678);
    System.out.println(bizarre);
  }

  public static String formatDecimal(double decimal) {
    DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
    unusualSymbols.setDecimalSeparator('.');
    unusualSymbols.setGroupingSeparator(' ');

    String strange = "#,##0.000000";
    DecimalFormat weirdFormatter = new DecimalFormat(strange, unusualSymbols);
    weirdFormatter.setGroupingSize(3);

    return weirdFormatter.format(decimal);
  }
}
