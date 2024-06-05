package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

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
        budget.setId(Integer.parseInt(map.getOrDefault("id", "0")));
        budget.setIdCategory(Integer.parseInt(map.getOrDefault("idCategory", "0")));
        budget.setName(map.get("name"));
        budget.setAmountLimit(Double.parseDouble(map.getOrDefault("amountLimit", "0.0")));
        budget.setActive(Boolean.parseBoolean(map.get("active")));
        budget.setCreatedAt(LocalDateTime.parse(map.get("createdAt")));
        budget.setUpdatedAt(LocalDateTime.parse(map.get("updatedAt")));
        return budget;
    }
}

