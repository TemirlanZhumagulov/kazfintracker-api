package kz.kazfintracker.sandboxserver.model.mongo;

import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
public class TransactionDto {
    public ObjectId id;
    public String date; // Consider using Date or LocalDate instead of String for date types
    public double amount;
    public String type;
    public String note;
    public int idCategory;
    public int idBankAccount;
    public Integer idBankAccountTransfer;
    public int recurring; // This is a boolean represented as an integer, consider using boolean
    public String recurrencyType;
    public Integer recurrencyPayDay;
    public String recurrencyFrom;
    public String recurrencyTo;
    public String createdAt; // Consider using Date or LocalDateTime
    public String updatedAt; // Consider using Date or LocalDateTime

}
