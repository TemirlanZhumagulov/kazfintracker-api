package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RecurringTransactionAmountElastic {
    private Integer id;
    private String from;
    private String to;
    private double amount;
    private Integer idTransaction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecurringTransactionAmountElastic fromMap(Map<String, String> map) {
        RecurringTransactionAmountElastic recurringTransactionAmount = new RecurringTransactionAmountElastic();
        recurringTransactionAmount.setId(Integer.parseInt(map.getOrDefault("id", "0")));
        recurringTransactionAmount.setFrom(map.get("from"));
        recurringTransactionAmount.setTo(map.get("to"));
        recurringTransactionAmount.setAmount(Double.parseDouble(map.getOrDefault("amount", "0.0")));
        recurringTransactionAmount.setIdTransaction(Integer.parseInt(map.getOrDefault("idTransaction", "0")));
        recurringTransactionAmount.setCreatedAt(LocalDateTime.parse(map.get("createdAt")));
        recurringTransactionAmount.setUpdatedAt(LocalDateTime.parse(map.get("updatedAt")));
        return recurringTransactionAmount;
    }

}
