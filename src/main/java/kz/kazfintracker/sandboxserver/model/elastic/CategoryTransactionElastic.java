package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CategoryTransactionElastic {
    private Integer id;
    private String name;
    private String symbol;
    private Integer color;
    private String note;
    private Integer parentCategoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryTransactionElastic fromMap(Map<String, String> map) {
        CategoryTransactionElastic category = new CategoryTransactionElastic();
        category.setId(Integer.parseInt(map.getOrDefault("id", "0")));
        category.setName(map.get("name"));
        category.setSymbol(map.get("symbol"));
        category.setColor(Integer.parseInt(map.getOrDefault("color", "0")));
        category.setNote(map.getOrDefault("note", ""));
        category.setParentCategoryId(map.containsKey("parentCategoryId") ? Integer.parseInt(map.get("parentCategoryId")) : null);
        category.setCreatedAt(LocalDateTime.parse(map.get("createdAt")));
        category.setUpdatedAt(LocalDateTime.parse(map.get("updatedAt")));
        return category;
    }
}
