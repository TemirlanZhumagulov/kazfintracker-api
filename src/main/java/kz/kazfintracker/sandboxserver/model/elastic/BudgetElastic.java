package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

import static kz.kazfintracker.sandboxserver.util.ParserUtil.*;

@Data
public class BudgetElastic {
    private Integer id;
    private Integer idCategory;
    private String name;
    private double amountLimit;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BudgetElastic fromMap(Map<String, String> map) {
        BudgetElastic budget = new BudgetElastic();
        budget.setId(parseInt(map.getOrDefault("id", "0")));
        budget.setIdCategory(parseInt(map.getOrDefault("idCategory", "0")));
        budget.setName(map.get("name"));
        budget.setAmountLimit(Double.parseDouble(map.getOrDefault("amountLimit", "0.0")));
        budget.setActive(parseBoolean(map.get("active")));
        budget.setCreatedAt(parseLocalDateTime(map.get("createdAt")));
        budget.setUpdatedAt(parseLocalDateTime(map.get("updatedAt")));
        return budget;
    }
}

