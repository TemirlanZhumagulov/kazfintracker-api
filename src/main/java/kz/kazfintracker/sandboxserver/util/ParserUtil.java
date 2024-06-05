package kz.kazfintracker.sandboxserver.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParserUtil {

    public static LocalDateTime parseLocalDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME) : null;
    }

    public static LocalDate parseLocalDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr) : null;
    }

    public static double parseAmount(String doubleStr) {
        return doubleStr != null ? (double) Math.round(Double.parseDouble(doubleStr) * 100) / 100 : 0.00;
    }

    public static int parseInt(String intStr) {
        return intStr != null ? Integer.parseInt(intStr) : 0;
    }

    public static boolean parseBoolean(String booleanStr) {
        return Boolean.parseBoolean(booleanStr);
    }

}
