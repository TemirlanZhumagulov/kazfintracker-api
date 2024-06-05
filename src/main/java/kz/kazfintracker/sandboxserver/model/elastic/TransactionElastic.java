package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
public class TransactionElastic {
    private String id;
    private LocalDateTime date; // Using LocalDate for date types
    private double amount;
    private String type;
    private String note;
    private Integer idCategory;
    private Integer idBankAccount;
    private Integer idBankAccountTransfer;
    private boolean recurring; // Using boolean for recurring
    private String recurrencyType;
    private Integer recurrencyPayDay;
    private LocalDateTime recurrencyFrom; // Using LocalDate for date types
    private LocalDateTime recurrencyTo; // Using LocalDate for date types
    private LocalDateTime createdAt; // Using LocalDateTime
    private LocalDateTime updatedAt; // Using LocalDateTime

    public static TransactionElastic fromMap(Map<String, String> map) {
        TransactionElastic transaction = new TransactionElastic();

        //@formatter:off
        transaction.setId                   (map.get("id"));
        transaction.setDate                 (parseLocalDateTime(map.get("date")));
        transaction.setAmount               (parseAmount(map.get("amount")));
        transaction.setType                 (map.get("type"));
        transaction.setNote                 (map.get("note"));
        transaction.setIdCategory           (parseInt(map.get("idCategory")));
        transaction.setIdBankAccount        (parseInt(map.get("idBankAccount")));
        transaction.setIdBankAccountTransfer(parseInt(map.get("idBankAccountTransfer")));
        transaction.setRecurring            (parseBoolean(map.get("recurring")));
        transaction.setRecurrencyType       (map.get("recurrencyType"));
        transaction.setRecurrencyPayDay     (parseInt(map.get("recurrencyPayDay")));
        transaction.setRecurrencyFrom       (parseLocalDateTime(map.get("recurrencyFrom")));
        transaction.setRecurrencyTo         (parseLocalDateTime(map.get("recurrencyTo")));
        transaction.setCreatedAt            (parseLocalDateTime(map.get("createdAt")));
        transaction.setUpdatedAt            (parseLocalDateTime(map.get("updatedAt")));
        //@formatter:on

        return transaction;
    }

    private static LocalDateTime parseLocalDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME) : null;
    }

    private static LocalDate parseLocalDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr) : null;
    }

    private static double parseAmount(String doubleStr) {
        return doubleStr != null ? (double) Math.round(Double.parseDouble(doubleStr) * 100) / 100 : 0.00;
    }

    private static int parseInt(String intStr) {
        return intStr != null ? Integer.parseInt(intStr) : 0;
    }

    private static boolean parseBoolean(String booleanStr) {
        return Boolean.parseBoolean(booleanStr);
    }

}
