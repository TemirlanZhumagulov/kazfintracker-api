package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static kz.kazfintracker.sandboxserver.util.ParserUtil.*;

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
        bankAccount.setColor(parseInt(map.getOrDefault("color", "0")));
        bankAccount.setStartingValue(parseDouble(map.getOrDefault("startingValue", "0.0")));
        bankAccount.setActive(parseBoolean(map.get("active")));
        bankAccount.setMainAccount(parseBoolean(map.get("mainAccount")));
        bankAccount.setCreatedAt(parseLocalDateTime(map.get("createdAt")));
        bankAccount.setUpdatedAt(parseLocalDateTime(map.get("updatedAt")));
        return bankAccount;
    }
}
