package kz.kazfintracker.sandboxserver.model.mongo;

import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class RecurringTransactionAmountDto {
  public ObjectId id;
  public String from; // Consider using Date or LocalDate
  public String to; // Consider using Date or LocalDate
  public double amount;
  public int idTransaction;
  public String createdAt; // Consider using Date or LocalDateTime
  public String updatedAt; // Consider using Date or LocalDateTime
}
