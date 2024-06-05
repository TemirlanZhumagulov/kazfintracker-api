package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

import static kz.kazfintracker.sandboxserver.util.ParserUtil.parseInt;
import static kz.kazfintracker.sandboxserver.util.ParserUtil.parseLocalDateTime;

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
        category.setId(parseInt(map.getOrDefault("id", "0")));
        category.setName(map.get("name"));
        category.setSymbol(map.get("symbol"));
        category.setColor(parseInt(map.getOrDefault("color", "0")));
        category.setNote(map.getOrDefault("note", ""));
        category.setParentCategoryId(parseInt(map.get("parentCategoryId")));
        category.setCreatedAt(parseLocalDateTime(map.get("createdAt")));
        category.setUpdatedAt(parseLocalDateTime(map.get("updatedAt")));
        return category;
    }
}
