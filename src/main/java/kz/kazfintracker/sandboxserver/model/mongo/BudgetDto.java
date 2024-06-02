package kz.kazfintracker.sandboxserver.model.mongo;

import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class BudgetDto {
  public ObjectId id;
  public int idCategory;
  public String name;
  public double amountLimit;
  public int active; // Consider using boolean
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}
