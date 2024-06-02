package kz.kazfintracker.sandboxserver.model.mongo;

import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class BankAccountDto {
  public ObjectId id;
  public String name;
  public String symbol;
  public int color;
  public double startingValue;
  public int active; // Consider using boolean
  public int mainAccount; // Consider using boolean
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}

