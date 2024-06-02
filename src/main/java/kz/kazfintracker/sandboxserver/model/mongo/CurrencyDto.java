package kz.kazfintracker.sandboxserver.model.mongo;

import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class CurrencyDto {
  public ObjectId id;
  public String symbol;
  public String code;
  public String name;
  public int mainCurrency; // Consider using boolean
}
