package kz.kazfintracker.sandboxserver.model.mongo;

import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class CategoryTransactionDto {
  public ObjectId id;
  public String name;
  public String symbol;
  public int color;
  public String note;
  public Integer parent;
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}
