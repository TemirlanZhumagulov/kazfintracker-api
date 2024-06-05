package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BankAccountElastic {
    private String id;
    private String name;
    private String symbol;
    private int color;
    private double startingValue;
    private boolean active;
    private boolean mainAccount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BankAccountElastic fromMap(Map<String, String> map) {
        BankAccountElastic bankAccount = new BankAccountElastic();
        bankAccount.setId(map.get("id"));
        bankAccount.setName(map.get("name"));
        bankAccount.setSymbol(map.get("symbol"));
        bankAccount.setColor(Integer.parseInt(map.getOrDefault("color", "0")));
        bankAccount.setStartingValue(Double.parseDouble(map.getOrDefault("startingValue", "0.0")));
        bankAccount.setActive(Boolean.parseBoolean(map.get("active")));
        bankAccount.setMainAccount(Boolean.parseBoolean(map.get("mainAccount")));
        bankAccount.setCreatedAt(LocalDateTime.parse(map.get("createdAt")));
        bankAccount.setUpdatedAt(LocalDateTime.parse(map.get("updatedAt")));
        return bankAccount;
    }
}
